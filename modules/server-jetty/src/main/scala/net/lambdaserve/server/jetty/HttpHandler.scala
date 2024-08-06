package net.lambdaserve.server.jetty

import net.lambdaserve.core.filters.FilterEngine
import net.lambdaserve.core.http.{Method, MultiPart, Request}
import org.eclipse.jetty.io.Content
import org.eclipse.jetty.{http as jettyHttp, server as jetty}
import org.eclipse.jetty.util.Callback

import java.nio.file.Files
import java.util
import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters.*

class HttpHandler(filterEngine: FilterEngine) extends jetty.Handler.Abstract():
  val inputTimeout    = 60
  val inputTimeutUnit = java.util.concurrent.TimeUnit.SECONDS

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

    val contentType = in.getHeaders.get(jettyHttp.HttpHeader.CONTENT_TYPE)

    val response =
      if jettyHttp.MimeTypes.Type.MULTIPART_FORM_DATA.is(contentType) then
        val boundary = jettyHttp.MultiPart.extractBoundary(contentType)
        val parser   = new jettyHttp.MultiPartFormData.Parser(boundary)
        parser.setFilesDirectory(Files.createTempDirectory("tmpDirPrefix"))
        val multiparts: Iterable[jettyHttp.MultiPart.Part] =
          parser.parse(in).get(inputTimeout, inputTimeutUnit).asScala

        val scalaMultiparts = multiparts.map(part =>
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
          multipartForm = scalaMultiparts.toSeq
        )
        filterEngine.processRequest(request)
      else if jettyHttp.MimeTypes.Type.FORM_ENCODED.is(contentType) then
        val formParams =
          jetty.FormFields
            .from(in)
            .get(inputTimeout, inputTimeutUnit)
            .toStringArrayMap
            .asScala
            .view.mapValues(ArraySeq.from(_))
            .toMap
          
        val request = Request(
          scheme = in.getHttpURI.getScheme,
          method = Method.valueOf(in.getMethod),
          path = in.getHttpURI.getPath,
          pathParams = Map.empty,
          headers = DelegatingMap.make(in.getHeaders),
          query = queryParams,
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
          requestContent = jetty.Request.asInputStream(in)
        )
        filterEngine.processRequest(request)

    out.setStatus(response.status.code)

    val responseHeaders = out.getHeaders
    response.length.foreach(responseHeaders.put(jettyHttp.HttpHeader.CONTENT_LENGTH, _))

    response.headers.foreach { case (headerName, headerValue) =>
      responseHeaders.put(headerName, headerValue.asJava)
    }

    val os = jetty.Response.asBufferedOutputStream(in, out)
    response.bodyWriter(os)
    os.close()
    callback.succeeded()

    true
