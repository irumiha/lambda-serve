package net.lambdaserve.filters

import FilterInResponse.Continue
import net.lambdaserve.http.Request

/** Adds a trailing slash if the path does not end in one. Allows for simpler
  * path handling, but it can be confusing so use with care. Put it at the
  * beginning of the filter chain.
  *
  * Don't use together with the RemoveTrailingSlash filter if you don't want to
  * confuse yourself even further.
  */
class AddTrailingSlash extends Filter:
  override val includePrefixes: List[String] = List("")

  override def handle(request: Request): FilterInResponse =
    if request.path.endsWith("/") then Continue(request)
    else Continue(request.copy(path = request.path + "/"))
