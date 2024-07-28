package net.lambdaserve.core.http

import net.lambdaserve.core.codec.EntityEncoder
import net.lambdaserve.core.http.{Header, Method}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, PipedInputStream, PipedOutputStream, StringReader}
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import scala.io.Source
import scala.util.matching.Regex

def defaultMap(): Map[String, IndexedSeq[String]] = Map.empty

case class RequestHeader(
  scheme: String = "https",
  method: Method = Method.GET,
  path: String = "/",
  pathParams: Map[String, IndexedSeq[String]] = Map.empty,
  headers: Map[String, IndexedSeq[String]] = Map.empty,
  query: Map[String, IndexedSeq[String]] = Map.empty,
  contentType: Option[String] = None,
  contentLength: Option[Long] = None,
  contentEncoding: Option[String] = None
)

object RequestHeader:
  def parseQuery(query: String): Map[String, IndexedSeq[String]] =
    if query == null || query.isBlank then Map.empty
    else
      query
        .split("&")
        .map(_.split("="))
        .groupBy(_(0))
        .map { case (k, v) => k -> v.map(_(1)).toIndexedSeq }

case class Request(header: RequestHeader, requestContent: InputStream):
  export header.*

  private def parseFormBody(
    body: InputStream,
    charset: String
  ): Map[String, IndexedSeq[String]] =
    val stringBody = Source.fromInputStream(body, charset).mkString
    URLDecoder
      .decode(stringBody, StandardCharsets.UTF_8.name())
      .split("&")
      .map(_.split("="))
      .groupBy(_(0))
      .map { case (k, v) => k -> v.map(_(1)).toIndexedSeq }

  private def parseMultipartFormData(
    input: InputStream,
    boundary: String
  ): Map[String, Seq[Part]] =
    val inputString        = Source.fromInputStream(input).mkString
    val partPattern: Regex = s"--$boundary\r\n(.*?)\r\n\r\n(.*?)--$boundary".r

    partPattern
      .findAllIn(inputString)
      .matchData
      .map { m =>
        val headers = m
          .group(1)
          .split("\r\n")
          .map { line =>
            val Array(name, value) = line.split(": ")
            name -> value
          }
          .toMap

        val body = m.group(2)

        Part(headers, body)
      }
      .toSeq
      .groupBy(_.headers("Content-Disposition"))

  private lazy val bodyCharset = header.contentEncoding match
    case Some(s"${_}; charset=${charset}") => Some(charset)
    case _                                 => None

  def string: String =
    bodyCharset.fold(Source.fromInputStream(requestContent).mkString) {
      charset =>
        Source.fromInputStream(requestContent, charset).mkString
    }

  def form: Map[String, IndexedSeq[String]] = header.contentType match
    case Some(ct) if ct.startsWith("application/x-www-form-urlencoded") =>
      parseFormBody(requestContent, StandardCharsets.UTF_8.name())
    case _ => Map.empty

  case class Part(headers: Map[String, String], body: String)

  def multipart: Map[String, Seq[Part]] = header.contentType match
    case Some(s"multipart/form-data; boundary=${boundary}") =>
      parseMultipartFormData(requestContent, boundary)
    case _ => Map.empty

  def withHeaders(newHeaders: Map[String, IndexedSeq[String]]): Request =
    this.copy(header = this.header.copy(headers = this.header.headers ++ newHeaders))

  def withHeader(header: String, value: IndexedSeq[String]): Request =
    this.copy(header = this.header.copy(headers = this.header.headers ++ Map(header -> value)))

  def withHeader(header: String, value: String): Request =
    withHeader(header, IndexedSeq(value))

  def withQueryParam(name: String, value: IndexedSeq[String]): Request =
    this.copy(header = this.header.copy(query = this.header.query ++ Map(name -> value)))

  def withQueryParam(name: String, value: String): Request =
    withQueryParam(name, IndexedSeq(value))

  def withQuery(newQuery: Map[String, IndexedSeq[String]]): Request =
    this.copy(header = this.header.copy(query = this.header.query ++ newQuery))

object Request:
  private def requestWithBody(method: Method, body: InputStream, path: String): Request =
    Request(
      RequestHeader(
        method = method,
        scheme = "http",
        path = path
      ),
      requestContent = body
    )

  def GET(path: String): Request =
    requestWithBody(Method.GET, InputStream.nullInputStream(), path)

  def POST(body: InputStream, path: String): Request =
    requestWithBody(Method.POST, body, path)

  def POST(body: String, path: String): Request =
    POST(ByteArrayInputStream(body.getBytes), path)

  def POST[B: EntityEncoder](body: B, path: String): Request =
    val ec = summon[EntityEncoder[B]]
    val baos = ByteArrayOutputStream()
    ec.bodyWriter(body)(baos)

    POST(new ByteArrayInputStream(baos.toByteArray), path)
      .withHeader(Header.ContentType.name, ec.contentTypeHeader)

  def PUT(body: InputStream, path: String): Request =
    requestWithBody(Method.PUT, body, path)

  def PUT(body: String, path: String): Request =
    PUT(ByteArrayInputStream(body.getBytes), path)

  def PUT[B: EntityEncoder](body: B, path: String): Request =
    val ec = summon[EntityEncoder[B]]
    val baos = ByteArrayOutputStream()
    ec.bodyWriter(body)(baos)

    PUT(new ByteArrayInputStream(baos.toByteArray), path)
      .withHeader(Header.ContentType.name, ec.contentTypeHeader)

  def PATCH(body: InputStream, path: String): Request =
    requestWithBody(Method.PATCH, body, path)

  def PATCH(body: String, path: String): Request =
    PATCH(ByteArrayInputStream(body.getBytes), path)

  def PATCH[B: EntityEncoder](body: B, path: String): Request =
    val ec = summon[EntityEncoder[B]]
    val baos = ByteArrayOutputStream()
    ec.bodyWriter(body)(baos)

    PATCH(new ByteArrayInputStream(baos.toByteArray), path)
      .withHeader(Header.ContentType.name, ec.contentTypeHeader)
