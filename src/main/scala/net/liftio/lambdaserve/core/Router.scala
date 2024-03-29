package net.liftio
package lambdaserve.core

import http.Util.HttpMethod
import http.{Request, Response}

type RouteHandler = Request => Response
case class Route(method: HttpMethod, path: String, handler: RouteHandler)

object Route:
  def GET(path: String)(handler: RouteHandler): Route =
    Route(HttpMethod.GET, path, handler)

  def POST(path: String)(handler: RouteHandler): Route =
    Route(HttpMethod.POST, path, handler)

  def PUT(path: String)(handler: RouteHandler): Route =
    Route(HttpMethod.PUT, path, handler)

  def DELETE(path: String)(handler: RouteHandler): Route =
    Route(HttpMethod.DELETE, path, handler)

  def PATCH(path: String)(handler: RouteHandler): Route =
    Route(HttpMethod.PATCH, path, handler)

  def HEAD(path: String)(handler: RouteHandler): Route =
    Route(HttpMethod.HEAD, path, handler)

  def OPTIONS(path: String)(handler: RouteHandler): Route =
    Route(HttpMethod.OPTIONS, path, handler)

  def TRACE(path: String)(handler: RouteHandler): Route =
    Route(HttpMethod.TRACE, path, handler)

  def CONNECT(path: String)(handler: RouteHandler): Route =
    Route(HttpMethod.CONNECT, path, handler)

class Router(val routes: Route*):
  def matchRoute(request: Request): Option[RouteHandler] =
    routes
      .find(route => route.method == request.header.method && route.path == request.header.path)
      .map(_.handler)
