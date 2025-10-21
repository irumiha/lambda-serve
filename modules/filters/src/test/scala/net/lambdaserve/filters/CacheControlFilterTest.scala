package net.lambdaserve.filters

import munit.FunSuite
import net.lambdaserve.http.{Request, HttpResponse}

class CacheControlFilterTest extends FunSuite:

  test("CacheControlFilter adds cache control header"):
    val filter = CacheControlFilter("public, max-age=3600")
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("Cache-Control"),
              Some(Seq("public, max-age=3600"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("CacheControlFilter adds expires header when configured"):
    val expiresValue = "Wed, 21 Oct 2025 07:28:00 GMT"
    val filter =
      CacheControlFilter("public, max-age=3600", expires = Some(expiresValue))
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("Expires"),
              Some(Seq(expiresValue))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("CacheControlFilter adds ETag when configured"):
    val filter = CacheControlFilter(
      "public, max-age=3600",
      eTag = Some(req => s"etag-${req.path.hashCode}")
    )
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            val expectedETag = s"etag-${request.path.hashCode}"
            assertEquals(
              wrappedResponse.asHttp.headers.get("ETag"),
              Some(Seq(expectedETag))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("CacheControlFilter.forStaticAssets creates appropriate filter"):
    val filter = CacheControlFilter.forStaticAssets(maxAgeSeconds = 86400)
    val request = Request.GET("/static/image.png")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("Cache-Control"),
              Some(Seq("public, max-age=86400, immutable"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("CacheControlFilter.noCache creates no-cache filter"):
    val filter = CacheControlFilter.noCache()
    val request = Request.GET("/api/data")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("Cache-Control"),
              Some(Seq("no-cache, no-store, must-revalidate"))
            )
            assertEquals(wrappedResponse.asHttp.headers.get("Expires"), Some(Seq("0")))
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("CacheControlFilter.forPrivateContent creates private cache filter"):
    val filter = CacheControlFilter.forPrivateContent(maxAgeSeconds = 600)
    val request = Request.GET("/user/profile")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(_, responseWrapper) =>
        val mockResponse = HttpResponse.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.asHttp.headers.get("Cache-Control"),
              Some(Seq("private, max-age=600"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("CacheControlFilter respects include and exclude prefixes"):
    val filter = CacheControlFilter(
      "public, max-age=3600",
      includePrefixes = List("/static"),
      excludePrefixes = List("/static/dynamic")
    )

    // This would be tested in FilterEngine context
    assertEquals(filter.includePrefixes, List("/static"))
    assertEquals(filter.excludePrefixes, List("/static/dynamic"))

end CacheControlFilterTest
