package net.lambdaserve.core.filters
import net.lambdaserve.core.Router
import net.lambdaserve.core.filters.FilterResponse.Stop
import net.lambdaserve.core.http.{Request, Response}

class RouteHandlerFilter(router: Router) extends Filter:
  override def handle(request: Request): FilterResponse =
    val response: Response = router.matchRoute(request) match
      case None => Response.NotFound
      case Some(req, routeHandler) => routeHandler.handle(req)

    Stop(response)
