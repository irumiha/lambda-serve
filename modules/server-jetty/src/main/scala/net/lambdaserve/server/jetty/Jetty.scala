package net.lambdaserve.server.jetty

import net.lambdaserve.core.filters.{Filter, FilterEngine, RouteHandlerFilter}
import net.lambdaserve.core.{Router, Server}
import org.eclipse.jetty.server as jetty
import org.eclipse.jetty.server.SizeLimitHandler
import org.eclipse.jetty.server.handler.{ContextHandler, ContextHandlerCollection, ResourceHandler}
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.util.resource.ResourceFactory
import org.eclipse.jetty.util.thread.QueuedThreadPool

import java.util.concurrent.Executors

object Jetty extends Server[jetty.Server, jetty.Handler]:
  def makeServer(
    host: String,
    port: Int,
    router: Router,
    filters: IndexedSeq[Filter] = IndexedSeq(),
    staticPaths: List[String] = List.empty,
    staticPrefix: Option[String] = None,
    gzipSupport: Boolean = false,
    // limit to 10MB uncompressed request size by default. Put in -1 for unlimited
    limitRequestSize: Long = 1024 * 1024 * 10,
    // limit to 100MB uncompressed response size by default. Put in -1 for unlimited
    limitResponsetSize: Long = 1024 * 1024 * 100,
    useVirtualThreads: Boolean = false
  ): jetty.Server =
    val threadPool = QueuedThreadPool()
    threadPool.setName("server")

    if useVirtualThreads then
      threadPool.setVirtualThreadsExecutor(
        Executors.newVirtualThreadPerTaskExecutor()
      )

    val server    = jetty.Server(threadPool)
    val connector = jetty.ServerConnector(server)

    connector.setHost(host)
    connector.setPort(port)

    server.addConnector(connector)

    val lambdaHandler = HttpHandler(
      FilterEngine(filters :+ RouteHandlerFilter(router))
    )

    val requestProcessingHandler =
      if staticPaths.nonEmpty && staticPrefix.nonEmpty then
        val resourceHandler = ResourceHandler()
        val resourcesFromPaths = staticPaths.map { sp =>
          if sp.startsWith("classpath:") then
            ResourceFactory
              .of(resourceHandler)
              .newClassLoaderResource(sp.replaceAll("classpath:", ""))
          else ResourceFactory.of(resourceHandler).newResource(sp)
        }
        val resources = ResourceFactory.combine(resourcesFromPaths*)
        resourceHandler.setBaseResource(resources)
        val contextHandlerCollection = ContextHandlerCollection()

        contextHandlerCollection.addHandler(new ContextHandler(lambdaHandler, "/"))
        contextHandlerCollection.addHandler(new ContextHandler(resourceHandler, staticPrefix.get))

        contextHandlerCollection
      else
        lambdaHandler

    val requestSizeLimitHandler =
      SizeLimitHandler(limitRequestSize, limitResponsetSize)

    val finalHandler = if gzipSupport then
      requestSizeLimitHandler.setHandler(requestProcessingHandler)

      val gzipHandler = GzipHandler(requestSizeLimitHandler)
      gzipHandler.setMinGzipSize(1024)
      gzipHandler.addIncludedMethods("POST")
      gzipHandler
    else
      requestSizeLimitHandler.setHandler(requestProcessingHandler)
      requestSizeLimitHandler


    server.setHandler(finalHandler)
    server.start()

    server

  override def addToConfiguredServer(
    router: Router,
    filters: IndexedSeq[Filter]
  )(serverConfigurer: jetty.Handler => jetty.Server): jetty.Server =
    val lambdaHandler = HttpHandler(
      FilterEngine(filters :+ RouteHandlerFilter(router))
    )
    val jettyServer = serverConfigurer(lambdaHandler)

    jettyServer.start()

    jettyServer