package net.lambdaserve.filters

import munit.FunSuite
import net.lambdaserve.http.{Request, HttpResponse}

class SecurityHeadersFilterTest extends FunSuite:

  test("SecurityHeadersFilter adds default security headers"):
    val filter = SecurityHeadersFilter()
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("X-Frame-Options"),
              Some(Seq("DENY"))
            )
            assertEquals(
              wrappedResponse.asHttp.headers.get("X-Content-Type-Options"),
              Some(Seq("nosniff"))
            )
            assertEquals(
              wrappedResponse.asHttp.headers.get("X-XSS-Protection"),
              Some(Seq("1; mode=block"))
            )
            assertEquals(
              wrappedResponse.asHttp.headers.get("Referrer-Policy"),
              Some(Seq("strict-origin-when-cross-origin"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("SecurityHeadersFilter with custom X-Frame-Options"):
    val filter = SecurityHeadersFilter(xFrameOptions = "SAMEORIGIN")
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("X-Frame-Options"),
              Some(Seq("SAMEORIGIN"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("SecurityHeadersFilter adds HSTS when configured"):
    val filter = SecurityHeadersFilter(
      strictTransportSecurity =
        Some("max-age=31536000; includeSubDomains; preload")
    )
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("Strict-Transport-Security"),
              Some(Seq("max-age=31536000; includeSubDomains; preload"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("SecurityHeadersFilter adds CSP when configured"):
    val csp = "default-src 'self'; script-src 'self' 'unsafe-inline'"
    val filter = SecurityHeadersFilter(contentSecurityPolicy = Some(csp))
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("Content-Security-Policy"),
              Some(Seq(csp))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("SecurityHeadersFilter does not add HSTS when not configured"):
    val filter = SecurityHeadersFilter(strictTransportSecurity = None)
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assert(
              !wrappedResponse.asHttp.headers.contains("Strict-Transport-Security")
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

end SecurityHeadersFilterTest
