//> using scala "3.7.1"
//> using jvm graalvm-oracle:24
//> using dep "net.lambdaserve::lambdaserve-all:0.1.0-SNAPSHOT"
//> using dep "com.github.plokhotnyuk.jsoniter-scala::jsoniter-scala-macros:2.37.0"
//> using dep "com.outr::scribe-slf4j2:3.17.0"

// This runs the app with the native-image-agent to generate the metadata before generating the native image binary
//> using javaOpt -agentlib:native-image-agent=config-merge-dir=resources/META-INF/native-image
//> using resourceDir ./resources

// After you've run the app one with `scala-cli run .`, create a native-image binary with:
// scala-cli --power package --native-image --graalvm-java-version 24 --graalvm-version 24 --graalvm-jvm-id graalvm-oracle:24 example.scala -o example-server

// GraalVM options needed by jetty
//> using packaging.graalvmArgs --no-fallback
//> using packaging.graalvmArgs --enable-http
//> using packaging.graalvmArgs --enable-url-protocols=http,https
//> using packaging.graalvmArgs --install-exit-handlers
//> using packaging.graalvmArgs -Djdk.http.auth.tunneling.disabledSchemes=

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import scalatags.Text.all.*
import scalatags.Text.tags2.title
import net.lambdaserve.*
import net.lambdaserve.http.*
import net.lambdaserve.server.jetty.JettyServer
import net.lambdaserve.json.jsoniter.JsoniterCodec.given
import net.lambdaserve.views.scalatags.ScalatagsEncoder.given
import net.lambdaserve.mapextract.{MapExtract, SourceName}
import net.lambdaserve.requestmapped.mapped

import java.time.LocalDateTime
import java.util.UUID

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
      POST -> "/".r            -> mapped(doWithHouse),
      GET  -> "/(?<id>\\w+)".r -> mapped(getHouse),
      GET  -> "/house-ui".r    -> houseUI
    )
end HouseController

class TopController:
  case class Message(name: String, currentTime: LocalDateTime)

  object Message:
    given codec: JsonValueCodec[Message] =
      JsonCodecMaker.makeCirceLikeSnakeCased

  case class JsonCommand(id: UUID, name: String):
    assert(name.nonEmpty, "name must not be empty")

  object JsonCommand:
    given codec: JsonValueCodec[JsonCommand] =
      JsonCodecMaker.makeCirceLikeSnakeCased

  val topRouter: Router =
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
      POST -> "/requestmapped".r -> mapped: (command: JsonCommand) =>
        Response.Ok(Message(command.name, LocalDateTime.now()))
    )

@main def server(): Unit = JettyServer
  .makeServer(
    host = "localhost",
    port = 8080,
    useVirtualThreads = true,
    router = Router.combine(
      "/api/houses" -> HouseController().router,
      "/top"        -> TopController().topRouter,
      "/" -> Router.make(Method.GET -> "/hello".r -> { request =>
        val name = request.query.get("name").flatMap(_.headOption)

        Response.Ok(name.getOrElse("Unknown"))
      })
    )
  )
  .join()
