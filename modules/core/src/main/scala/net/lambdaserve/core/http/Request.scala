package net.lambdaserve.core.http

import net.lambdaserve.core.codec.EntityEncoder

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import scala.io.Source

case class Request(
  scheme: String,
  method: Method,
  path: String,
  pathParams: Map[String, IndexedSeq[String]] = Map.empty,
  headers: Map[String, IndexedSeq[String]] = Map.empty,
  query: Map[String, IndexedSeq[String]] = Map.empty,
  cookies: Map[String, Cookie] = Map.empty,
  form: Map[String, IndexedSeq[String]] = Map.empty,
  multipartForm: Seq[MultiPart] = Seq.empty,
  requestContent: InputStream = InputStream.nullInputStream()
):
  lazy val contentType: Option[String] =
    headers.get(Header.ContentType.name).flatMap(_.headOption)
  lazy val contentLength: Option[Long] =
    headers.get(Header.ContentLength.name).flatMap(_.headOption.map(_.toLong))
  lazy val contentEncoding: Option[String] =
    headers.get(Header.ContentEncoding.name).flatMap(_.headOption)

  private lazy val bodyCharset = contentEncoding match
    case Some(s"${_}; charset=${charset}") => Some(charset)
    case _                                 => None

  lazy val stringBody: String =
    bodyCharset.fold(Source.fromInputStream(requestContent).mkString) {
      charset =>
        Source.fromInputStream(requestContent, charset).mkString
    }

  def withHeaders(newHeaders: Map[String, IndexedSeq[String]]): Request =
    this.copy(headers = this.headers ++ newHeaders)

  def withHeader(header: String, value: IndexedSeq[String]): Request =
    this.copy(headers = headers ++ Map(header -> value))

  def withHeader(header: String, value: String): Request =
    withHeader(header, IndexedSeq(value))

  def withQueryParam(name: String, value: IndexedSeq[String]): Request =
    this.copy(query = query ++ Map(name -> value))

  def withQueryParam(name: String, value: String): Request =
    withQueryParam(name, IndexedSeq(value))

  def withQuery(newQuery: Map[String, IndexedSeq[String]]): Request =
    this.copy(query = this.query ++ newQuery)

  def withFormParam(name: String, value: IndexedSeq[String]): Request =
    this.copy(form = form ++ Map(name -> value))

  def withFormParam(name: String, value: String): Request =
    withFormParam(name, IndexedSeq(value))

  def withForm(newForm: Map[String, IndexedSeq[String]]): Request =
    this.copy(form = this.form ++ newForm)

object Request:
  private def requestWithBody(
    method: Method,
    body: InputStream,
    path: String,
    query: Map[String, IndexedSeq[String]] = Map.empty
  ): Request =
    val uri = new java.net.URI(
      "http://localhost" + (if path.startsWith("/") then path else "/" + path)
    )

    Request(
      method = method,
      scheme = uri.getScheme,
      path = uri.getPath,
      query = query,
      pathParams = Map.empty,
      headers = Map.empty,
      requestContent = body
    )

  def GET(path: String): Request =
    requestWithBody(Method.GET, InputStream.nullInputStream(), path)

  def POST(body: InputStream, path: String): Request =
    requestWithBody(Method.POST, body, path)

  def POST(body: String, path: String): Request =
    POST(ByteArrayInputStream(body.getBytes), path)

  def POST[B: EntityEncoder](body: B, path: String): Request =
    val ec   = summon[EntityEncoder[B]]
    val baos = ByteArrayOutputStream()
    ec.bodyWriter(body)(baos)

    POST(new ByteArrayInputStream(baos.toByteArray), path)
      .withHeader(Header.ContentType.name, ec.contentTypeHeader)

  def PUT(body: InputStream, path: String): Request =
    requestWithBody(Method.PUT, body, path)

  def PUT(body: String, path: String): Request =
    PUT(ByteArrayInputStream(body.getBytes), path)

  def PUT[B: EntityEncoder](body: B, path: String): Request =
    val ec   = summon[EntityEncoder[B]]
    val baos = ByteArrayOutputStream()
    ec.bodyWriter(body)(baos)

    PUT(new ByteArrayInputStream(baos.toByteArray), path)
      .withHeader(Header.ContentType.name, ec.contentTypeHeader)

  def PATCH(body: InputStream, path: String): Request =
    requestWithBody(Method.PATCH, body, path)

  def PATCH(body: String, path: String): Request =
    PATCH(ByteArrayInputStream(body.getBytes), path)

  def PATCH[B: EntityEncoder](body: B, path: String): Request =
    val ec   = summon[EntityEncoder[B]]
    val baos = ByteArrayOutputStream()
    ec.bodyWriter(body)(baos)

    PATCH(new ByteArrayInputStream(baos.toByteArray), path)
      .withHeader(Header.ContentType.name, ec.contentTypeHeader)
