package net.lambdaserve.server.jetty

import net.lambdaserve.core.filters.FilterEngine
import net.lambdaserve.core.http.RequestHeader.parseQuery
import net.lambdaserve.core.http.{Method, Request, RequestHeader}
import org.eclipse.jetty.http.{HttpFields, HttpHeader}
import org.eclipse.jetty.server as jetty
import org.eclipse.jetty.util.Callback

import scala.jdk.CollectionConverters.*

class HttpHandler(filterEngine: FilterEngine) extends jetty.Handler.Abstract():

  override def handle(
    in: jetty.Request,
    out: jetty.Response,
    callback: Callback
  ): Boolean =
    val requestHeader = extractHeader(
      scheme = in.getHttpURI.getScheme,
      method = in.getMethod,
      path = in.getHttpURI.getPath,
      headers = in.getHeaders,
      queryString = Option(in.getHttpURI.getQuery)
    )
    val request = Request(requestHeader, jetty.Request.asInputStream(in))

    val response = filterEngine.processRequest(request)

    out.setStatus(response.status.code)

    val responseHeaders = out.getHeaders
    response.length.foreach(responseHeaders.put(HttpHeader.CONTENT_LENGTH, _))

    response.headers.foreach { case (headerName, headerValue) =>
      responseHeaders.put(headerName, headerValue.asJava)
    }

    val os = jetty.Response.asBufferedOutputStream(in, out)
    response.bodyWriter(os)
    os.close()
    callback.succeeded()

    true

private def extractHeader(
  scheme: String,
  method: String,
  path: String,
  headers: HttpFields,
  queryString: Option[String]
): RequestHeader =
  val requestQuery: Map[String, IndexedSeq[String]] =
    queryString.fold(Map.empty[String, IndexedSeq[String]])(parseQuery)

  val contentType =
    headers.getFields("Content-Type").asScala.headOption.map(_.getValue)

  val contentLength =
    headers.getFields("Content-Length").asScala.headOption.map(_.getLongValue)

  val contentEncoding =
    headers.getFields("Content-Encoding").asScala.headOption.map(_.getValue)

  val headersMap: Map[String, IndexedSeq[String]] =
    DelegatingMap.make(headers)

  RequestHeader(
    scheme = scheme,
    method = Method.valueOf(method),
    path = path,
    pathParams = Map.empty,
    headers = headersMap,
    query = requestQuery,
    contentType = contentType,
    contentLength = contentLength,
    contentEncoding = contentEncoding
  )
