
# Lambdaserve - A minimal web server platform for Scala on Jetty

Lambdaserve is a minimalistic web server platform for Scala on Jetty. It is designed to be simple
and easy to use, and to provide a solid foundation for building small to medium web applications
in Scala. 

## Similarities

Scalatra, Django, Spring Boot (the web parts) provided some inspiration for the approach and features.

Scalatra for the route definitions.
Django for the regex-based path definitions.
Spring boot for the easy extraction of data from the requests.


## Features

### Simple route definitions

Routes are defined using a simple DSL that is similar to Scalatra's route definitions.

```scala
import net.lambdaserve.core.Router
import net.lambdaserve.core.http.*
import net.lambdaserve.core.http.Util.HttpMethod
import net.lambdaserve.server.jetty.Server
import org.eclipse.jetty.server.{Server, ServerConnector}
import scala.io.StdIn

import HttpMethod.*

val routes = List(
  GET -> "/hello".r -> { req =>
    Response.Ok("Hello, world!")
  },
)

val router = Router.make(routes)

val jetty = Server.makeServer(
  "localhost",
  0,
  router,
  staticPaths = List("classpath:static-files"),
  staticPrefix = Some("/static")
)

val openPorts = jetty.getConnectors
  .collect { case s: ServerConnector => s.getLocalPort }
  .mkString(",")

StdIn.readLine(s"Listening on port(s) $openPorts, awaiting exit...")
jetty.stop()

```

### Easy extraction of data from requests

### Easy to use and extend

### Minimal dependencies

With everything included the dependencies are:

 - Jetty
 - jsoniter for JSON (de)serialization
 - slf4j and I recommend Scribe for logging
 - Tyrian or Scalatags

This brings the example application to a total size of 12MB.

### No reflection

We use Magnolia macros for some typeclases. 
