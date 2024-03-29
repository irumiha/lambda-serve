package net.liftio
package lambdaserve.core

import http as lhttp

import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server.{Handler as JSHandler, Request as JSRequest, Response as JSResponse}
import org.eclipse.jetty.util.Callback

import scala.jdk.CollectionConverters.*

class HttpHandler(router: Router) extends JSHandler.Abstract():

  override def handle(in: JSRequest, out: JSResponse, callback: Callback): Boolean =
    val requestHeader = lhttp.RequestHeader(
      scheme = in.getHttpURI.getScheme,
      method = in.getMethod,
      path = in.getHttpURI.getPath,
      headers = in.getHeaders,
      queryString = Option(in.getHttpURI.getQuery)
    )
    val request = lhttp.Request(requestHeader, JSRequest.asInputStream(in))

    val response: lhttp.Response = router.matchRoute(request) match
      case None               => lhttp.Response.NotFound
      case Some(routeHandler) => routeHandler(request)

    out.setStatus(response.header.status.code)

    val responseHeaders = out.getHeaders
    response.length.foreach(responseHeaders.put(HttpHeader.CONTENT_LENGTH, _))

    response.header.headers.foreach { case (headerName, headerValue) =>
      responseHeaders.put(headerName, headerValue.asJava)
    }

    val os = JSResponse.asBufferedOutputStream(in, out)
    response.body.transferTo(os)
    os.close()
    callback.succeeded()

    true
