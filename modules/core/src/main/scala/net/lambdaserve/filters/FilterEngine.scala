package net.lambdaserve.filters

import net.lambdaserve.http.{Request, Response}
import org.slf4j.LoggerFactory

final class FilterEngine(filters: IndexedSeq[Filter]):
  private val logger = LoggerFactory.getLogger(classOf[FilterEngine])

  logger.info(s"Filter engine initialized with filters: $filters")

  private[lambdaserve] def processRequest(request: Request): Response =
    val validFilters =
      filters
        .filter(
          _.includePrefixes.exists(prefix => request.path.startsWith(prefix))
        )
        .filterNot(
          _.excludePrefixes.exists(prefix => request.path.startsWith(prefix))
        )

    var progressing               = true
    var currentRequest            = request
    var currentResponse: Response = null

    var wrappers = Vector[Response => FilterOutResponse]()

    var i = 0
    while progressing && i < validFilters.length do
      filters(i).handle(currentRequest) match
        case FilterInResponse.Continue(request) =>
          currentRequest = request
        case FilterInResponse.Stop(response) =>
          progressing = false
          currentResponse = response
        case FilterInResponse.Wrap(request, responseWrapper) =>
          currentRequest = request
          wrappers = responseWrapper +: wrappers
      i += 1

    assert(
      currentResponse != null,
      "Filter pipeline never stopped with a response!"
    )
    i = 0
    progressing = true
    while progressing && i < wrappers.length do
      wrappers(i).apply(currentResponse) match
        case FilterOutResponse.Continue(response) =>
          currentResponse = response
        case FilterOutResponse.Stop(response) =>
          currentResponse = response
          progressing = false
      i += 1

    currentResponse
