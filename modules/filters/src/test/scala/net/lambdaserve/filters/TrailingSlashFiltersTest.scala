package net.lambdaserve.filters

import munit.FunSuite
import net.lambdaserve.http.Request

class TrailingSlashFiltersTest extends FunSuite:

  test("AddTrailingSlash adds slash to path without trailing slash"):
    val filter = AddTrailingSlash()
    val request = Request.GET("/api/users")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Continue(modifiedRequest) =>
        assertEquals(modifiedRequest.path, "/api/users/")
      case _ => fail("Expected Continue response")

  test("AddTrailingSlash does not modify path that already has trailing slash"):
    val filter = AddTrailingSlash()
    val request = Request.GET("/api/users/")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Continue(modifiedRequest) =>
        assertEquals(modifiedRequest.path, "/api/users/")
      case _ => fail("Expected Continue response")

  test("AddTrailingSlash handles root path"):
    val filter = AddTrailingSlash()
    val request = Request.GET("/")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Continue(modifiedRequest) =>
        assertEquals(modifiedRequest.path, "/")
      case _ => fail("Expected Continue response")

  test("AddTrailingSlash handles empty path"):
    val filter = AddTrailingSlash()
    val request = Request.GET("")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Continue(modifiedRequest) =>
        assertEquals(modifiedRequest.path, "/")
      case _ => fail("Expected Continue response")

  test("RemoveTrailingSlash removes slash from path with trailing slash"):
    val filter = RemoveTrailingSlash()
    val request = Request.GET("/api/users/")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Continue(modifiedRequest) =>
        assertEquals(modifiedRequest.path, "/api/users")
      case _ => fail("Expected Continue response")

  test("RemoveTrailingSlash does not modify path without trailing slash"):
    val filter = RemoveTrailingSlash()
    val request = Request.GET("/api/users")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Continue(modifiedRequest) =>
        assertEquals(modifiedRequest.path, "/api/users")
      case _ => fail("Expected Continue response")

  test("RemoveTrailingSlash preserves root path"):
    val filter = RemoveTrailingSlash()
    val request = Request.GET("/")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Continue(modifiedRequest) =>
        assertEquals(modifiedRequest.path, "/")
      case _ => fail("Expected Continue response")

  test("RemoveTrailingSlash handles paths with multiple segments"):
    val filter = RemoveTrailingSlash()
    val request = Request.GET("/api/v1/users/123/")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Continue(modifiedRequest) =>
        assertEquals(modifiedRequest.path, "/api/v1/users/123")
      case _ => fail("Expected Continue response")

  test("AddTrailingSlash and RemoveTrailingSlash are inverses"):
    val addFilter = AddTrailingSlash()
    val removeFilter = RemoveTrailingSlash()
    val originalPath = "/api/users"
    val request = Request.GET(originalPath)

    // Apply AddTrailingSlash
    val afterAdd = addFilter.handle(request) match
      case FilterInResponse.Continue(req) => req
      case _                              => fail("Expected Continue")

    assertEquals(afterAdd.path, "/api/users/")

    // Apply RemoveTrailingSlash
    val afterRemove = removeFilter.handle(afterAdd) match
      case FilterInResponse.Continue(req) => req
      case _                              => fail("Expected Continue")

    assertEquals(afterRemove.path, originalPath)

end TrailingSlashFiltersTest
