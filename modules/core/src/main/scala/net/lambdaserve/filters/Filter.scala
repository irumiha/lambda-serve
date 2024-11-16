package net.lambdaserve.filters

import net.lambdaserve.http.Request

trait Filter:
  // This default will apply the filter to all requests. Override to restrict.
  // First the includePrefixes are checked, then the excludePrefixes.
  val includePrefixes: List[String] = List("")
  val excludePrefixes: List[String] = List.empty

  def handle(request: Request): FilterInResponse
