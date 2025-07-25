package net.lambdaserve

import net.lambdaserve.filters.Filter

trait Server[S, H]:
  /** Creates a new server. Accepts some typically useful options. The returned
    * server will be already started.
    *
    * @param host
    *   The hostname to listen on.
    * @param port
    *   The port to listen on. A value of 0 means a random port will be
    *   selected.
    * @param router
    *   The router for all application routes.
    * @param filters
    *   All application filters.
    * @param staticPaths
    *   List of directories on the filesystem or the classpath where static
    *   assets can be found. Prefix the classpath resources with `classpath:`
    * @param staticPrefix
    *   The URL prefix to serve the static assets from.
    * @param gzipSupport
    *   Whether to turn on support for decompressing the compressed request and
    *   response bodies.
    * @param limitResponseSize
    *   The uncompressed response size limit.
    * @param limitRequestSize
    *   The uncompressed request size limit. -1 for unlimited.
    * @param useVirtualThreads
    *   Run handler functions on virtual threads. -1 for unlimited.
    * @return
    */
  def makeServer(
    host: String,
    port: Int,
    router: Router,
    filters: IndexedSeq[Filter],
    staticPaths: List[String],
    staticPrefix: Option[String],
    gzipSupport: Boolean,
    limitRequestSize: Long,
    limitResponseSize: Long,
    useVirtualThreads: Boolean
  ): S

  /** Add a handler to a server that has already been configured.
    *
    * This method will use the provided router and a list of filters to build an
    * HTTP handler for the underlying server. The serverConfigurer function
    * implementation must configure the server and add the handler to it.
    *
    * This allows for maximum configurability without duplicating the server
    * configuration.
    *
    * @param router
    *   The router for all application routes.
    * @param filters
    *   All application filters.
    * @param serverConfigurer
    *   The server configuration function.
    * @return
    */
  def addToConfiguredServer(router: Router, filters: IndexedSeq[Filter])(
    serverConfigurer: H => S
  ): S
