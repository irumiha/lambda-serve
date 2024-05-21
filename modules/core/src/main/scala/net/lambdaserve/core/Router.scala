package net.lambdaserve.core

import net.lambdaserve.core.http.Request
import net.lambdaserve.core.http.Util.HttpMethod

import scala.annotation.targetName
import scala.jdk.CollectionConverters.given
import scala.util.matching.Regex

case class Route(method: HttpMethod, path: Regex, handler: RouteHandler):
  private val pathParamNames: Vector[String] =
    path.pattern.namedGroups().asScala.keys.toVector

  def matchRequest(request: Request): Option[Request] =
    val pathMatch = path.pattern.matcher(request.path)

    if pathMatch.matches() then
      val pathParamValues =
        pathParamNames
          .map(name => name -> IndexedSeq(pathMatch.group(name)))
          .toMap
      if pathParamValues.nonEmpty then
        Some(request.copy(header =
          request.header.copy(pathParams = () => pathParamValues)
        ))
      else Some(request)
    else None

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

case class Router(routes: Seq[Route]):

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

object Router:

  @targetName("combineWithPrefix")
  def combine(routerMounts: (String, Router)*): Router =
    Router(routes = routerMounts.flatMap { (prefix, router) =>
      router.routes.map { route =>
        route.copy(path = s"$prefix${route.path}".r)
      }
    })
