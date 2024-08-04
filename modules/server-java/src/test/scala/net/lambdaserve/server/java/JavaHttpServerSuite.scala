package net.lambdaserve.server.java

import com.sun.net.httpserver.HttpServer
import munit.FunSuite
import net.lambdaserve.core.filters.{Filter, FilterInResponse}
import net.lambdaserve.core.http.Method.GET
import net.lambdaserve.core.http.Request
import net.lambdaserve.core.http.Response.Ok
import net.lambdaserve.core.{Route, Router}

import java.net.InetSocketAddress

class JavaHttpServerSuite extends FunSuite:

  val testRouter: Router = Router(
    List(Route(GET, "/test".r, req => Ok("test")))
  )
  val testFilter: Filter = (request: Request) =>
    FilterInResponse.Continue(request)

  test("makeServer should create and start the server") {

    val server = JavaHttpServer.makeServer(
      "localhost",
      0,
      testRouter,
      IndexedSeq(testFilter),
      List("static"),
      Some("/static"),
      gzipSupport = true,
      limitRequestSize = 1024 * 1024 * 10,
      limitResponseSize = 1024 * 1024 * 100,
      useVirtualTheads = false
    )

    assert(server.getAddress.getPort > 0)

  }

  test("addToConfiguredServer should configure and start the server") {

    val server =
      JavaHttpServer.addToConfiguredServer(testRouter, IndexedSeq(testFilter)) {
        handler =>
          assert(handler != null)
          val server = HttpServer.create(InetSocketAddress("localhost", 0), 128)
          server.createContext("/", handler)
          server
      }
    assert(server.getAddress.getPort > 0)
  }
