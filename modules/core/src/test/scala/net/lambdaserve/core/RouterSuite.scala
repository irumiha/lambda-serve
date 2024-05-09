package net.lambdaserve.core

import net.lambdaserve.core.http.*
import net.lambdaserve.core.http.Util.HttpMethod.GET

import java.io.InputStream

class RouterSuite extends munit.FunSuite:
  def defaultHandler(request: Request): Response = Response.Ok("Done")

  private val defaultRequest = Request(
    header = RequestHeader(),
    requestContent = InputStream.nullInputStream()
  )

  test("matchRequest empty regex matches ANYTHING") {
    val route = Route(GET, "".r, defaultHandler)

    val matched = route.matchRequest(defaultRequest)

    assert(matched.isDefined)
  }

  test("matchRequest empty regex with $ does not match /") {
    val route = Route(GET, "$".r, defaultHandler)

    val matched = route.matchRequest(defaultRequest)

    assert(matched.isEmpty)
  }

  test("matchRequest regex with /$ matches /") {
    val route = Route(GET, "/$".r, defaultHandler)

    val matched = route.matchRequest(defaultRequest)

    assert(matched.isDefined)
  }
