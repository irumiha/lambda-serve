package net.lambdaserve.http

import munit.FunSuite
import net.lambdaserve.codec.EntityEncoder

import java.io.{ByteArrayOutputStream, OutputStream}
import java.time.Instant

class ResponseTest extends FunSuite:

  given EntityEncoder[String] with
    def bodyWriter(entity: String): OutputStream => Unit = os =>
      os.write(entity.getBytes)
    def contentTypeHeader: String = "text/plain"

  private def getResponseBody(response: Response): String =
    val baos = ByteArrayOutputStream()
    response.bodyWriter(baos)
    baos.toString()

  test("Response.Ok creates 200 response with entity"):
    val response = Response.Ok("Hello, World!")

    assertEquals(response.status, Status.OK)
    assertEquals(getResponseBody(response), "Hello, World!")
    assertEquals(
      response.headers.get(Header.ContentType.name),
      Some(Seq("text/plain"))
    )

  test("Response.Ok with headers"):
    val response = Response.Ok(
      "Hello",
      Map("X-Custom-Header" -> Seq("custom-value"))
    )

    assertEquals(response.status, Status.OK)
    assertEquals(response.headers.get("X-Custom-Header"), Some(Seq("custom-value")))

  test("Response.NotFound creates 404 response"):
    val response = Response.NotFound

    assertEquals(response.status, Status.NotFound)

  test("Response.BadRequest creates 400 response"):
    val response = Response.BadRequest

    assertEquals(response.status, Status.BadRequest)

  test("Response.BadRequest with entity"):
    val response = Response.BadRequest("Invalid input")

    assertEquals(response.status, Status.BadRequest)
    assertEquals(getResponseBody(response), "Invalid input")

  test("Response.Found creates 302 redirect"):
    val response = Response.Found("/new-location")

    assertEquals(response.status, Status.Found)
    assertEquals(response.headers.get(Header.Location.name), Some(Seq("/new-location")))

  test("Response.SeeOther creates 303 redirect"):
    val response = Response.SeeOther("/other-location")

    assertEquals(response.status, Status.SeeOther)
    assertEquals(
      response.headers.get(Header.Location.name),
      Some(Seq("/other-location"))
    )

  test("Response addHeader adds single header"):
    val response = Response.Ok("test").addHeader("X-Custom", "value")

    assertEquals(response.headers.get("X-Custom"), Some(Seq("value")))

  test("Response addHeader with multiple values"):
    val response = Response.Ok("test").addHeader("X-Custom", Seq("value1", "value2"))

    assertEquals(response.headers.get("X-Custom"), Some(Seq("value1", "value2")))

  test("Response addHeader merges with existing headers"):
    val response = Response
      .Ok("test")
      .addHeader("X-Header-1", "value1")
      .addHeader("X-Header-2", "value2")

    assertEquals(response.headers.get("X-Header-1"), Some(Seq("value1")))
    assertEquals(response.headers.get("X-Header-2"), Some(Seq("value2")))

  test("Response withCookie adds cookie"):
    val cookie = Cookie("session_id", "abc123")
    val response = Response.Ok("test").withCookie(cookie)

    assertEquals(response.cookies.get("session_id"), Some(cookie))

  test("Response withCookie with multiple cookies"):
    val cookie1 = Cookie("session_id", "abc123")
    val cookie2 = Cookie("user_pref", "dark_mode")
    val response = Response
      .Ok("test")
      .withCookie(cookie1)
      .withCookie(cookie2)

    assertEquals(response.cookies.get("session_id"), Some(cookie1))
    assertEquals(response.cookies.get("user_pref"), Some(cookie2))

  test("Response deleteCookie marks cookie for deletion"):
    val cookie = Cookie("session_id", "abc123")
    val response = Response
      .Ok("test")
      .withCookie(cookie)
      .deleteCookie("session_id")

    val deletedCookie = response.cookies.get("session_id")
    assert(deletedCookie.isDefined)
    assertEquals(deletedCookie.get.value, "")
    assertEquals(deletedCookie.get.expires, Some(Instant.EPOCH))
    assertEquals(deletedCookie.get.maxAge, Some(0L))

  test("Response deleteCookie with Cookie object"):
    val cookie = Cookie("session_id", "abc123", path = Some("/app"))
    val response = Response
      .Ok("test")
      .withCookie(cookie)
      .deleteCookie(cookie)

    val deletedCookie = response.cookies.get("session_id")
    assert(deletedCookie.isDefined)
    assertEquals(deletedCookie.get.path, Some("/app")) // Preserves original path
    assertEquals(deletedCookie.get.expires, Some(Instant.EPOCH))
    assertEquals(deletedCookie.get.maxAge, Some(0L))

  test("Response can have error"):
    val exception = new RuntimeException("Test error")
    val response = Response.Ok("test").copy(error = Some(exception))

    assert(response.error.isDefined)
    assertEquals(response.error.get.getMessage, "Test error")

  test("Response can have content length"):
    val response = Response.Ok("test").copy(length = Some(100L))

    assertEquals(response.length, Some(100L))

  test("Response with custom status"):
    val response = Response(
      status = Status.Created,
      headers = Map.empty,
      bodyWriter = _ => ()
    )

    assertEquals(response.status, Status.Created)

  test("Response preserves all fields when adding headers"):
    val originalCookie = Cookie("test", "value")
    val originalError = new RuntimeException("error")
    val response = Response
      .Ok("test")
      .copy(
        cookies = Map("test" -> originalCookie),
        error = Some(originalError),
        length = Some(42L)
      )
      .addHeader("X-Custom", "value")

    assertEquals(response.cookies.get("test"), Some(originalCookie))
    assertEquals(response.error, Some(originalError))
    assertEquals(response.length, Some(42L))
    assertEquals(response.headers.get("X-Custom"), Some(Seq("value")))

  test("Response body writer can be executed"):
    var written = false
    val response = Response(
      status = Status.OK,
      headers = Map.empty,
      bodyWriter = _ => written = true
    )

    val baos = ByteArrayOutputStream()
    response.bodyWriter(baos)
    assert(written)

end ResponseTest
