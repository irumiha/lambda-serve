package net.lambdaserve.core.filters
import net.lambdaserve.core.Router
import net.lambdaserve.core.filters.FilterInResponse.Stop
import net.lambdaserve.core.http.{Request, Response}

private [lambdaserve] class RouteHandlerFilter(router: Router) extends Filter:
  override def handle(request: Request): FilterInResponse =
    val response: Response = router.matchMethodAndPath(request) match
      case None => Response.NotFound
      case Some(req, routeHandler) => routeHandler.handle(req)

    Stop(response)
