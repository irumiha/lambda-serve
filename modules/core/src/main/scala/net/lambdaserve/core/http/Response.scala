package net.lambdaserve.core.http

import net.lambdaserve.core.codec.EntityEncoder
import net.lambdaserve.core.http.Util.HttpHeader.ContentType
import net.lambdaserve.core.http.Util.{HttpHeader, HttpStatus}

import java.io.OutputStream

case class Response(
  status: HttpStatus,
  headers: Map[String, Seq[String]],
  bodyWriter: OutputStream => Unit,
  length: Option[Long] = None,
  error: Option[Throwable] = None
):
  def addHeader(name: String, value: String): Response =
    copy(headers = headers + (name -> Seq(value)))

  def addHeader(name: String, value: Seq[String]): Response =
    copy(headers = headers + (name -> value))


object Response:
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
