package net.lambdaserve.filters

import munit.FunSuite
import net.lambdaserve.http.{Request, Status}

class PredicateRedirectFilterTest extends FunSuite:

  test("PredicateRedirectFilter redirects when predicate is true"):
    val filter =
      PredicateRedirectFilter(req => req.path.startsWith("/old"), "/new")
    val request = Request.GET("/old/page")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.SeeOther)
        assertEquals(response.asHttp.headers.get("Location"), Some(Seq("/new")))
      case _ => fail("Expected Stop response with redirect")

  test("PredicateRedirectFilter continues when predicate is false"):
    val filter =
      PredicateRedirectFilter(req => req.path.startsWith("/old"), "/new")
    val request = Request.GET("/api/page")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Continue(req) =>
        assertEquals(req.path, "/api/page")
      case _ => fail("Expected Continue response")

  test("PredicateRedirectFilter with query parameter check"):
    val filter =
      PredicateRedirectFilter(req => req.query.contains("admin"), "/login")
    val requestWithParam = Request.GET("/page").withQueryParam("admin", "true")
    val requestWithoutParam = Request.GET("/page")

    val resultWithParam    = filter.handle(requestWithParam)
    val resultWithoutParam = filter.handle(requestWithoutParam)

    resultWithParam match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.SeeOther)
      case _ => fail("Expected redirect for request with query param")

    resultWithoutParam match
      case FilterInResponse.Continue(_) => // Expected
      case _ => fail("Expected continue for request without query param")

  test("PredicateRedirectFilter with header check"):
    val filter = PredicateRedirectFilter(
      req => req.headers.get("X-Custom-Header").isEmpty,
      "/error"
    )
    val requestWithHeader = Request
      .GET("/api/test")
      .withHeader("X-Custom-Header", "value")
    val requestWithoutHeader = Request.GET("/api/test")

    val resultWithHeader    = filter.handle(requestWithHeader)
    val resultWithoutHeader = filter.handle(requestWithoutHeader)

    resultWithHeader match
      case FilterInResponse.Continue(_) => // Expected
      case _ => fail("Expected continue for request with header")

    resultWithoutHeader match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.SeeOther)
        assertEquals(
          response.asHttp.headers.get("Location"),
          Some(Seq("/error"))
        )
      case _ => fail("Expected redirect for request without header")

  test("PredicateRedirectFilter can check method"):
    val filter = PredicateRedirectFilter(
      req => req.method.toString == "POST",
      "/method-not-allowed"
    )
    val postRequest = Request.POST("test", "/api/resource")
    val getRequest  = Request.GET("/api/resource")

    val postResult = filter.handle(postRequest)
    val getResult  = filter.handle(getRequest)

    postResult match
      case FilterInResponse.Stop(response) =>
        assertEquals(response.asHttp.status, Status.SeeOther)
      case _ => fail("Expected redirect for POST request")

    getResult match
      case FilterInResponse.Continue(_) => // Expected
      case _ => fail("Expected continue for GET request")

  test("PredicateRedirectFilter with custom redirect path"):
    val filter =
      PredicateRedirectFilter(_ => true, "/custom/redirect/path")
    val request = Request.GET("/any/path")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Stop(response) =>
        assertEquals(
          response.asHttp.headers.get("Location"),
          Some(Seq("/custom/redirect/path"))
        )
      case _ => fail("Expected redirect")

end PredicateRedirectFilterTest
