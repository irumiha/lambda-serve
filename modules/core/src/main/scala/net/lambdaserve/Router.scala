package net.lambdaserve

import net.lambdaserve.http.{Method, Request}

import scala.util.matching.Regex

final case class Router(routes: Seq[Route]):

  def matchMethodAndPath(request: Request): Option[(Request, RouteHandler)] =
    var found: Option[(Request, RouteHandler)] = None
    var i                                      = 0

    while i < routes.length && found.isEmpty do
      val route = routes(i)
      found =
        if route.method == request.method then
          route.matchRequest(request).map(request => (request, route.handler))
        else None
      i += 1

    found
    
  def findRoutesForPath(path: String): Seq[Route] =
    routes.filter(_.path.pattern.matcher(path).matches())

object Router:

  def make(routes: ((Method, Regex), RouteHandler)*): Router =
    Router(routes.map{
      case ((m, r), rh) => Route(m,r,rh)
    })

  def combine(routerMounts: (String, Router)*): Router =
    Router(routes = routerMounts.flatMap { (prefix, router) =>
      router.routes.map { route =>
        route.copy(path = s"$prefix${route.path}".r)
      }
    })
