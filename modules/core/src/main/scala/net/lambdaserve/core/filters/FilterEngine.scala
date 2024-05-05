package net.lambdaserve.core.filters

import net.lambdaserve.core.http.{Request, Response}

class FilterEngine(filters: IndexedSeq[Filter]):
  def processRequest(request: Request): Response =
    var progressing = true
    var currentRequest = request
    var currentResponse: Response = null

    var wrappers = Vector[Response => WrapperResponse]()

    var i = 0
    while progressing && i < filters.length do
      filters(i).handle(currentRequest) match
        case FilterResponse.Continue(request) =>
          currentRequest = request
        case FilterResponse.Stop(response) =>
          progressing = false
          currentResponse = response
        case FilterResponse.Wrap(request, responseWrapper) =>
          currentRequest = request
          wrappers = responseWrapper +: wrappers
      i += 1

    assert(currentResponse != null, "Filter pipeline never stops with a response!")
    i = 0
    progressing = true
    while progressing && i < wrappers.length do
      wrappers(i).apply(currentResponse) match
        case WrapperResponse.Continue(response) =>
          currentResponse = response
        case WrapperResponse.Stop(response) =>
          currentResponse = response
          progressing = false
      i += 1

    currentResponse
