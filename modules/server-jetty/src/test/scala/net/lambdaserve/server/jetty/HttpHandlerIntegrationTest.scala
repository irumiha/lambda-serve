package net.lambdaserve.server.jetty

import munit.FunSuite
import net.lambdaserve.Router
import net.lambdaserve.http.{Method, Request, Response}

import java.net.http.HttpRequest.BodyPublishers
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.{ServerSocket, URI}
import java.nio.charset.StandardCharsets
import scala.util.Using

class HttpHandlerIntegrationTest extends FunSuite:

  private var server: Option[org.eclipse.jetty.server.Server] = None
  private var port: Int                                       = _
  private val httpClient = HttpClient.newHttpClient()

  // Test state to capture what the handler received
  private val receivedRequests = scala.collection.mutable.ListBuffer[Request]()
  private val receivedBytes = scala.collection.mutable.ListBuffer[Array[Byte]]()

  private def findAvailablePort(): Int =
    Using(new ServerSocket(0))(_.getLocalPort).get

  private def testHandler(request: Request): Response =
    receivedRequests += request
    receivedBytes += request.requestContent.readAllBytes()

    val contentSummary =
      if request.multipartForm.nonEmpty then
        s"multipart: ${request.multipartForm.size} parts"
      else if request.form.nonEmpty then s"form: ${request.form.size} fields"
      else s"raw bytes"

    Response.Ok(s"Received $contentSummary")

  override def beforeEach(context: BeforeEach): Unit =
    receivedRequests.clear()
    receivedBytes.clear()
    port = findAvailablePort()

    val router = Router.make(Method.POST -> "/test".r -> testHandler)

    server = Some(
      JettyServer
        .makeServer("localhost", port, router)
    )
    server.get.start()

  override def afterEach(context: AfterEach): Unit =
    server.foreach(_.stop())
    server = None

  test("HttpHandler processes multipart/form-data correctly"):
    // Arrange
    val boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
    val multipartBody =
      s"""------WebKitFormBoundary7MA4YWxkTrZu0gW
         |Content-Disposition: form-data; name="username"
         |
         |testuser
         |------WebKitFormBoundary7MA4YWxkTrZu0gW
         |Content-Disposition: form-data; name="file"; filename="test.txt"
         |Content-Type: text/plain
         |
         |Hello World from file
         |
         |------WebKitFormBoundary7MA4YWxkTrZu0gW--
         |""".stripMargin

    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(s"http://localhost:$port/test"))
      .header("Content-Type", s"multipart/form-data; boundary=$boundary")
      .POST(BodyPublishers.ofString(multipartBody))
      .build()

    // Act
    val response =
      httpClient.send(request, HttpResponse.BodyHandlers.ofString())

    // Assert
    assertEquals(response.statusCode(), 200)
    assert(response.body().contains("multipart: 2 parts"))

    assertEquals(receivedRequests.size, 1)
    val receivedRequest = receivedRequests.head
    assertEquals(receivedRequest.multipartForm.size, 2)

    val usernamePart =
      receivedRequest.multipartForm.find(_.name.contains("username"))
    assert(usernamePart.isDefined)

    val filePart = receivedRequest.multipartForm.find(_.name.contains("file"))
    assert(filePart.isDefined)
    assertEquals(filePart.get.fileName, Some("test.txt"))
    assertEquals(
      new String(filePart.get.content.readAllBytes()),
      "Hello World from file\n"
    )

  test("HttpHandler processes application/x-www-form-urlencoded correctly"):
    // Arrange
    val formData =
      "username=testuser&email=test%40example.com&age=25&tags=scala&tags=web"

    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(s"http://localhost:$port/test"))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(BodyPublishers.ofString(formData))
      .build()

    // Act
    val response =
      httpClient.send(request, HttpResponse.BodyHandlers.ofString())

    // Assert
    assertEquals(response.statusCode(), 200)
    assert(response.body().contains("form:"))

    assertEquals(receivedRequests.size, 1)
    val receivedRequest = receivedRequests.head
    assert(receivedRequest.form.nonEmpty)

    assertEquals(
      receivedRequest.form.get("username"),
      Some(IndexedSeq("testuser"))
    )

    assertEquals(
      receivedRequest.form.get("email"),
      Some(IndexedSeq("test@example.com"))
    )
    assertEquals(receivedRequest.form.get("age"), Some(IndexedSeq("25")))
    assertEquals(
      receivedRequest.form.get("tags"),
      Some(IndexedSeq("scala", "web"))
    )

    // Should not have multipart or raw content
    assert(receivedRequest.multipartForm.isEmpty)

  test("HttpHandler processes JSON/raw content correctly"):
    // Arrange
    val jsonData =
      """{"name": "John Doe", "age": 30, "email": "john@example.com"}"""

    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(s"http://localhost:$port/test"))
      .header("Content-Type", "application/json")
      .POST(BodyPublishers.ofString(jsonData))
      .build()

    // Act
    val response =
      httpClient.send(request, HttpResponse.BodyHandlers.ofString())

    // Assert
    assertEquals(response.statusCode(), 200)
    assert(response.body().contains("raw"))

    assertEquals(receivedRequests.size, 1)
    val receivedRequest = receivedRequests.head

    // Read the content to verify it's the JSON we sent
    assert(receivedBytes.nonEmpty)
    val contentString = new String(receivedBytes.head, StandardCharsets.UTF_8)
    assertEquals(contentString, jsonData)

    // Should not have form or multipart data
    assert(receivedRequest.form.isEmpty)
    assert(receivedRequest.multipartForm.isEmpty)

  test("HttpHandler processes text/plain content as raw"):
    // Arrange
    val textData =
      "This is plain text content for testing raw input processing."

    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(s"http://localhost:$port/test"))
      .header("Content-Type", "text/plain")
      .POST(BodyPublishers.ofString(textData))
      .build()

    // Act
    val response =
      httpClient.send(request, HttpResponse.BodyHandlers.ofString())

    // Assert
    assertEquals(response.statusCode(), 200)
    assert(response.body().contains("raw"))

    assertEquals(receivedRequests.size, 1)
    val receivedRequest = receivedRequests.head

    assert(receivedBytes.nonEmpty)
    val contentString = new String(receivedBytes.head, StandardCharsets.UTF_8)
    assertEquals(contentString, textData)

    assert(receivedRequest.form.isEmpty)
    assert(receivedRequest.multipartForm.isEmpty)

  test("HttpHandler processes request with no Content-Type as raw"):
    // Arrange
    val bodyData = "Some content without an explicit content type"

    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(s"http://localhost:$port/test"))
      .POST(BodyPublishers.ofString(bodyData))
      .build()

    // Act
    val response =
      httpClient.send(request, HttpResponse.BodyHandlers.ofString())

    // Assert
    assertEquals(response.statusCode(), 200)
    assert(response.body().contains("raw"))

    assertEquals(receivedRequests.size, 1)
    val receivedRequest = receivedRequests.head

    assert(receivedBytes.nonEmpty)
    val contentString = new String(receivedBytes.head, StandardCharsets.UTF_8)
    assertEquals(contentString, bodyData)

    assert(receivedRequest.form.isEmpty)
    assert(receivedRequest.multipartForm.isEmpty)
