package net.lambdaserve.filters

import net.lambdaserve.http.{Request, HttpResponse, Status}

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*

/** Simple in-memory rate limiting filter based on a sliding window algorithm.
  *
  * This is a basic implementation suitable for single-instance deployments. For
  * distributed systems, consider using a distributed cache like Redis.
  *
  * @param maxRequests
  *   Maximum number of requests allowed in the time window
  * @param windowMs
  *   Time window in milliseconds
  * @param keyExtractor
  *   Function to extract the rate limit key from a request (e.g., IP address,
  *   user ID)
  * @param rateLimitedResponse
  *   Response to return when the rate limit is exceeded
  * @param includePrefixes
  *   URL prefixes to apply this filter to (default: all paths)
  * @param excludePrefixes
  *   URL prefixes to exclude from this filter
  */
class RateLimitFilter(
  val maxRequests: Int = 100,
  val windowMs: Long = 60000, // 1 minute
  val keyExtractor: Request => String = defaultKeyExtractor,
  val rateLimitedResponse: HttpResponse = HttpResponse(
    Status.TooManyRequests,
    Map("Content-Type" -> Seq("text/plain")),
    ""
  ),
  override val includePrefixes: List[String] = List(""),
  override val excludePrefixes: List[String] = List.empty
) extends Filter:

  // Map of key -> a list of request timestamps
  private val requestLog =
    new ConcurrentHashMap[String, java.util.List[Long]]()

  private def cleanupOldEntries(key: String, now: Long): Unit =
    val timestamps = requestLog.get(key)
    if timestamps != null then
      val cutoff = now - windowMs
      timestamps.removeIf(_ < cutoff)
      if timestamps.isEmpty then requestLog.remove(key)

  private def isRateLimited(key: String): Boolean =
    val now = System.currentTimeMillis()
    cleanupOldEntries(key, now)

    val timestamps = requestLog.computeIfAbsent(
      key,
      _ => new java.util.concurrent.CopyOnWriteArrayList[Long]()
    )

    val cutoff         = now - windowMs
    val recentRequests = timestamps.asScala.count(_ > cutoff)

    if recentRequests >= maxRequests then true
    else
      timestamps.add(now)
      false

  override def handle(request: Request): FilterInResponse =
    val key = keyExtractor(request)

    if isRateLimited(key) then
      FilterInResponse.Stop(
        rateLimitedResponse
          .addHeader("X-RateLimit-Limit", maxRequests.toString)
          .addHeader("X-RateLimit-Remaining", "0")
          .addHeader("Retry-After", (windowMs / 1000).toString)
      )
    else
      // Continue with the request and add rate limit headers to response
      FilterInResponse.Wrap(
        request,
        {
          case response: HttpResponse =>
            val key        = keyExtractor(request)
            val now        = System.currentTimeMillis()
            val timestamps = requestLog.get(key)
            val remaining =
              if timestamps != null then
                val cutoff      = now - windowMs
                val recentCount = timestamps.asScala.count(_ > cutoff)
                Math.max(0, maxRequests - recentCount)
              else maxRequests

            FilterOutResponse.Continue(
              response
                .addHeader("X-RateLimit-Limit", maxRequests.toString)
                .addHeader("X-RateLimit-Remaining", remaining.toString)
            )
          case anyOtherResponse => FilterOutResponse.Continue(anyOtherResponse)
        }
      )

end RateLimitFilter

private def defaultKeyExtractor(request: Request): String =
  // Try to get client IP from various headers (for proxied requests)
  request.headers
    .get("X-Forwarded-For")
    .headOption
    .orElse(request.headers.get("X-Real-IP").headOption)
    .getOrElse("unknown")

object RateLimitFilter:
  /** Creates a rate limit filter based on an IP address */
  def perIp(maxRequests: Int = 100, windowMs: Long = 60000): RateLimitFilter =
    RateLimitFilter(maxRequests, windowMs, defaultKeyExtractor)

  /** Creates a rate limit filter based on a custom header (e.g., API key) */
  def perHeader(
    headerName: String,
    maxRequests: Int = 100,
    windowMs: Long = 60000
  ): RateLimitFilter =
    RateLimitFilter(
      maxRequests,
      windowMs,
      req =>
        req.headers
          .get(headerName)
          .headOption
          .getOrElse("anonymous")
    )

  /** Creates a rate limit filter based on the user session */
  def perSession(
    maxRequests: Int = 100,
    windowMs: Long = 60000
  ): RateLimitFilter =
    RateLimitFilter(
      maxRequests,
      windowMs,
      req =>
        req.cookies
          .get("session_id")
          .map(_.value)
          .getOrElse("anonymous")
    )
