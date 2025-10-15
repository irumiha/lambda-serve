package net.lambdaserve.filters

import net.lambdaserve.http.Request
import org.slf4j.LoggerFactory

/** Filter that measures request processing time and optionally adds timing
  * information to response headers.
  *
  * Useful for monitoring and debugging performance issues.
  *
  * @param addHeaderToResponse
  *   Whether to add the timing information to the response header (default:
  *   true)
  * @param timingHeader
  *   Name of the header to use for timing info (default: "X-Response-Time")
  * @param logTiming
  *   Whether to log timing information (default: true)
  * @param logSlowRequests
  *   Threshold in milliseconds above which to log slow requests (optional)
  * @param includePrefixes
  *   URL prefixes to apply this filter to (default: all paths)
  * @param excludePrefixes
  *   URL prefixes to exclude from this filter
  */
class TimingFilter(
  val addHeaderToResponse: Boolean = true,
  val timingHeader: String = "X-Response-Time",
  val logTiming: Boolean = true,
  val logSlowRequests: Option[Long] = Some(1000), // Log requests taking > 1 second
  override val includePrefixes: List[String] = List(""),
  override val excludePrefixes: List[String] = List.empty
) extends Filter:

  private val logger = LoggerFactory.getLogger(classOf[TimingFilter])

  override def handle(request: Request): FilterInResponse =
    val startTime = System.nanoTime()

    FilterInResponse.Wrap(
      request,
      response =>
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000.0

        if logTiming then
          val method = request.method
          val path = request.path
          logger.debug(f"$method $path completed in $durationMs%.2f ms")

          logSlowRequests.foreach { threshold =>
            if durationMs > threshold then
              logger.warn(
                f"Slow request detected: $method $path took $durationMs%.2f ms (threshold: $threshold ms)"
              )
          }

        val updatedResponse =
          if addHeaderToResponse then
            response.addHeader(timingHeader, f"$durationMs%.2f ms")
          else response

        FilterOutResponse.Continue(updatedResponse)
    )

end TimingFilter
