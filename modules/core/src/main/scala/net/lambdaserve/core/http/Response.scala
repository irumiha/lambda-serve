package net.lambdaserve.core.http

import net.lambdaserve.core.codec.EntityEncoder
import net.lambdaserve.core.http.Util.HttpHeader.ContentType
import net.lambdaserve.core.http.Util.{HttpHeader, HttpStatus}

import java.io.OutputStream
import java.nio.charset.Charset

case class Response(
  status: HttpStatus,
  headers: Map[String, Seq[String]],
  bodyWriter: OutputStream => Unit,
  length: Option[Long] = None,
  error: Option[Throwable] = None
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
      HttpStatus.OK,
      finalHeaders,
      os => os.write(bodyArray),
      Some(bodyArray.length)
    )

  def Ok(body: String): Response = Ok(body, Charset.defaultCharset(), Map())

  def Ok[R](entity: R)(using enc: EntityEncoder[R]): Response =
    val contentType = Map(
      HttpHeader.ContentType.name -> Seq(enc.contentTypeHeader)
    )
    Response(HttpStatus.OK, contentType, enc.bodyWriter(entity), None)

  def Ok[R](entity: R, headers: Map[String, Seq[String]])(using
    enc: EntityEncoder[R]
  ): Response =
    val contentType = Map(
      HttpHeader.ContentType.name -> Seq(enc.contentTypeHeader)
    )
    Response(
      HttpStatus.OK,
      contentType ++ headers,
      enc.bodyWriter(entity),
      None
    )

  def NotFound: Response =
    Response(HttpStatus.NotFound, Map.empty, os => {}, Some(-1))

  def BadRequest: Response =
    Response(HttpStatus.BadRequest, Map.empty, os => {}, Some(-1))

  def Redirect(location: String): Response =
    Response(
      HttpStatus.Found,
      Map("Location" -> Seq(location)),
      os => {},
      Some(-1)
    )

  def SeeAlso(location: String): Response =
    Response(
      HttpStatus.SeeOther,
      Map("Location" -> Seq(location)),
      os => {},
      Some(-1)
    )
