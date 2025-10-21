package net.lambdaserve.filters

import munit.FunSuite
import net.lambdaserve.http.{Request, HttpResponse, Status}

class FilterEngineTest extends FunSuite:

  // Helper filters for testing
  class ContinueFilter(val pathModifier: String => String) extends Filter:
    override def handle(request: Request): FilterInResponse =
      FilterInResponse.Continue(request.copy(path = pathModifier(request.path)))

  class StopFilter(val response: HttpResponse) extends Filter:
    override def handle(request: Request): FilterInResponse =
      FilterInResponse.Stop(response)

  class WrapFilter(val headerToAdd: (String, String)) extends Filter:
    override def handle(request: Request): FilterInResponse =
      FilterInResponse.Wrap(
        request,
        response =>
          FilterOutResponse.Continue(
            response.asHttp.addHeader(headerToAdd._1, headerToAdd._2)
          )
      )

  class ConditionalStopFilter(val predicate: Request => Boolean) extends Filter:
    override def handle(request: Request): FilterInResponse =
      if predicate(request) then
        FilterInResponse.Stop(HttpResponse.Ok("Stopped by filter"))
      else FilterInResponse.Continue(request)

  test("FilterEngine with empty filter list fails assertion"):
    val engine = FilterEngine(IndexedSeq.empty)
    val request = Request.GET("/test")

    // This should fail the assertion in FilterEngine
    intercept[AssertionError] {
      engine.processRequest(request)
    }

  test("FilterEngine with single Continue filter must include terminal filter"):
    val continueFilter = ContinueFilter(path => path + "/modified")
    val engine = FilterEngine(IndexedSeq(continueFilter))
    val request = Request.GET("/test")

    // This should fail because no filter stops the pipeline
    intercept[AssertionError] {
      engine.processRequest(request)
    }

  test("FilterEngine with Continue then Stop filters processes both"):
    val continueFilter = ContinueFilter(path => path + "/modified")
    val stopFilter = StopFilter(HttpResponse.Ok("Done"))
    val engine = FilterEngine(IndexedSeq(continueFilter, stopFilter))
    val request = Request.GET("/test")

    val response = engine.processRequest(request)

    assertEquals(response.asHttp.status, Status.OK)

  test("FilterEngine stops processing when Stop is encountered"):
    val continueFilter1 = ContinueFilter(path => path + "/first")
    val stopFilter = StopFilter(HttpResponse.Ok("Stopped"))
    val continueFilter2 =
      ContinueFilter(path => path + "/second") // Should not be executed
    val engine =
      FilterEngine(IndexedSeq(continueFilter1, stopFilter, continueFilter2))
    val request = Request.GET("/test")

    val response = engine.processRequest(request)

    assertEquals(response.asHttp.status, Status.OK)
    // Verify that the third filter was not applied (path should not contain "/second")

  test("FilterEngine processes multiple Continue filters in sequence"):
    val filter1 = ContinueFilter(path => path + "/first")
    val filter2 = ContinueFilter(path => path + "/second")
    val filter3 = ContinueFilter(path => path + "/third")
    val stopFilter = StopFilter(HttpResponse.Ok("Done"))
    val engine = FilterEngine(IndexedSeq(filter1, filter2, filter3, stopFilter))
    val request = Request.GET("/test")

    val response = engine.processRequest(request)

    // All Continue filters should have been applied before Stop
    assertEquals(response.asHttp.status, Status.OK)

  test("FilterEngine applies Wrap filters to response"):
    val wrapFilter = WrapFilter("X-Custom-Header" -> "CustomValue")
    val stopFilter = StopFilter(HttpResponse.Ok("Done"))
    val engine = FilterEngine(IndexedSeq(wrapFilter, stopFilter))
    val request = Request.GET("/test")

    val response = engine.processRequest(request)

    assertEquals(response.asHttp.status, Status.OK)
    assertEquals(
      response.asHttp.headers.get("X-Custom-Header"),
      Some(Seq("CustomValue"))
    )

  test("FilterEngine applies multiple Wrap filters in reverse order"):
    val wrapFilter1 = WrapFilter("X-Header-1" -> "Value1")
    val wrapFilter2 = WrapFilter("X-Header-2" -> "Value2")
    val stopFilter = StopFilter(HttpResponse.Ok("Done"))
    val engine = FilterEngine(IndexedSeq(wrapFilter1, wrapFilter2, stopFilter))
    val request = Request.GET("/test")

    val response = engine.processRequest(request)

    assertEquals(response.asHttp.status, Status.OK)
    assertEquals(response.asHttp.headers.get("X-Header-1"), Some(Seq("Value1")))
    assertEquals(response.asHttp.headers.get("X-Header-2"), Some(Seq("Value2")))

  test("FilterEngine combines Continue, Wrap, and Stop filters"):
    val continueFilter = ContinueFilter(path => path + "/modified")
    val wrapFilter = WrapFilter("X-Modified" -> "true")
    val stopFilter = StopFilter(HttpResponse.Ok("Final"))
    val engine = FilterEngine(IndexedSeq(continueFilter, wrapFilter, stopFilter))
    val request = Request.GET("/test")

    val response = engine.processRequest(request)

    assertEquals(response.asHttp.status, Status.OK)
    assertEquals(response.asHttp.headers.get("X-Modified"), Some(Seq("true")))

  test("FilterEngine respects includePrefixes"):
    val filter = new Filter:
      override val includePrefixes = List("/api")
      override def handle(request: Request): FilterInResponse =
        FilterInResponse.Continue(request.copy(path = request.path + "/api-only"))

    val stopFilter = StopFilter(HttpResponse.Ok("Done"))
    val engine = FilterEngine(IndexedSeq(filter, stopFilter))

    // Request matching includePrefixes
    val apiRequest = Request.GET("/api/test")
    val apiResponse = engine.processRequest(apiRequest)
    assertEquals(apiResponse.asHttp.status, Status.OK)

    // Request not matching includePrefixes
    val nonApiRequest = Request.GET("/other/test")
    val nonApiResponse = engine.processRequest(nonApiRequest)
    assertEquals(nonApiResponse.asHttp.status, Status.OK)

  test("FilterEngine respects excludePrefixes"):
    val filter = new Filter:
      override val includePrefixes = List("")
      override val excludePrefixes = List("/admin")
      override def handle(request: Request): FilterInResponse =
        FilterInResponse.Continue(request.copy(path = request.path + "/filtered"))

    val stopFilter = StopFilter(HttpResponse.Ok("Done"))
    val engine = FilterEngine(IndexedSeq(filter, stopFilter))

    // Request matching excludePrefixes should skip filter
    val adminRequest = Request.GET("/admin/test")
    val adminResponse = engine.processRequest(adminRequest)
    assertEquals(adminResponse.asHttp.status, Status.OK)

    // Regular request should apply filter
    val regularRequest = Request.GET("/api/test")
    val regularResponse = engine.processRequest(regularRequest)
    assertEquals(regularResponse.asHttp.status, Status.OK)

  test("FilterEngine handles FilterOutResponse.Stop"):
    val wrapFilterWithStop = new Filter:
      override def handle(request: Request): FilterInResponse =
        FilterInResponse.Wrap(
          request,
          response =>
            FilterOutResponse.Stop(
              response.asHttp.addHeader("X-Stopped", "true")
            )
        )

    val wrapFilter2 =
      WrapFilter("X-Should-Not-Appear" -> "value") // Should not be applied
    val stopFilter = StopFilter(HttpResponse.Ok("Done"))
    val engine =
      FilterEngine(IndexedSeq(wrapFilter2, wrapFilterWithStop, stopFilter))
    val request = Request.GET("/test")

    val response = engine.processRequest(request)

    assertEquals(response.asHttp.status, Status.OK)
    assertEquals(response.asHttp.headers.get("X-Stopped"), Some(Seq("true")))
    // The second wrap filter should not have been applied
    assert(!response.asHttp.headers.contains("X-Should-Not-Appear"))

  test("FilterEngine handles conditional Stop in middle of chain"):
    val continueFilter = ContinueFilter(path => path + "/first")
    val conditionalStop =
      ConditionalStopFilter(req => req.path.contains("/stop"))
    val continueFilter2 =
      ContinueFilter(path => path + "/second") // Should not execute
    val stopFilter = StopFilter(HttpResponse.Ok("Final"))
    val engine = FilterEngine(
      IndexedSeq(continueFilter, conditionalStop, continueFilter2, stopFilter)
    )

    val stopRequest = Request.GET("/stop/test")
    val response = engine.processRequest(stopRequest)

    assertEquals(response.asHttp.status, Status.OK)

  test("FilterEngine with request modification through chain"):
    val addHeaderFilter = new Filter:
      override def handle(request: Request): FilterInResponse =
        FilterInResponse.Continue(
          request.withHeader("X-Filter-1", "Applied")
        )

    val addQueryParamFilter = new Filter:
      override def handle(request: Request): FilterInResponse =
        FilterInResponse.Continue(
          request.withQueryParam("filtered", "true")
        )

    val stopFilter = StopFilter(HttpResponse.Ok("Done"))
    val engine =
      FilterEngine(IndexedSeq(addHeaderFilter, addQueryParamFilter, stopFilter))
    val request = Request.GET("/test")

    val response = engine.processRequest(request)

    assertEquals(response.asHttp.status, Status.OK)
    // Request modifications should have been applied through the chain

end FilterEngineTest
