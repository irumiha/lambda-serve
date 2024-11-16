package net.lambdaserve.filters

import FilterInResponse.Continue
import net.lambdaserve.http.Request

/** Removes a trailing slash if the path ends in one and is not the root path.
  * Allows for simpler path handling, but it can be confusing so use with care.
  * Put it at the beginning of the filter chain. This does the opposite of the
  * AddTrailingSlash filter.
  *
  * Don't use both at the same time.
  */
class RemoveTrailingSlash extends Filter:
  override val includePrefixes: List[String] = List("")

  override def handle(request: Request): FilterInResponse =
    if request.path.endsWith("/") && request.path.length > 1 then
      Continue(request.copy(path = request.path.dropRight(1)))
    else Continue(request)
