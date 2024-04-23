package net.lambdaserve.core

import http.Util.HttpMethod
import http.{Request, Response}

import scala.util.matching.Regex
import scala.jdk.CollectionConverters.given

type RouteHandler = Request => Response
case class Route(method: HttpMethod, path: Regex, handler: RouteHandler):
  val pathParamNames: Vector[String] =
    path.pattern.namedGroups().asScala.keys.toVector

  def matchRequest(request: Request): Option[Request] =
    path
      .findPrefixMatchOf(request.path)
      .map: matched =>
        val pathParamValues =
          pathParamNames
            .map(name => name -> IndexedSeq(matched.group(name)))
            .toMap

        if pathParamValues.nonEmpty then
          request
            .copy(header = request.header.copy(pathParams = pathParamValues))
        else request

object Route:
  def GET(path: Regex)(handler: RouteHandler): Route =
    Route(HttpMethod.GET, path, handler)

  def POST(path: Regex)(handler: RouteHandler): Route =
    Route(HttpMethod.POST, path, handler)

  def PUT(path: Regex)(handler: RouteHandler): Route =
    Route(HttpMethod.PUT, path, handler)

  def DELETE(path: Regex)(handler: RouteHandler): Route =
    Route(HttpMethod.DELETE, path, handler)

  def PATCH(path: Regex)(handler: RouteHandler): Route =
    Route(HttpMethod.PATCH, path, handler)

  def HEAD(path: Regex)(handler: RouteHandler): Route =
    Route(HttpMethod.HEAD, path, handler)

  def OPTIONS(path: Regex)(handler: RouteHandler): Route =
    Route(HttpMethod.OPTIONS, path, handler)

  def TRACE(path: Regex)(handler: RouteHandler): Route =
    Route(HttpMethod.TRACE, path, handler)

  def CONNECT(path: Regex)(handler: RouteHandler): Route =
    Route(HttpMethod.CONNECT, path, handler)

class Router(val routes: Route*):

  def matchRoute(request: Request): Option[(Request, RouteHandler)] =
    var found: Option[(Request, RouteHandler)] = None
    var i                                      = 0

    while i < routes.length && found.isEmpty do
      val route = routes(i)
      found =
        if route.method == request.method then
          route.matchRequest(request).map(r => (r, route.handler))
        else None
      i += 1

    found
