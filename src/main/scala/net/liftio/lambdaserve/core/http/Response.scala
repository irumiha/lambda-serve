package net.liftio
package lambdaserve.core.http

import lambdaserve.core.http.Util.{HttpHeader, HttpStatus}

import com.github.plokhotnyuk.jsoniter_scala.core.{
  JsonValueCodec,
  writeToByteBufferReentrant,
  writeToString,
  writeToStringReentrant
}

import java.io.{ByteArrayInputStream, InputStream, StringReader}
import java.nio.charset.Charset

case class ResponseHeader(status: HttpStatus, headers: Map[String, Seq[String]])

case class Response(
  header: ResponseHeader,
  body: InputStream,
  length: Option[Long] = None
)

object Response:
  def Ok(body: InputStream, headers: Map[String, Seq[String]]): Response =
    Response(ResponseHeader(HttpStatus.OK, headers), body)

  def Ok(body: String, charset: Charset, headers: Map[String, Seq[String]]): Response =
    val bodyArray    = body.getBytes(charset)
    val finalHeaders = headers + ("Content-Type" -> Seq(s"text/plain; charset=${charset.name}"))

    Response(
      ResponseHeader(HttpStatus.OK, finalHeaders),
      new ByteArrayInputStream(bodyArray),
      Some(bodyArray.length)
    )

  def Ok(body: String): Response = Ok(body, Charset.defaultCharset(), Map())

  def OkJson[R: JsonValueCodec](value: R): Response =
    Ok(
      writeToStringReentrant(value),
      Charset.defaultCharset(),
      Map(HttpHeader.ContentType.name -> Seq("application/json"))
    )

  def OkJson[R: JsonValueCodec](value: R, headers: Map[String, Seq[String]]): Response =
    Ok(
      writeToStringReentrant(value),
      Charset.defaultCharset(),
      headers + (HttpHeader.ContentType.name -> Seq("application/json"))
    )

  def NotFound: Response =
    Response(
      ResponseHeader(HttpStatus.NotFound, Map.empty),
      InputStream.nullInputStream(),
      Some(-1)
    )

  def BadRequest: Response =
    Response(
      ResponseHeader(HttpStatus.BadRequest, Map.empty),
      InputStream.nullInputStream(),
      Some(-1)
    )

  def Redirect(location: String): Response =
    Response(
      ResponseHeader(HttpStatus.Found, Map("Location" -> Seq(location))),
      InputStream.nullInputStream(),
      Some(-1)
    )

  def SeeAlso(location: String): Response =
    Response(
      ResponseHeader(HttpStatus.SeeOther, Map("Location" -> Seq(location))),
      InputStream.nullInputStream(),
      Some(-1)
    )
