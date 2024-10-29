//> using jvm graalvm-java21:21.0.2
//> using dep "net.lambdaserve::lambdaserve-all:0.1.0-SNAPSHOT"

// Create a native-image binary with:
//  scala-cli --power package --native-image --graalvm-java-version 21 --graalvm-version 21.0.2 example.sc -o example-server

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