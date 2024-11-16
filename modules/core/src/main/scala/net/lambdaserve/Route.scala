package net.lambdaserve

import net.lambdaserve.http.{Method, Request}

import scala.jdk.CollectionConverters.given
import scala.util.matching.Regex

case class Route(method: Method, path: Regex, handler: RouteHandler):
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
        Some(request.copy(pathParams = pathParamValues))
      else Some(request)
    else None
