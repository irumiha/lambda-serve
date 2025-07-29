package net.lambdaserve.server.jetty

import net.lambdaserve.filters.FilterEngine
import net.lambdaserve.http.{Method, MultiPart, Request}
import org.eclipse.jetty.http.MultiPartFormData
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.util.{Callback, Promise}
import org.eclipse.jetty.{http as jettyHttp, server as jetty}

import java.nio.file.Files
import scala.collection.immutable.ArraySeq
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
      jetty.Request
        .extractQueryParameters(in)
        .toStringArrayMap
        .asScala
        .view
        .mapValues(_.toIndexedSeq)
        .toMap
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
            override def accept(
              result: MultiPartFormData.Parts,
              error: Throwable
            ): Unit =
              if error != null then multiparts.failure(error)
              else multiparts.success(result.iterator().asScala)
        )

        val scalaMultiparts = Await
          .result(multiparts.future, 60.seconds)
          .map(part =>
            MultiPart(
              name = Option(part.getName),
              fileName = Option(part.getFileName),
              headers = part.getHeaders.asScala
                .groupMap(_.getName)(_.getValue)
                .view
                .mapValues(_.toIndexedSeq)
                .toMap,
              content = Content.Source.asInputStream(part.getContentSource)
            )
          )

        val request = Request(
          scheme = in.getHttpURI.getScheme,
          method = Method.valueOf(in.getMethod),
          path = in.getHttpURI.getPath,
          pathParams = Map.empty,
          headers = DelegatingMap.make(in.getHeaders),
          query = queryParams,
          cookies = requestCookies,
          multipartForm = scalaMultiparts.toSeq
        )
        filterEngine.processRequest(request)
      else if jettyHttp.MimeTypes.Type.FORM_ENCODED.is(contentType) then
        val formParams =
          jetty.FormFields
            .getFields(in)
            .toStringArrayMap
            .asScala
            .view
            .mapValues(ArraySeq.from(_))
            .toMap

        val request = Request(
          scheme = in.getHttpURI.getScheme,
          method = Method.valueOf(in.getMethod),
          path = in.getHttpURI.getPath,
          pathParams = Map.empty,
          headers = DelegatingMap.make(in.getHeaders),
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
          pathParams = Map.empty,
          headers = DelegatingMap.make(in.getHeaders),
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
