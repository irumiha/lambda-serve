package net.liftio
package lambdaserve

import core.{Router, Server}
import core.Route.GET
import core.http.Response.*

import scala.io.StdIn

@main
def main(): Unit =
  val router = Router(GET("/hello") { request =>
    val name = request.query.get("name").flatMap(_.headOption)

    //Ok(s"Hello friend ${name.getOrElse("Anonymous")}")
    Redirect("/someother")
  })

  val s = Server.makeServer("localhost", 8080, router)

  StdIn.readLine("Awaiting exit...")
  s.stop()
