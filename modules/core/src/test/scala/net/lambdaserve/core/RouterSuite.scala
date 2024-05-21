package net.lambdaserve.core

import net.lambdaserve.core.http.*
import net.lambdaserve.core.http.Util.HttpMethod.GET

class RouterSuite extends munit.FunSuite:
  def defaultHandler(request: Request): Response = Response.Ok("Done")

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

    val matched1 = combinedRouter.matchRoute(request1)
    val matched2 = combinedRouter.matchRoute(request2)

    assert(matched1.isDefined)
    assert(matched2.isDefined)
  }
