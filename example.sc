//> using dep "net.lambdaserve::lambdaserve-all:0.1.0-SNAPSHOT"

import net.lambdaserve.core.*
import net.lambdaserve.core.http.*
import net.lambdaserve.server.jetty.JettyServer

JettyServer.makeServer(
  host="localhost",
  port=8080,
  router = Router.make(
    Method.GET -> "/hello".r -> { request =>
      val name = request.query.get("name").flatMap(_.headOption)

      Response.Ok(name.getOrElse("Unknown"))
    }
  )
).join()