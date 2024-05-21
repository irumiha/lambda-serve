package net.lambdaserve.core

import net.lambdaserve.core.http.Request
import net.lambdaserve.core.http.Util.HttpMethod

import scala.util.matching.Regex
import scala.jdk.CollectionConverters.given

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
        Some(
          request.copy(header =
            request.header.copy(pathParams = () => pathParamValues)
          )
        )
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
