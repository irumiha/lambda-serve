package net.lambdaserve.http

import net.lambdaserve.codec.EntityEncoder
import net.lambdaserve.types.{MultiMap, TypedKey, TypedMap}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import scala.io.Source

case class Request(
  scheme: String,
  method: Method,
  path: String,
  pathParams: MultiMap = MultiMap(),
  headers: MultiMap = MultiMap(),
  query: MultiMap = MultiMap(),
  cookies: Map[String, Cookie] = Map.empty,
  form: MultiMap = MultiMap(),
  multipartForm: Seq[MultiPart] = Seq.empty,
  requestContent: InputStream = InputStream.nullInputStream(),
  data: TypedMap = TypedMap()
):
  lazy val contentType: Option[String] =
    headers.get(Header.ContentType.name).headOption
  lazy val contentLength: Option[Long] =
    headers.get(Header.ContentLength.name).headOption.map(_.toLong)
  lazy val contentEncoding: Option[String] =
    headers.get(Header.ContentEncoding.name).headOption

  private lazy val bodyCharset = contentEncoding match
    case Some(s"${_}; charset=${charset}") => Some(charset)
    case _                                 => None

  lazy val stringBody: String =
    bodyCharset.fold(Source.fromInputStream(requestContent).mkString) {
      charset =>
        Source.fromInputStream(requestContent, charset).mkString
    }

  def withHeader(header: String, value: String): Request =
    this.copy(headers = headers.update(header, value))

  def withQueryParam(name: String, value: String): Request =
    this.copy(query = query.update(name, value))

  def withFormParam(name: String, value: String): Request =
    this.copy(form = form.update(name, value))

  def withQueryParams(params: (String, String)*): Request =
    this.copy(query = query.extend(MultiMap(params: _*)))

  def withFormParams(params: (String, String)*): Request =
    this.copy(form = form.extend(MultiMap(params: _*)))

object Request:
  private def requestWithBody(
    method: Method,
    body: InputStream,
    path: String,
    query: MultiMap = MultiMap()
  ): Request =
    val uri = new java.net.URI(
      "http://localhost" + (if path.startsWith("/") then path else "/" + path)
    )

    Request(
      method = method,
      scheme = uri.getScheme,
      path = uri.getPath,
      query = query,
      pathParams = MultiMap(),
      headers = MultiMap(),
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
