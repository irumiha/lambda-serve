//> using scala "3.6.3"
//> using jvm graalvm-oracle:23
//> using dep "net.lambdaserve::lambdaserve-all:0.1.0-SNAPSHOT"
//> using dep "com.outr::scribe-slf4j2:3.16.0"

// Create a native-image binary with:
// scala-cli --power package --native-image --graalvm-java-version 23 --graalvm-version 23 example.scala -o example-server

// This runs the app with the native-image-agent to generate the metadata before generating the native image binary
//> using javaOpt -agentlib:native-image-agent=config-merge-dir=resources/META-INF/native-image
//> using resourceDir ./resources

// GraalVM options needed by netty
//> using packaging.graalvmArgs --no-fallback
//> using packaging.graalvmArgs --enable-http
//> using packaging.graalvmArgs --enable-url-protocols=http,https
//> using packaging.graalvmArgs --install-exit-handlers
//> using packaging.graalvmArgs -Djdk.http.auth.tunneling.disabledSchemes=


import net.lambdaserve.*
import net.lambdaserve.http.*
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
