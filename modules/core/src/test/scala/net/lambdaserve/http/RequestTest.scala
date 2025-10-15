package net.lambdaserve.http

import munit.FunSuite

import java.io.ByteArrayInputStream

class RequestTest extends FunSuite:

  test("Request.GET creates GET request with path"):
    val request = Request.GET("/api/test")

    assertEquals(request.method, Method.GET)
    assertEquals(request.path, "/api/test")
    assertEquals(request.scheme, "http")

  test("Request.GET handles path without leading slash"):
    val request = Request.GET("api/test")

    assertEquals(request.path, "/api/test")

  test("Request.POST creates POST request with string body"):
    val request = Request.POST("test body", "/api/test")

    assertEquals(request.method, Method.POST)
    assertEquals(request.path, "/api/test")
    assertEquals(request.stringBody, "test body")

  test("Request.POST creates POST request with InputStream body"):
    val body = ByteArrayInputStream("test body".getBytes)
    val request = Request.POST(body, "/api/test")

    assertEquals(request.method, Method.POST)
    assertEquals(request.path, "/api/test")
    assertEquals(request.stringBody, "test body")

  test("Request.PUT creates PUT request"):
    val request = Request.PUT("test body", "/api/resource")

    assertEquals(request.method, Method.PUT)
    assertEquals(request.path, "/api/resource")

  test("Request.PATCH creates PATCH request"):
    val request = Request.PATCH("test body", "/api/resource")

    assertEquals(request.method, Method.PATCH)
    assertEquals(request.path, "/api/resource")

  test("Request withHeader adds single header"):
    val request = Request.GET("/test").withHeader("X-Custom", "value")

    assertEquals(request.headers.get("X-Custom"), Some(IndexedSeq("value")))

  test("Request withHeader adds multiple values"):
    val request =
      Request.GET("/test").withHeader("X-Custom", IndexedSeq("value1", "value2"))

    assertEquals(
      request.headers.get("X-Custom"),
      Some(IndexedSeq("value1", "value2"))
    )

  test("Request withHeaders merges multiple headers"):
    val request = Request
      .GET("/test")
      .withHeaders(
        Map(
          "X-Header-1" -> IndexedSeq("value1"),
          "X-Header-2" -> IndexedSeq("value2")
        )
      )

    assertEquals(request.headers.get("X-Header-1"), Some(IndexedSeq("value1")))
    assertEquals(request.headers.get("X-Header-2"), Some(IndexedSeq("value2")))

  test("Request withQueryParam adds single query parameter"):
    val request = Request.GET("/test").withQueryParam("key", "value")

    assertEquals(request.query.get("key"), Some(IndexedSeq("value")))

  test("Request withQueryParam adds multiple values"):
    val request =
      Request.GET("/test").withQueryParam("tags", IndexedSeq("scala", "web"))

    assertEquals(request.query.get("tags"), Some(IndexedSeq("scala", "web")))

  test("Request withQuery merges multiple query parameters"):
    val request = Request
      .GET("/test")
      .withQuery(
        Map(
          "page" -> IndexedSeq("1"),
          "size" -> IndexedSeq("10")
        )
      )

    assertEquals(request.query.get("page"), Some(IndexedSeq("1")))
    assertEquals(request.query.get("size"), Some(IndexedSeq("10")))

  test("Request withFormParam adds single form parameter"):
    val request = Request.GET("/test").withFormParam("username", "john")

    assertEquals(request.form.get("username"), Some(IndexedSeq("john")))

  test("Request withForm merges multiple form parameters"):
    val request = Request
      .GET("/test")
      .withForm(
        Map(
          "username" -> IndexedSeq("john"),
          "email" -> IndexedSeq("john@example.com")
        )
      )

    assertEquals(request.form.get("username"), Some(IndexedSeq("john")))
    assertEquals(request.form.get("email"), Some(IndexedSeq("john@example.com")))

  test("Request contentType extracts Content-Type header"):
    val request = Request
      .GET("/test")
      .withHeader(Header.ContentType.name, "application/json")

    assertEquals(request.contentType, Some("application/json"))

  test("Request contentLength extracts Content-Length header"):
    val request = Request
      .GET("/test")
      .withHeader(Header.ContentLength.name, "123")

    assertEquals(request.contentLength, Some(123L))

  test("Request contentEncoding extracts Content-Encoding header"):
    val request = Request
      .GET("/test")
      .withHeader(Header.ContentEncoding.name, "gzip")

    assertEquals(request.contentEncoding, Some("gzip"))

  test("Request handles empty headers"):
    val request = Request.GET("/test")

    assertEquals(request.headers, Map.empty)
    assertEquals(request.contentType, None)
    assertEquals(request.contentLength, None)

  test("Request handles empty query parameters"):
    val request = Request.GET("/test")

    assertEquals(request.query, Map.empty)

  test("Request handles empty form data"):
    val request = Request.GET("/test")

    assertEquals(request.form, Map.empty)

  test("Request copy preserves all fields"):
    val original = Request.GET("/test")
      .withHeader("X-Custom", "value")
      .withQueryParam("page", "1")
      .withFormParam("username", "john")

    val copied = original.copy(path = "/new-path")

    assertEquals(copied.path, "/new-path")
    assertEquals(copied.headers, original.headers)
    assertEquals(copied.query, original.query)
    assertEquals(copied.form, original.form)

  test("Request stringBody reads content as string"):
    val request = Request.POST("Hello, World!", "/test")

    assertEquals(request.stringBody, "Hello, World!")

  test("Request with empty body"):
    val request = Request.GET("/test")

    assertEquals(request.stringBody, "")

  test("Request handles path params"):
    val request = Request
      .GET("/users/123")
      .copy(pathParams = Map("id" -> IndexedSeq("123")))

    assertEquals(request.pathParams.get("id"), Some(IndexedSeq("123")))

  test("Request handles cookies"):
    val cookie = Cookie("session_id", "abc123")
    val request = Request.GET("/test").copy(cookies = Map("session_id" -> cookie))

    assertEquals(request.cookies.get("session_id"), Some(cookie))

end RequestTest
