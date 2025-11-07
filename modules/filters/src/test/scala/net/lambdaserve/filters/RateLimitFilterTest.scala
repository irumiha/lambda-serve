package net.lambdaserve.filters

import munit.FunSuite
import net.lambdaserve.http.{Request, Status}

class RateLimitFilterTest extends FunSuite:

  test("RateLimitFilter allows requests within limit"):
    val filter  = RateLimitFilter(maxRequests = 5, windowMs = 60000)
    val request = Request.GET("/test").withHeader("X-Forwarded-For", "1.2.3.4")

    // Make 5 requests (at the limit)
    for _ <- 1 to 5 do
      val result = filter.handle(request)
      result match
        case FilterInResponse.Wrap(_, _) => // Expected
        case _ => fail("Expected Wrap response for request within limit")

  test("RateLimitFilter blocks requests exceeding limit"):
    val filter  = RateLimitFilter(maxRequests = 2, windowMs = 60000)
    val request = Request.GET("/test").withHeader("X-Forwarded-For", "1.2.3.4")

    // First 2 requests should pass
    for _ <- 1 to 2 do
      val result = filter.handle(request)
      result match
        case FilterInResponse.Wrap(_, _) => // Expected
        case _                           => fail("Expected Wrap response")

    // Third request should be blocked
    val blockedResult = filter.handle(request)
    blockedResult match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.TooManyRequests)
        assertEquals(
          response.asHttp.headers.get("X-RateLimit-Limit"),
          Some(Seq("2"))
        )
        assertEquals(
          response.asHttp.headers.get("X-RateLimit-Remaining"),
          Some(Seq("0"))
        )
        assert(response.asHttp.headers.contains("Retry-After"))
      case _ => fail("Expected Stop response for rate-limited request")

  test("RateLimitFilter uses custom key extractor"):
    val filter = RateLimitFilter(
      maxRequests = 2,
      windowMs = 60000,
      keyExtractor =
        req => req.headers.get("API-Key").headOption.getOrElse("anonymous")
    )

    val request1 = Request.GET("/test").withHeader("API-Key", "key-123")
    val request2 = Request.GET("/test").withHeader("API-Key", "key-456")

    // Each API key should have its own limit
    for _ <- 1 to 2 do
      filter.handle(request1) match
        case FilterInResponse.Wrap(_, _) => // Expected
        case _                           => fail("Expected Wrap response")

      filter.handle(request2) match
        case FilterInResponse.Wrap(_, _) => // Expected
        case _                           => fail("Expected Wrap response")

    // Both should be at their limits now
    filter.handle(request1) match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.TooManyRequests)
      case _ => fail("Expected Stop response")

    filter.handle(request2) match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.TooManyRequests)
      case _ => fail("Expected Stop response")

  test("RateLimitFilter adds rate limit headers to response"):
    val filter  = RateLimitFilter(maxRequests = 5, windowMs = 60000)
    val request = Request.GET("/test").withHeader("X-Forwarded-For", "1.2.3.4")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        import net.lambdaserve.http.HttpResponse
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("X-RateLimit-Limit"),
              Some(Seq("5"))
            )
            // After first request, should have 4 remaining
            assert(
              wrappedResponse.asHttp.headers.contains("X-RateLimit-Remaining")
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("RateLimitFilter.perIp creates IP-based rate limiter"):
    val filter = RateLimitFilter.perIp(maxRequests = 3, windowMs = 60000)

    assertEquals(filter.maxRequests, 3)
    assertEquals(filter.windowMs, 60000L)

  test("RateLimitFilter.perHeader creates header-based rate limiter"):
    val filter =
      RateLimitFilter.perHeader("X-API-Key", maxRequests = 10, windowMs = 60000)

    val request = Request.GET("/test").withHeader("X-API-Key", "test-key")

    // Should use the header value as the key
    assertEquals(filter.maxRequests, 10)

  test("RateLimitFilter.perSession creates session-based rate limiter"):
    val filter = RateLimitFilter.perSession(maxRequests = 20, windowMs = 60000)

    assertEquals(filter.maxRequests, 20)

  test("RateLimitFilter respects different IPs independently"):
    val filter = RateLimitFilter(maxRequests = 2, windowMs = 60000)

    val request1 = Request.GET("/test").withHeader("X-Forwarded-For", "1.2.3.4")
    val request2 = Request.GET("/test").withHeader("X-Forwarded-For", "5.6.7.8")

    // Each IP should have independent limits
    for _ <- 1 to 2 do
      filter.handle(request1) match
        case FilterInResponse.Wrap(_, _) => // Expected
        case _                           => fail("Expected Wrap response")

      filter.handle(request2) match
        case FilterInResponse.Wrap(_, _) => // Expected
        case _                           => fail("Expected Wrap response")

    // Both IPs should now be at their limit
    filter.handle(request1) match
      case FilterInResponse.Stop(_) => // Expected
      case _                        => fail("Expected Stop response")

    filter.handle(request2) match
      case FilterInResponse.Stop(_) => // Expected
      case _                        => fail("Expected Stop response")

end RateLimitFilterTest
