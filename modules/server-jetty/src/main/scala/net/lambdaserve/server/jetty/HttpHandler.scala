package net.lambdaserve.server.jetty

import net.lambdaserve.filters.FilterEngine
import net.lambdaserve.http.{Method, MultiPart, Request}
import net.lambdaserve.types.MultiMap
import org.eclipse.jetty.http.MultiPartFormData
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.thread.Invocable
import org.eclipse.jetty.util.{Callback, Promise}
import org.eclipse.jetty.{http as jettyHttp, server as jetty}

import java.nio.file.Files
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.*

class HttpHandler(filterEngine: FilterEngine) extends jetty.Handler.Abstract():

  override def handle(
    in: jetty.Request,
    out: jetty.Response,
    callback: Callback
  ): Boolean =
    val queryParams =
      val params = jetty.Request
        .extractQueryParameters(in)
        .iterator()
        .asScala
        .flatMap(f => f.getValues.asScala.map(v => f.getName -> v))
        .toSeq

      MultiMap(params*)

    val requestCookies = Cookies.extractCookies(in)

    val contentType = in.getHeaders.get(jettyHttp.HttpHeader.CONTENT_TYPE)

    val response =
      if contentType != null && contentType.startsWith(
          jettyHttp.MimeTypes.Type.MULTIPART_FORM_DATA.asString()
        )
      then
        val boundary = jettyHttp.MultiPart.extractBoundary(contentType)
        val parser   = new jettyHttp.MultiPartFormData.Parser(boundary)
        parser.setFilesDirectory(Files.createTempDirectory("tmpDirPrefix"))

        val multiparts =
          scala.concurrent.Promise[Iterator[jettyHttp.MultiPart.Part]]

        parser.parse(
          in,
          new Promise.Invocable[MultiPartFormData.Parts]:
            override def succeeded(result: MultiPartFormData.Parts): Unit =
              multiparts.success(result.iterator().asScala)

            override def failed(x: Throwable): Unit =
              multiparts.failure(x)

            override def getInvocationType: Invocable.InvocationType =
              Invocable.InvocationType.BLOCKING
        )

        val scalaMultiparts = Await
          .result(multiparts.future, 60.seconds)
          .map(part =>
            MultiPart(
              name = Option(part.getName),
              fileName = Option(part.getFileName),
              headers =
                val pairs =
                  part.getHeaders.asScala.flatMap { f =>
                    f.getValueList.asScala.map(v => f.getName -> v)
                  }
                MultiMap(pairs.toSeq*)
              ,
              content = Content.Source.asInputStream(part.getContentSource)
            )
          )

        val request = Request(
          scheme = in.getHttpURI.getScheme,
          method = Method.valueOf(in.getMethod),
          path = in.getHttpURI.getPath,
          pathParams = MultiMap(),
          headers = in.getHeaders.toMultiMap,
          query = queryParams,
          cookies = requestCookies,
          multipartForm = scalaMultiparts.toSeq
        )
        filterEngine.processRequest(request)
      else if jettyHttp.MimeTypes.Type.FORM_ENCODED.is(contentType) then
        val formParams =
          val params = jetty.FormFields
            .getFields(in)
            .iterator()
            .asScala
            .flatMap(f => f.getValues.asScala.map(v => f.getName -> v))
            .toSeq

          MultiMap(params*)

        val request = Request(
          scheme = in.getHttpURI.getScheme,
          method = Method.valueOf(in.getMethod),
          path = in.getHttpURI.getPath,
          pathParams = MultiMap(),
          headers = in.getHeaders.toMultiMap,
          query = queryParams,
          cookies = requestCookies,
          form = formParams
        )
        filterEngine.processRequest(request)
      else
        val request = Request(
          scheme = in.getHttpURI.getScheme,
          method = Method.valueOf(in.getMethod),
          path = in.getHttpURI.getPath,
          pathParams = MultiMap(),
          headers = in.getHeaders.toMultiMap,
          query = queryParams,
          cookies = requestCookies,
          requestContent = jetty.Request.asInputStream(in)
        )
        filterEngine.processRequest(request)

    out.setStatus(response.status.code)
    for cookie <- response.cookies.values do
      val jettyCookie = Cookies.toJettyCookie(cookie)
      jetty.Response.addCookie(out, jettyCookie)
    val responseHeaders = out.getHeaders
    response.length.foreach(
      responseHeaders.put(jettyHttp.HttpHeader.CONTENT_LENGTH, _)
    )

    response.headers.foreach { case (headerName, headerValue) =>
      responseHeaders.put(headerName, headerValue.asJava)
    }

    val os = jetty.Response.asBufferedOutputStream(in, out)
    response.bodyWriter(os)
    os.close()
    callback.succeeded()

    true
