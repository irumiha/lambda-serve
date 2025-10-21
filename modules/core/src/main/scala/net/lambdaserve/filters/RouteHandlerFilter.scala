package net.lambdaserve.filters

import FilterInResponse.Stop
import net.lambdaserve.Router
import net.lambdaserve.http.{HttpResponse, Request, Response}

private[lambdaserve] class RouteHandlerFilter(router: Router) extends Filter:
  override def handle(request: Request): FilterInResponse =
    val response: Response = router.matchMethodAndPath(request) match
      case None                    => HttpResponse.NotFound
      case Some(req, routeHandler) => routeHandler(req)

    Stop(response)
