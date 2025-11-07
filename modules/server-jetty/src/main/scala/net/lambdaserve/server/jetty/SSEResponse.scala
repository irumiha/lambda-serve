package net.lambdaserve.server.jetty

import org.eclipse.jetty.io.Content
import org.eclipse.jetty.server.Response
import org.eclipse.jetty.util.Callback

object SSEResponse:
  /** Sends an SSE event
    * @param data
    *   The data to send
    * @param event
    *   Optional event name
    * @param id
    *   Optional event ID
    * @param retry
    *   Optional retry time in milliseconds
    */
  case class Event(
    data: String,
    event: Option[String] = None,
    id: Option[String] = None,
    retry: Option[Long] = None
  ):
    def format: String =
      val sb = new StringBuilder
      event.foreach(e => sb.append(s"event: $e\n"))
      id.foreach(i => sb.append(s"id: $i\n"))
      retry.foreach(r => sb.append(s"retry: $r\n"))
      // Split data by newlines and prefix each with "data: "
      data.split("\n").foreach(line => sb.append(s"data: $line\n"))
      sb.append("\n") // The empty line indicates the end of the event
      sb.toString

  /** Initialize an SSE response
    */
  def initSSE(response: Response): Unit =
    response.setStatus(200)
    response.getHeaders.put("Content-Type", "text/event-stream")
    response.getHeaders.put("Cache-Control", "no-cache")
    response.getHeaders.put("Connection", "keep-alive")
    // Disable buffering
    response.getHeaders.put("X-Accel-Buffering", "no")

  /** Send an SSE event
    */
  def sendEvent(response: Response, event: Event): Unit =
    Content.Sink.write(response, false, event.format, Callback.NOOP)

  /** Send a simple message (convenience method)
    */
  def sendMessage(response: Response, data: String): Unit =
    sendEvent(response, Event(data))

  /** Send a keep-alive comment (prevents timeout)
    */
  def sendKeepAlive(response: Response): Unit =
    val comment = ": keep-alive\n\n"
    Content.Sink.write(response, false, comment, Callback.NOOP)

  /** Complete the SSE stream
    */
  def complete(response: Response): Unit =
    Content.Sink.write(response, true, null, Callback.NOOP)
