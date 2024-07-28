package net.lambdaserve.core.http

import net.lambdaserve.core.codec.EntityEncoder
import net.lambdaserve.core.http.Header.ContentType
import net.lambdaserve.core.http.{Header, Status}

import java.io.OutputStream

case class Response(
  status: Status,
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
  def apply[R](
    status: Status,
    headers: Map[String, Seq[String]],
    entity: R
  )(using enc: EntityEncoder[R]): Response =
    val contentType = Map(
      Header.ContentType.name -> Seq(enc.contentTypeHeader)
      )
    Response(
      status,
      contentType ++ headers,
      enc.bodyWriter(entity),
      None
    )

  def Ok[R](entity: R)(using enc: EntityEncoder[R]): Response =
    Response(Status.OK, Map.empty, entity)

  def Ok[R](entity: R, headers: Map[String, Seq[String]])(using
    enc: EntityEncoder[R]
  ): Response =
    Response(Status.OK, headers, entity)

  def NotFound: Response =
    Response(Status.NotFound, Map.empty, "")

  def BadRequest: Response =
    Response(Status.BadRequest, Map.empty, "")

  def Found(location: String): Response =
    Response(
      Status.Found,
      Map(Header.Location.name -> Seq(location)),
      ""
      )

  def SeeOther(location: String): Response =
    Response(
      Status.SeeOther,
      Map("Location" -> Seq(location)),
      ""
      )
