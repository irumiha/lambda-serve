package net.lambdaserve.example

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import net.lambdaserve.core.Router
import net.lambdaserve.core.http.Response
import net.lambdaserve.json.jsoniter.JsoniterCodec.given
import net.lambdaserve.mapextract.MapExtract
import net.lambdaserve.requestmapped.*
import net.lambdaserve.requestmapped.AutoMappedRoute
import net.lambdaserve.core.Route
import net.lambdaserve.server.jetty.Server

import java.time.LocalDateTime
import java.util.UUID
import scala.io.StdIn

class HouseController:
  import AutoMappedRoute.*
  case class HouseCommand(id: String, name: String) derives MapExtract
  case class HouseGetCommand(id: String) derives MapExtract

  def doWithHouse(houseCommand: HouseCommand): Response =
    Response.Ok(s"House ${houseCommand.name} with id ${houseCommand.id}")

  def getHouse(cmd: HouseGetCommand): Response =
    Response.Ok(s"House with id ${cmd.id}")

  val router: Router =
    Router(
      Seq(
        POST("/".r)(doWithHouse),
        GET("/(?<id>\\w+)".r)(getHouse)
      )
    )

@main
def main(): Unit =
  case class Message(name: String, currentTime: LocalDateTime)
  object Message:
    given codec: JsonValueCodec[Message] =
      JsonCodecMaker.make(
        CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case)
      )

  case class DemoCommand(id: UUID, name: String) derives MapExtract

  case class JsonCommand(id: UUID, name: String):
    assert(name.nonEmpty, "name must not be empty")

  object JsonCommand:
    given codec: JsonValueCodec[JsonCommand] =
      JsonCodecMaker.make(
        CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case)
      )
  import Route.*
  val topRouter = Router(
    Seq(
      GET("/hello".r): request =>
        val name = request.query.get("name").flatMap(_.headOption)
        Response.Ok(Message(name.getOrElse("Unknown"), LocalDateTime.now()))
      ,
      GET("/something/(?<thisname>\\w+)/?".r): request =>
        val name = request.pathParams.get("thisname").flatMap(_.headOption)
        Response.Ok(Message(name.getOrElse("Unknown"), LocalDateTime.now()))
      ,
      POST("/requestmapped".r):
        mapped: (command: JsonCommand) =>
          Response.Ok(Message(command.name, LocalDateTime.now()))
    )
  )

  val router =
    Router.combine("" -> topRouter, "/api/houses" -> HouseController().router)

  val s = Server.makeServer("localhost", 8080, router)

  StdIn.readLine("Awaiting exit...")
  s.stop()
