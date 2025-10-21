package net.lambdaserve.filters

import net.lambdaserve.http.{Request, HttpResponse}

class PredicateRedirectFilter(p: Request => Boolean, redirectPath: String)
    extends Filter:
  override def handle(request: Request): FilterInResponse =
    if p(request) then FilterInResponse.Stop(HttpResponse.SeeOther(redirectPath))
    else FilterInResponse.Continue(request)
