package net.lambdaserve.server.java

import com.sun.net.httpserver
import net.lambdaserve.core.filters.{Filter, FilterEngine, RouteHandlerFilter}
import net.lambdaserve.core.{Router, Server}

import java.net.InetSocketAddress
import java.util.concurrent.Executors

object JavaHttpServer extends Server[httpserver.HttpServer, httpserver.HttpHandler]:
  override def makeServer(
    host: String,
    port: Int,
    router: Router,
    filters: IndexedSeq[Filter] = IndexedSeq(),
    staticPaths: List[String] = List.empty,
    staticPrefix: Option[String] = None,
    gzipSupport: Boolean = false,
    limitRequestSize: Long = 1024 * 1024 * 10,
    limitResponseSize: Long = 1024 * 1024 * 100,
    useVirtualTheads: Boolean = false,
  ): httpserver.HttpServer =
    val server = httpserver.HttpServer.create(InetSocketAddress(host, port), 128)

    if useVirtualTheads then
      server.setExecutor(Executors.newVirtualThreadPerTaskExecutor())
    else
      server.setExecutor(Executors.newWorkStealingPool())

    val lambdaHandler = HttpHandler(
      FilterEngine(filters :+ RouteHandlerFilter(router))
    )

    server.createContext("/", lambdaHandler)

    server.start()

    server

  override def addToConfiguredServer(
    router: Router,
    filters: IndexedSeq[Filter]
  )(serverConfigurer: httpserver.HttpHandler => httpserver.HttpServer): httpserver.HttpServer =
    val lambdaHandler = HttpHandler(
      FilterEngine(filters :+ RouteHandlerFilter(router))
      )
    val javaServer = serverConfigurer(lambdaHandler)

    javaServer.start()

    javaServer
