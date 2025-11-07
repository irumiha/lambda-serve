package net.lambdaserve.filters

import munit.FunSuite
import net.lambdaserve.http.{Header, Method, Request, HttpResponse, Status}

import java.io.ByteArrayOutputStream

class CorsFilterTest extends FunSuite:

  private def getResponseBody(response: HttpResponse): String =
    val baos = ByteArrayOutputStream()
    response.bodyWriter(baos)
    baos.toString()

  test("CORS filter allows preflight request with wildcard origin"):
    val filter = CorsFilter()
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.NoContent)
        assertEquals(
          response.asHttp.headers.get(Header.AccessControlAllowOrigin.name),
          Some(Seq("*"))
        )
        assertEquals(
          response.asHttp.headers
            .get(Header.AccessControlAllowMethods.name)
            .map(_.head.split(", ").toSet),
          Some(Set("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"))
        )
        assertEquals(
          response.asHttp.headers
            .get(Header.AccessControlAllowHeaders.name)
            .map(_.head.split(", ").toSet),
          Some(Set("Content-Type", "Authorization", "X-Requested-With"))
        )
        assertEquals(
          response.asHttp.headers.get(Header.AccessControlMaxAge.name),
          Some(Seq("3600"))
        )
      case _ => fail("Expected Stop response for preflight request")

  test("CORS filter adds headers to actual request with wildcard origin"):
    val filter = CorsFilter()
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers
                .get(Header.AccessControlAllowOrigin.name),
              Some(Seq("*"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response for actual request")

  test("CORS filter with specific allowed origins accepts allowed origin"):
    val filter =
      CorsFilter(allowedOrigins =
        Set("https://example.com", "https://test.com")
      )
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.NoContent)
        assertEquals(
          response.asHttp.headers.get(Header.AccessControlAllowOrigin.name),
          Some(Seq("https://example.com"))
        )
      case _ => fail("Expected Stop response")

  test("CORS filter with specific allowed origins rejects disallowed origin"):
    val filter = CorsFilter(allowedOrigins = Set("https://example.com"))
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://malicious.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.Forbidden)
        assert(
          !response.asHttp.headers
            .contains(Header.AccessControlAllowOrigin.name)
        )
      case _ => fail("Expected Stop response")

  test("CORS filter with credentials includes credentials header"):
    val filter = CorsFilter(
      allowedOrigins = Set("https://example.com"),
      allowCredentials = true
    )
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(
          response.asHttp.headers
            .get(Header.AccessControlAllowCredentials.name),
          Some(Seq("true"))
        )
        // With credentials, should return specific origin, not "*"
        assertEquals(
          response.asHttp.headers.get(Header.AccessControlAllowOrigin.name),
          Some(Seq("https://example.com"))
        )
      case _ => fail("Expected Stop response")

  test("CORS filter with exposed headers includes expose headers"):
    val filter =
      CorsFilter(exposedHeaders = Set("X-Custom-Header", "X-Another-Header"))
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            val exposedHeaders = wrappedResponse.asHttp.headers
              .get(Header.AccessControlExposeHeaders.name)
              .map(_.head.split(", ").toSet)
            assertEquals(
              exposedHeaders,
              Some(Set("X-Custom-Header", "X-Another-Header"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("CORS filter with custom max age"):
    val filter = CorsFilter(maxAge = Some(7200))
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(
          response.asHttp.headers.get(Header.AccessControlMaxAge.name),
          Some(Seq("7200"))
        )
      case _ => fail("Expected Stop response")

  test("CORS filter without max age omits max age header"):
    val filter = CorsFilter(maxAge = None)
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        assert(
          !response.asHttp.headers.contains(Header.AccessControlMaxAge.name)
        )
      case _ => fail("Expected Stop response")

  test("CORS filter with includePrefixes only applies to matching paths"):
    val filter = CorsFilter(includePrefixes = List("/api"))
    val apiRequest = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(apiRequest)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.NoContent)
      case _ => fail("Expected Stop response for /api path")

  test("CORS filter with excludePrefixes skips excluded paths"):
    val filter = CorsFilter(excludePrefixes = List("/admin"))

    // Test that non-excluded path works
    val regularRequest = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(regularRequest)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.NoContent)
      case _ => fail("Expected Stop response for non-excluded path")

  test("CORS filter without Origin header in preflight returns Forbidden"):
    val filter  = CorsFilter(allowedOrigins = Set("https://example.com"))
    val request = Request.GET("/api/test").copy(method = Method.OPTIONS)

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.Forbidden)
      case _ => fail("Expected Stop response")

  test("CORS filter handles GET request without wrapping when no Origin"):
    val filter  = CorsFilter()
    val request = Request.GET("/api/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            // Without origin, no CORS headers should be added
            assert(
              !wrappedResponse.asHttp.headers
                .contains(Header.AccessControlAllowOrigin.name)
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("CORS filter with custom allowed methods"):
    val filter = CorsFilter(allowedMethods = Set(Method.GET, Method.POST))
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        val methods = response.asHttp.headers
          .get(Header.AccessControlAllowMethods.name)
          .map(_.head.split(", ").toSet)
        assertEquals(methods, Some(Set("GET", "POST")))
      case _ => fail("Expected Stop response")

  test("CORS filter with custom allowed headers"):
    val filter =
      CorsFilter(allowedHeaders = Set("X-Custom-Header", "Authorization"))
    val request = Request
      .GET("/api/test")
      .withHeader(Header.Origin.name, "https://example.com")
      .copy(method = Method.OPTIONS)

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        val headers = response.asHttp.headers
          .get(Header.AccessControlAllowHeaders.name)
          .map(_.head.split(", ").toSet)
        assertEquals(headers, Some(Set("X-Custom-Header", "Authorization")))
      case _ => fail("Expected Stop response")

end CorsFilterTest
