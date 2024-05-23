package net.lambdaserve.example

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{
  CodecMakerConfig,
  JsonCodecMaker
}
import net.lambdaserve.core.Router
import net.lambdaserve.core.http.Util.HttpMethod
import net.lambdaserve.core.http.{Request, Response}
import net.lambdaserve.json.jsoniter.JsoniterCodec.given
import net.lambdaserve.mapextract.MapExtract
import net.lambdaserve.requestmapped.*
import net.lambdaserve.server.jetty.Server
import net.lambdaserve.views.scalatags.ScalatagsEncoder.tagEncoder
import scalatags.Text.all.*
import scalatags.Text.tags2

import java.time.LocalDateTime
import java.util.UUID
import scala.io.StdIn

class HouseController:
  case class HouseCommand(id: String, name: String) derives MapExtract
  case class HouseGetCommand(id: String) derives MapExtract

  def doWithHouse(houseCommand: HouseCommand): Response =
    Response.Ok(s"House ${houseCommand.name} with id ${houseCommand.id}")

  def getHouse(cmd: HouseGetCommand): Response =
    Response.Ok(s"House with id ${cmd.id}")

  def houseUI(req: Request): Response =
    Response.Ok(
      html(
        head(tags2.title("Just a page!")),
        body(div(h1("The great page title"), p("My little paragraph")))
      )
    )

  val router: Router =
    import HttpMethod.*
    Router.dsl(
      POST -> raw"/".r           -> doWithHouse.mapped,
      GET  -> raw"/(?<id>\w+)".r -> getHouse.mapped,
      GET  -> raw"/house-ui".r   -> houseUI
    )

@main
def main(): Unit =
  case class Message(name: String, currentTime: LocalDateTime)
  object Message:
    given codec: JsonValueCodec[Message] =
      JsonCodecMaker.make(
        CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case)
      )

  case class JsonCommand(id: UUID, name: String):
    assert(name.nonEmpty, "name must not be empty")

  object JsonCommand:
    given codec: JsonValueCodec[JsonCommand] =
      JsonCodecMaker.make(
        CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case)
      )

  val topRouter =
    import HttpMethod.*
    Router.dsl(
      GET -> raw"/hello".r -> { request =>
        val name = request.query().get("name").flatMap(_.headOption)
        Response.Ok(Message(name.getOrElse("Unknown"), LocalDateTime.now()))
      },
      GET -> raw"/something/(?<thisname>\w+)/?".r -> { request =>
        val name = request.pathParams().get("thisname").flatMap(_.headOption)
        Response.Ok(Message(name.getOrElse("Unknown"), LocalDateTime.now()))
      },
      POST -> raw"/requestmapped".r -> { (command: JsonCommand) =>
        Response.Ok(Message(command.name, LocalDateTime.now()))
      }.mapped
    )

  val router =
    Router.combine("" -> topRouter, "/api/houses" -> HouseController().router)

  val s = Server.makeServer("localhost", 8080, router)

  StdIn.readLine("Awaiting exit...")
  s.stop()
