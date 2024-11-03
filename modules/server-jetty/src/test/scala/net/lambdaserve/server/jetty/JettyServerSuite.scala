package net.lambdaserve.server.jetty

import munit.FunSuite
import net.lambdaserve.core.filters.{Filter, FilterInResponse}
import net.lambdaserve.core.http.Method.GET
import net.lambdaserve.core.http.Request
import net.lambdaserve.core.http.Response.Ok
import net.lambdaserve.core.{Route, Router}
import org.eclipse.jetty.server.{Server, ServerConnector}

class JettyServerSuite extends FunSuite:

  val testRouter: Router = Router(
    List(Route(GET, "/test".r, req => Ok("test")))
    )
  val testFilter: Filter = (request: Request) =>
    FilterInResponse.Continue(request)

  test("makeServer should create and start the server") {
    val server = JettyServer.makeServer(
      "localhost",
      0,
      testRouter,
      IndexedSeq(testFilter),
      List.empty,
      None,
      gzipSupport = true,
      limitRequestSize = 1024 * 1024 * 10,
      limitResponseSize = 1024 * 1024 * 100,
      useVirtualThreads = false
      )

    assert(server.getConnectors.collect { case s: ServerConnector => s.getLocalPort }.head > 0)
    server.stop()
  }

  test("addToConfiguredServer should configure and start the server") {
    val server = JettyServer.addToConfiguredServer(testRouter, IndexedSeq(testFilter)) { handler =>
      assert(handler != null)
      val server = Server()
      val connector = ServerConnector(server)

      connector.setHost("localhost")
      connector.setPort(0)

      server.addConnector(connector)
      server.setHandler(handler)
      server
    }

    assert(server.getConnectors.collect { case s: ServerConnector => s.getLocalPort }.head > 0)
    server.stop()
  }
