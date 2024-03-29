package net.liftio
package lambdaserve

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.{Router, Server}
import core.Route.GET
import core.http.Response.*

import java.time.LocalDateTime
import scala.io.StdIn

@main
def main(): Unit =
  case class Message(name: String, currentTime: LocalDateTime)
  object Message:
    given codec: JsonValueCodec[Message] = JsonCodecMaker.make

  val router = Router(GET("/hello") { request =>
    val name = request.query.get("name").flatMap(_.headOption)

    OkJson(Message(name.getOrElse("Unknown"), LocalDateTime.now()))
  })

  val s = Server.makeServer("localhost", 8080, router)

  StdIn.readLine("Awaiting exit...")
  s.stop()
