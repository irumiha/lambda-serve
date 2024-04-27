package net.lambdaserve.server.jetty

import net.lambdaserve.core.Router
import net.lambdaserve.core.http.{Request, RequestHeader, Response}
import org.eclipse.jetty.http.HttpHeader
import org.eclipse.jetty.server as jetty
import org.eclipse.jetty.util.Callback

import scala.jdk.CollectionConverters.*

class HttpHandler(router: Router) extends jetty.Handler.Abstract():

  override def handle(
    in: jetty.Request,
    out: jetty.Response,
    callback: Callback
  ): Boolean =
    val requestHeader = RequestHeaderExtractor(
      scheme = in.getHttpURI.getScheme,
      method = in.getMethod,
      path = in.getHttpURI.getPath,
      headers = in.getHeaders,
      queryString = Option(in.getHttpURI.getQuery)
    )
    val request = Request(requestHeader, jetty.Request.asInputStream(in))

    val response: Response = router.matchRoute(request) match
      case None                    => Response.NotFound
      case Some(req, routeHandler) => routeHandler.handle(req)

    out.setStatus(response.header.status.code)

    val responseHeaders = out.getHeaders
    response.length.foreach(responseHeaders.put(HttpHeader.CONTENT_LENGTH, _))

    response.header.headers.foreach { case (headerName, headerValue) =>
      responseHeaders.put(headerName, headerValue.asJava)
    }

    val os = jetty.Response.asBufferedOutputStream(in, out)
    response.bodyWriter(os)
    os.close()
    callback.succeeded()

    true
