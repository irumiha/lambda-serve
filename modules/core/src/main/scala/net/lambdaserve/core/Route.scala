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
