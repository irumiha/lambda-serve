package net.lambdaserve.filters

import net.lambdaserve.http.{HttpResponse, Request}

/** Filter that adds common security headers to HTTP responses.
  *
  * Adds the following security headers by default: - X-Frame-Options: Protects
  * against clickjacking - X-Content-Type-Options: Prevents MIME type sniffing -
  * X-XSS-Protection: Enables browser XSS protection -
  * Strict-Transport-Security: Enforces HTTPS (if enabled) -
  * Content-Security-Policy: Defines content security policy (if provided) -
  * Referrer-Policy: Controls referrer information
  *
  * @param xFrameOptions
  *   X-Frame-Options header value (default: "DENY")
  * @param xContentTypeOptions
  *   X-Content-Type-Options header value (default: "nosniff")
  * @param xXSSProtection
  *   X-XSS-Protection header value (default: "1; mode=block")
  * @param strictTransportSecurity
  *   Strict-Transport-Security header value (optional)
  * @param contentSecurityPolicy
  *   Content-Security-Policy header value (optional)
  * @param referrerPolicy
  *   Referrer-Policy header value (default: "strict-origin-when-cross-origin")
  * @param includePrefixes
  *   URL prefixes to apply this filter to (default: all paths)
  * @param excludePrefixes
  *   URL prefixes to exclude from this filter
  */
class SecurityHeadersFilter(
  val xFrameOptions: String = "DENY",
  val xContentTypeOptions: String = "nosniff",
  val xXSSProtection: String = "1; mode=block",
  val strictTransportSecurity: Option[String] =
    None, // e.g., "max-age=31536000; includeSubDomains"
  val contentSecurityPolicy: Option[String] = None,
  val referrerPolicy: String = "strict-origin-when-cross-origin",
  override val includePrefixes: List[String] = List(""),
  override val excludePrefixes: List[String] = List.empty
) extends Filter:

  override def handle(request: Request): FilterInResponse =
    FilterInResponse.Wrap(
      request,
      {
        case response: HttpResponse =>
          var updatedResponse = response
            .addHeader("X-Frame-Options", xFrameOptions)
            .addHeader("X-Content-Type-Options", xContentTypeOptions)
            .addHeader("X-XSS-Protection", xXSSProtection)
            .addHeader("Referrer-Policy", referrerPolicy)

          strictTransportSecurity.foreach { hsts =>
            updatedResponse =
              updatedResponse.addHeader("Strict-Transport-Security", hsts)
          }

          contentSecurityPolicy.foreach { csp =>
            updatedResponse =
              updatedResponse.addHeader("Content-Security-Policy", csp)
          }

          FilterOutResponse.Continue(updatedResponse)
        case anyOtherResponse => FilterOutResponse.Continue(anyOtherResponse)
      }
    )

end SecurityHeadersFilter
