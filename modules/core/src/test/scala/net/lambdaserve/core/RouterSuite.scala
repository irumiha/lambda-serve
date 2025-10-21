package net.lambdaserve.core

import net.lambdaserve.{Route, Router}
import net.lambdaserve.http.*
import net.lambdaserve.http.Method.{GET, POST}
import net.lambdaserve.http.{Header, Request, HttpResponse}

import java.io.ByteArrayOutputStream

class RouterSuite extends munit.FunSuite:
  def defaultHandler(request: Request): HttpResponse = HttpResponse.Ok("Done")

  test("matchRequest empty regex does not match /") {
    val route = Route(GET, "".r, defaultHandler)

    val matched = route.matchRequest(Request.GET("/"))

    assert(matched.isEmpty)
  }

  test("matchRequest regex with /$ matches /") {
    val route = Route(GET, "/".r, defaultHandler)

    val matched = route.matchRequest(Request.GET("/"))

    assert(matched.isDefined)
  }

  test("router combine with prefix") {
    val route1 = Route(GET, "/".r, defaultHandler)
    val route2 = Route(GET, "/".r, defaultHandler)
    val route3 = Route(GET, "/".r, defaultHandler)

    val router1 = Router(Seq(route1, route2))
    val router2 = Router(Seq(route3))

    val combinedRouter = Router.combine("/api" -> router1, "/api/v1" -> router2)

    val request1 = Request.GET("/api/")
    val request2 = Request.GET("/api/v1/")

    val matched1 = combinedRouter.matchMethodAndPath(request1)
    val matched2 = combinedRouter.matchMethodAndPath(request2)

    assert(matched1.isDefined)
    assert(matched2.isDefined)
  }

  test("route with path params") {
    val route = Route(
      GET,
      raw"/(?<paramname>.*?)/?".r,
      { request =>
        val paramValue =
          request.pathParams.get("paramname", "")

        HttpResponse.Ok(paramValue)
      }
    )

    val router  = Router(Seq(route))
    val request = Request.GET("/something")
    val matched = router.matchMethodAndPath(request)

    assert(matched.isDefined)
    val baos                      = ByteArrayOutputStream()
    val (matchedRequest, handler) = matched.get
    handler(matchedRequest).asHttp.bodyWriter(baos)
    val out = baos.toString()
    assert(out == "something")
  }

  test("route with path params and query params") {
    val route = Route(
      GET,
      "/(?<paramname>\\w+)/?".r,
      { request =>
        val paramValue =
          request.pathParams.get("paramname", "")
        val queryValue = request.query.get("queryname", "")

        HttpResponse.Ok(paramValue + queryValue)
      }
    )

    val router  = Router(Seq(route))
    val request = Request.GET("/something").withQueryParam("queryname", "123")
    val matched = router.matchMethodAndPath(request)

    assert(matched.isDefined)
    val baos                      = ByteArrayOutputStream()
    val (matchedRequest, handler) = matched.get
    handler(matchedRequest).asHttp.bodyWriter(baos)
    val out = baos.toString()
    assert(out == "something123")
  }

  test("route with path params and query params and body") {
    val route = Route(
      POST,
      "/(?<paramname>\\w+)/?".r,
      { request =>
        val paramValue = request.pathParams.get("paramname", "")
        val queryValue = request.query.get("queryname", "")
        val username   = request.form.get("username", "")
        val password   = request.form.get("password", "")
        HttpResponse.Ok(paramValue + queryValue + username + password)
      }
    )

    val router = Router(Seq(route))
    val request =
      Request
        .POST("", "/something")
        .withHeader(
          Header.ContentType.name,
          "application/x-www-form-urlencoded"
        )
        .withQueryParam("queryname", "123")
        .withFormParam("username", "user")
        .withFormParam("password", "123")

    val matched = router.matchMethodAndPath(request)

    assert(matched.isDefined)
    val baos                      = ByteArrayOutputStream()
    val (matchedRequest, handler) = matched.get
    handler(matchedRequest).asHttp.bodyWriter(baos)
    val out = baos.toString()
    assertEquals(out, "something123user123")
  }
