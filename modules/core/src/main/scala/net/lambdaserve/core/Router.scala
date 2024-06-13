package net.lambdaserve.core

import net.lambdaserve.core.http.Request
import net.lambdaserve.core.http.Util.HttpMethod

import scala.util.matching.Regex

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

  def make(routes: ((HttpMethod, Regex), RouteHandler)*): Router =
    Router(routes.map{
      case ((m, r), rh) => Route(m,r,rh)
    })

  def combine(routerMounts: (String, Router)*): Router =
    Router(routes = routerMounts.flatMap { (prefix, router) =>
      router.routes.map { route =>
        route.copy(path = s"$prefix${route.path}".r)
      }
    })
