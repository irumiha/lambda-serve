package net.lambdaserve.http

import net.lambdaserve.codec.EntityEncoder
import Header.ContentType

import java.io.OutputStream
import java.time.Instant

sealed trait Response

object Response:
  extension (r: Response)
    def asHttp: HttpResponse =
      r match
        case r: HttpResponse => r
        case _ => throw new IllegalArgumentException("Not an HTTP response")

case class HttpResponse(
  status: Status,
  headers: Map[String, Seq[String]],
  bodyWriter: OutputStream => Unit,
  length: Option[Long] = None,
  error: Option[Throwable] = None,
  cookies: Map[String, Cookie] = Map.empty
) extends Response:
  def addHeader(name: String, value: String): HttpResponse =
    copy(headers = headers + (name -> Seq(value)))

  def addHeader(name: String, value: Seq[String]): HttpResponse =
    copy(headers = headers + (name -> value))

  def withCookie(cookie: Cookie): HttpResponse =
    copy(cookies = cookies + (cookie.name -> cookie))

  def deleteCookie(name: String): HttpResponse =
    copy(cookies =
      cookies + (name -> Cookie(
        name,
        "",
        expires = Some(Instant.EPOCH),
        maxAge = Some(0)
      ))
    )

  def deleteCookie(cookie: Cookie): HttpResponse =
    copy(cookies =
      cookies + (cookie.name ->
        cookie.copy(expires = Some(Instant.EPOCH), maxAge = Some(0)))
    )

object HttpResponse:

  def apply[R](status: Status, headers: Map[String, Seq[String]], entity: R)(
    using enc: EntityEncoder[R]
  ): HttpResponse =
    val contentType = Map(Header.ContentType.name -> Seq(enc.contentTypeHeader))
    HttpResponse(status, contentType ++ headers, enc.bodyWriter(entity), None)

  def Ok[R](entity: R)(using enc: EntityEncoder[R]): HttpResponse =
    HttpResponse(Status.OK, Map.empty, entity)

  def Ok[R](entity: R, headers: Map[String, Seq[String]])(using
    enc: EntityEncoder[R]
  ): HttpResponse =
    HttpResponse(Status.OK, headers, entity)

  def NotFound: HttpResponse =
    HttpResponse(Status.NotFound, Map.empty, "")

  def BadRequest: HttpResponse =
    HttpResponse(Status.BadRequest, Map.empty, "")

  def BadRequest[R](entity: R)(using enc: EntityEncoder[R]): HttpResponse =
    HttpResponse(Status.BadRequest, Map.empty, entity)

  def Found(location: String): HttpResponse =
    HttpResponse(Status.Found, Map(Header.Location.name -> Seq(location)), "")

  def SeeOther(location: String): HttpResponse =
    HttpResponse(
      Status.SeeOther,
      Map(Header.Location.name -> Seq(location)),
      ""
    )

sealed trait SSEResponse extends Response:
  case class Event(event: String, data: String) extends SSEResponse
  case class Close()                            extends SSEResponse
