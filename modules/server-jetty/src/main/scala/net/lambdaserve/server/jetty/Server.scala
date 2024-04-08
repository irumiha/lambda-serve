package net.lambdaserve.server.jetty

import net.lambdaserve.core.{Router, Server}
import org.eclipse.jetty.server as jetty
import org.eclipse.jetty.util.thread.QueuedThreadPool

object Server extends Server[jetty.Server]:
  def makeServer(host: String, port: Int, router: Router): jetty.Server =
    val threadPool = QueuedThreadPool()
    threadPool.setName("server")

    val server = jetty.Server(threadPool)
    val connector = jetty.ServerConnector(server)

    connector.setHost(host)
    connector.setPort(port)

    server.addConnector(connector)
    server.setHandler(new HttpHandler(router))
    server.start()

    server
