package net.lambdaserve.filters

import net.lambdaserve.http.Request
import org.slf4j.LoggerFactory

/** Filter that logs HTTP requests for debugging and monitoring purposes.
  *
  * @param logHeaders
  *   Whether to log request headers
  * @param logQueryParams
  *   Whether to log query parameters
  * @param includePrefixes
  *   URL prefixes to apply this filter to (default: all paths)
  * @param excludePrefixes
  *   URL prefixes to exclude from this filter
  */
class RequestLoggingFilter(
  val logHeaders: Boolean = false,
  val logQueryParams: Boolean = true,
  override val includePrefixes: List[String] = List(""),
  override val excludePrefixes: List[String] = List.empty
) extends Filter:

  private val logger = LoggerFactory.getLogger(classOf[RequestLoggingFilter])

  override def handle(request: Request): FilterInResponse =
    val method = request.method
    val path = request.path
    val queryString =
      if logQueryParams && !request.query.isEmpty then
        val params = request.query
          .map { case (k, v) => s"$k=${v.mkString(",")}" }
          .mkString("&")
        s"?$params"
      else ""

    val baseLog = s"$method $path$queryString"

    if logHeaders && !request.headers.isEmpty then
      val headersStr = request.headers
        .map { case (k, v) => s"  $k: ${v.mkString(", ")}" }
        .mkString("\n")
      logger.info(s"$baseLog\nHeaders:\n$headersStr")
    else logger.info(baseLog)

    FilterInResponse.Wrap(
      request,
      response =>
        logger.info(s"$method $path -> ${response.status.code}")
        FilterOutResponse.Continue(response)
    )

end RequestLoggingFilter
