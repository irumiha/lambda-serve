package net.lambdaserve.core.http

import net.lambdaserve.core.http.Util.HttpHeader.ContentType
import net.lambdaserve.core.http.Util.{HttpHeader, HttpStatus}

import java.io.OutputStream
import java.nio.charset.Charset

case class ResponseHeader(status: HttpStatus, headers: Map[String, Seq[String]])

case class Response(
  header: ResponseHeader,
  bodyWriter: OutputStream => Unit,
  length: Option[Long] = None
)

object Response:

  def Ok(
    body: String,
    charset: Charset,
    headers: Map[String, Seq[String]]
  ): Response =
    val bodyArray = body.getBytes(charset)
    val finalHeaders =
      if !headers.contains(ContentType.name) then
        headers + ("Content-Type" -> Seq(
          s"text/plain; charset=${charset.name}"
        ))
      else headers

    Response(
      ResponseHeader(HttpStatus.OK, finalHeaders),
      os => os.write(bodyArray),
      Some(bodyArray.length)
    )

  def Ok(body: String): Response = Ok(body, Charset.defaultCharset(), Map())

  def NotFound: Response =
    Response(
      ResponseHeader(HttpStatus.NotFound, Map.empty),
      os => {},
      Some(-1)
    )

  def BadRequest: Response =
    Response(
      ResponseHeader(HttpStatus.BadRequest, Map.empty),
      os => {},
      Some(-1)
    )

  def Redirect(location: String): Response =
    Response(
      ResponseHeader(HttpStatus.Found, Map("Location" -> Seq(location))),
      os => {},
      Some(-1)
    )

  def SeeAlso(location: String): Response =
    Response(
      ResponseHeader(HttpStatus.SeeOther, Map("Location" -> Seq(location))),
      os => {},
      Some(-1)
    )
