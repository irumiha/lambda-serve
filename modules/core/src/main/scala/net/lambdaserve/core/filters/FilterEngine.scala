package net.lambdaserve.core.filters

import net.lambdaserve.core.http.{Request, Response}

class FilterEngine(filters: IndexedSeq[Filter]):
  def processRequest(request: Request): Response =
    ???
