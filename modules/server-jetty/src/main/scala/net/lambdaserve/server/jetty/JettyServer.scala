package net.lambdaserve.server.jetty

import net.lambdaserve.filters.{Filter, FilterEngine, RouteHandlerFilter}
import net.lambdaserve.{Router, Server}
import org.eclipse.jetty.server as jetty
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.server.handler.{
  ContextHandler,
  ContextHandlerCollection,
  ResourceHandler,
  SizeLimitHandler
}
import org.eclipse.jetty.util.resource.ResourceFactory
import org.eclipse.jetty.util.thread.QueuedThreadPool

import java.util.concurrent.Executors

object JettyServer extends Server[jetty.Server, jetty.Handler]:
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
    limitResponseSize: Long = 1024 * 1024 * 100,
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

    // Build the handler chain step by step
    val baseHandler = createBaseHandler(lambdaHandler, staticPaths, staticPrefix)
    val sizeLimitedHandler = applySizeLimits(baseHandler, limitRequestSize, limitResponseSize)
    val finalHandler = applyGzipIfNeeded(sizeLimitedHandler, gzipSupport)

    server.setHandler(finalHandler)
    server.start()

    server

  private def createBaseHandler(
    lambdaHandler: jetty.Handler,
    staticPaths: List[String],
    staticPrefix: Option[String]
  ): jetty.Handler =
    if staticPaths.nonEmpty && staticPrefix.nonEmpty then
      createStaticContentHandler(lambdaHandler, staticPaths, staticPrefix.get)
    else
      lambdaHandler

  private def createStaticContentHandler(
    lambdaHandler: jetty.Handler,
    staticPaths: List[String],
    staticPrefix: String
  ): ContextHandlerCollection =
    val resourceHandler = ResourceHandler()
    val resourcesFromPaths = staticPaths.map { sp =>
      if sp.startsWith("classpath:") then
        ResourceFactory
          .of(resourceHandler)
          .newClassLoaderResource(sp.replaceAll("classpath:", ""))
      else ResourceFactory.of(resourceHandler).newResource(sp)
    }

    staticPaths.zip(resourcesFromPaths).foreach { case (sp, rp) =>
      if rp == null then
        throw new RuntimeException(s"Could not find static path: $sp")
    }

    val resources = ResourceFactory.combine(resourcesFromPaths.toArray*)
    resourceHandler.setBaseResource(resources)

    val contextHandlerCollection = ContextHandlerCollection()
    contextHandlerCollection.addHandler(new ContextHandler(lambdaHandler, "/"))
    contextHandlerCollection.addHandler(new ContextHandler(resourceHandler, staticPrefix))

    contextHandlerCollection

  private def applySizeLimits(
    handler: jetty.Handler,
    limitRequestSize: Long,
    limitResponseSize: Long
  ): SizeLimitHandler =
    val sizeLimitHandler = SizeLimitHandler(limitRequestSize, limitResponseSize)
    sizeLimitHandler.setHandler(handler)
    sizeLimitHandler

  private def applyGzipIfNeeded(
    handler: jetty.Handler,
    gzipSupport: Boolean
  ): jetty.Handler =
    if gzipSupport then
      val gzipHandler = GzipHandler(handler)
      gzipHandler.setMinGzipSize(1024)
      gzipHandler.setInflateBufferSize(2048)
      gzipHandler.addIncludedMethods("GET", "POST")
      gzipHandler
    else
      handler

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
