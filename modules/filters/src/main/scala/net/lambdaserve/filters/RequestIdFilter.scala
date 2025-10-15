package net.lambdaserve.filters

import net.lambdaserve.http.Request

import java.util.UUID

/** Filter that adds a unique request ID to each request for distributed tracing
  * and log correlation.
  *
  * The request ID is added to the request headers and also added to the
  * response headers. This allows tracking requests through the entire
  * application stack.
  *
  * @param requestIdHeader
  *   Name of the header to use for the request ID (default: "X-Request-ID")
  * @param generateId
  *   Function to generate a unique ID (default: UUID)
  * @param includePrefixes
  *   URL prefixes to apply this filter to (default: all paths)
  * @param excludePrefixes
  *   URL prefixes to exclude from this filter
  */
class RequestIdFilter(
  val requestIdHeader: String = "X-Request-ID",
  val generateId: () => String = () => UUID.randomUUID().toString,
  override val includePrefixes: List[String] = List(""),
  override val excludePrefixes: List[String] = List.empty
) extends Filter:

  override def handle(request: Request): FilterInResponse =
    // Check if request already has an ID (e.g., from a load balancer)
    val requestId = request.headers
      .get(requestIdHeader)
      .flatMap(_.headOption)
      .getOrElse(generateId())

    // Add request ID to request if it wasn't already present
    val updatedRequest =
      if request.headers.contains(requestIdHeader) then request
      else request.withHeader(requestIdHeader, requestId)

    // Also add request ID to response
    FilterInResponse.Wrap(
      updatedRequest,
      response =>
        FilterOutResponse.Continue(
          response.addHeader(requestIdHeader, requestId)
        )
    )

end RequestIdFilter
