package net.lambdaserve.filters

import munit.FunSuite
import net.lambdaserve.http.{Request, Response}

class RequestIdFilterTest extends FunSuite:

  test("RequestIdFilter generates and adds request ID"):
    var generatedId = ""
    val filter = RequestIdFilter(generateId = () => {
      generatedId = "test-id-123"
      generatedId
    })
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(updatedRequest, responseWrapper) =>
        assertEquals(
          updatedRequest.headers.get("X-Request-ID"),
          Some(IndexedSeq("test-id-123"))
        )
        val mockResponse = Response.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.headers.get("X-Request-ID"),
              Some(Seq("test-id-123"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("RequestIdFilter preserves existing request ID"):
    val filter = RequestIdFilter(generateId = () => "should-not-be-used")
    val request = Request
      .GET("/test")
      .withHeader("X-Request-ID", "existing-id-456")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(updatedRequest, responseWrapper) =>
        assertEquals(
          updatedRequest.headers.get("X-Request-ID"),
          Some(IndexedSeq("existing-id-456"))
        )
        val mockResponse = Response.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.headers.get("X-Request-ID"),
              Some(Seq("existing-id-456"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("RequestIdFilter with custom header name"):
    val filter = RequestIdFilter(
      requestIdHeader = "X-Trace-ID",
      generateId = () => "trace-789"
    )
    val request = Request.GET("/test")

    val result = filter.handle(request)

    result match
      case FilterInResponse.Wrap(updatedRequest, responseWrapper) =>
        assertEquals(
          updatedRequest.headers.get("X-Trace-ID"),
          Some(IndexedSeq("trace-789"))
        )
        val mockResponse = Response.Ok("test")
        responseWrapper(mockResponse) match
          case FilterOutResponse.Continue(wrappedResponse) =>
            assertEquals(
              wrappedResponse.headers.get("X-Trace-ID"),
              Some(Seq("trace-789"))
            )
          case _ => fail("Expected Continue response")
      case _ => fail("Expected Wrap response")

  test("RequestIdFilter generates unique IDs for different requests"):
    var idCounter = 0
    val filter = RequestIdFilter(generateId = () => {
      idCounter += 1
      s"id-$idCounter"
    })

    val request1 = Request.GET("/test1")
    val request2 = Request.GET("/test2")

    val result1 = filter.handle(request1)
    val result2 = filter.handle(request2)

    result1 match
      case FilterInResponse.Wrap(updatedRequest, _) =>
        assertEquals(
          updatedRequest.headers.get("X-Request-ID"),
          Some(IndexedSeq("id-1"))
        )
      case _ => fail("Expected Wrap response")

    result2 match
      case FilterInResponse.Wrap(updatedRequest, _) =>
        assertEquals(
          updatedRequest.headers.get("X-Request-ID"),
          Some(IndexedSeq("id-2"))
        )
      case _ => fail("Expected Wrap response")

end RequestIdFilterTest
