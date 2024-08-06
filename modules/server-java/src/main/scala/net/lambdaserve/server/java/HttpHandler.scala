package net.lambdaserve.server.java

import com.sun.net.httpserver
import com.sun.net.httpserver.{Headers, HttpExchange}
import net.lambdaserve.core.filters.FilterEngine
import net.lambdaserve.core.http.RequestHeader.parseQuery
import net.lambdaserve.core.http.{Header, Method, Request, RequestHeader}

import scala.jdk.CollectionConverters.*

class HttpHandler(filterEngine: FilterEngine) extends httpserver.HttpHandler:
  private def extractHeader(
    scheme: String,
    method: String,
    path: String,
    headers: Headers,
    queryString: Option[String]
  ): RequestHeader =
    val requestQuery: Map[String, IndexedSeq[String]] =
      queryString.fold(Map.empty[String, IndexedSeq[String]])(parseQuery)

    val headersMap: Map[String, IndexedSeq[String]] =
      headers.asScala.view.mapValues(_.asScala.toIndexedSeq).toMap

    RequestHeader(
      scheme = scheme,
      method = Method.valueOf(method),
      path = path,
      pathParams = Map.empty,
      headers = headersMap,
      query = requestQuery,
    )

  override def handle(exchange: HttpExchange): Unit =
    try
      val requestHeader = extractHeader(
        scheme = exchange.getRequestURI.getScheme,
        method = exchange.getRequestMethod,
        path = exchange.getRequestURI.getPath,
        headers = exchange.getRequestHeaders,
        queryString = Option(exchange.getRequestURI.getQuery)
      )

      val request = Request(requestHeader, exchange.getRequestBody)

      val response = filterEngine.processRequest(request)

      val responseHeaders = exchange.getResponseHeaders

      response.length.foreach(l =>
        responseHeaders.put(
          Header.ContentLength.name,
          java.util.List.of(l.toString)
        )
      )
      response.headers.foreach { case (headerName, headerValue) =>
        responseHeaders.put(headerName, java.util.List.of(headerValue*))
      }
      exchange.sendResponseHeaders(response.status.code, 0)

      val os = exchange.getResponseBody
      response.bodyWriter(os)
      os.close()
    catch
      case e: Exception =>
        e.printStackTrace()
        exchange.sendResponseHeaders(500, 0)
        exchange.getResponseBody.close()
