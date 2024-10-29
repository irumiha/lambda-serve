package net.lambdaserve.example

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import net.lambdaserve.core.Router
import net.lambdaserve.core.http.*
import net.lambdaserve.core.http.Method
import net.lambdaserve.json.jsoniter.JsoniterCodec.given
import net.lambdaserve.mapextract.{MapExtract, SourceName}
import net.lambdaserve.requestmapped.mapped
import net.lambdaserve.server.jetty
import net.lambdaserve.views.scalatags.ScalatagsEncoder.given
import org.eclipse.jetty.server.ServerConnector
import scalatags.Text.all.*
import scalatags.Text.tags2.title

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.UUID
import scala.io.StdIn

class HouseController:
  case class HouseCommand(
    id: String,
    name: String,
    @SourceName("User-Agent") userAgent: String
  ) derives MapExtract
  case class HouseGetCommand(id: String) derives MapExtract

  def doWithHouse(houseCommand: HouseCommand): Response =
    Response.Ok(
      s"House ${houseCommand.name} with id ${houseCommand.id}. The header was ${houseCommand.userAgent}"
    )

  def getHouse(cmd: HouseGetCommand): Response =
    Response.Ok(s"House with id ${cmd.id}")

  def houseUI(req: Request): Response =
    Response
      .Ok(
        html(
          head(title("Just a page!")),
          body(div(h1("The great page title"), p("My little paragraph")))
        )
      )
      .withCookie(
        Cookie("mycookie", "myvalue", maxAge = Some(7200), httpOnly = true)
      )
      .addHeader("Server", "Lambdaserve")

  val router: Router =
    import Method.*
    Router.make(
      POST -> "/".r            -> doWithHouse.mapped,
      GET  -> "/(?<id>\\w+)".r -> getHouse.mapped,
      GET  -> "/house-ui".r    -> houseUI
    )
end HouseController

@main def main(): Unit =
  case class Message(name: String, currentTime: LocalDateTime)
  object Message:
    given codec: JsonValueCodec[Message] =
      JsonCodecMaker.makeCirceLikeSnakeCased

  case class JsonCommand(id: UUID, name: String):
    assert(name.nonEmpty, "name must not be empty")

  object JsonCommand:
    given codec: JsonValueCodec[JsonCommand] =
      JsonCodecMaker.makeCirceLikeSnakeCased

  val topRouter =
    import Method.*
    Router.make(
      GET -> "/hello".r -> { request =>
        val name = request.query.get("name").flatMap(_.headOption)
        Response.Ok(Message(name.getOrElse("Unknown"), LocalDateTime.now()))
      },
      GET -> "/something/(?<thisname>\\w+)/?".r -> { request =>
        val name = request.pathParams.get("thisname").flatMap(_.headOption)
        Response.Ok(Message(name.getOrElse("Unknown"), LocalDateTime.now()))
      },
      POST -> "/requestmapped".r -> { (command: JsonCommand) =>
        Response.Ok(Message(command.name, LocalDateTime.now()))
      }.mapped
    )

  val router =
    Router.combine("" -> topRouter, "/api/houses" -> HouseController().router)

  val s = jetty.JettyServer.makeServer(
    "localhost",
    8080,
    router,
    staticPaths = List("classpath:static-files"),
    staticPrefix = Some("/static")
  )

  println(s"Listening on port ${s.getConnectors
      .collect { case s: ServerConnector => s.getLocalPort }
      .mkString(",")}")
  s.join()
