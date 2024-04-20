package net.lambdaserve.example

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import net.lambdaserve.core.Route.GET
import net.lambdaserve.core.Router
import net.lambdaserve.json.jsoniter.JsonResponse.Ok
import net.lambdaserve.server.jetty.Server

import java.time.LocalDateTime
import scala.io.StdIn

@main
def main(): Unit =
  case class Message(name: String, currentTime: LocalDateTime)
  object Message:
    given codec: JsonValueCodec[Message] =
      JsonCodecMaker.make(
        CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case)
      )

  val router = Router(
    GET("/hello".r) { request =>
      val name = request.query.get("name").flatMap(_.headOption)

      Ok(Message(name.getOrElse("Unknown"), LocalDateTime.now()))
    },

    GET("/something/(?<thisname>\\w+)".r) { request =>
      val name = request.pathParams.get("thisname").flatMap(_.headOption)

      Ok(Message(name.getOrElse("Unknown"), LocalDateTime.now()))
    }
  )

  val s = Server.makeServer("localhost", 8080, router)

  StdIn.readLine("Awaiting exit...")
  s.stop()
