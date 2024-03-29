package net.liftio
package lambdaserve.core

import org.eclipse.jetty.server.{ServerConnector, Server as JServer}
import org.eclipse.jetty.util.thread.QueuedThreadPool

object Server:
  def makeServer(host: String, port: Int, router: Router): JServer =
    val threadPool = QueuedThreadPool()
    threadPool.setName("server")

    val server = JServer(threadPool)
    val connector = ServerConnector(server)
    connector.setHost(host)
    connector.setPort(port)
    server.addConnector(connector)
    server.setHandler(new HttpHandler(router))
    server.start()
    server
