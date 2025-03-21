package net.lambdaserve.http

import net.lambdaserve.codec.EntityEncoder
import Header.ContentType

import java.io.OutputStream
import java.time.Instant

case class Response(
  status: Status,
  headers: Map[String, Seq[String]],
  bodyWriter: OutputStream => Unit,
  length: Option[Long] = None,
  error: Option[Throwable] = None,
  cookies: Map[String, Cookie] = Map.empty
):
  def addHeader(name: String, value: String): Response =
    copy(headers = headers + (name -> Seq(value)))

  def addHeader(name: String, value: Seq[String]): Response =
    copy(headers = headers + (name -> value))

  def withCookie(cookie: Cookie): Response =
    copy(cookies = cookies + (cookie.name -> cookie))

  def deleteCookie(name: String): Response =
    copy(cookies =
      cookies + (name -> Cookie(
        name,
        "",
        expires = Some(Instant.EPOCH),
        maxAge = Some(0)
      ))
    )

  def deleteCookie(cookie: Cookie): Response =
    copy(cookies =
      cookies + (cookie.name ->
        cookie.copy(expires = Some(Instant.EPOCH), maxAge = Some(0)))
    )

object Response:
  def apply[R](status: Status, headers: Map[String, Seq[String]], entity: R)(
    using enc: EntityEncoder[R]
  ): Response =
    val contentType = Map(Header.ContentType.name -> Seq(enc.contentTypeHeader))
    Response(status, contentType ++ headers, enc.bodyWriter(entity), None)

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

  def BadRequest[R](entity: R)(using enc: EntityEncoder[R]): Response =
    Response(Status.BadRequest, Map.empty, entity)

  def Found(location: String): Response =
    Response(Status.Found, Map(Header.Location.name -> Seq(location)), "")

  def SeeOther(location: String): Response =
    Response(Status.SeeOther, Map(Header.Location.name -> Seq(location)), "")
