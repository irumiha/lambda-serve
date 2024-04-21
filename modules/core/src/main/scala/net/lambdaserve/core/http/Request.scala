package net.lambdaserve.core.http

import net.lambdaserve.core.http.Util.HttpMethod

import java.io.InputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import scala.io.Source
import scala.util.matching.Regex

case class RequestHeader(
  scheme: String,
  method: HttpMethod,
  path: String,
  pathParams: Map[String, IndexedSeq[String]] = Map(),
  headers: Map[String, IndexedSeq[String]],
  query: Map[String, IndexedSeq[String]],
  contentType: Option[String],
  contentLength: Option[Long],
  contentEncoding: Option[String]
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
    case Some("application/x-www-form-urlencoded") =>
      parseFormBody(requestContent, StandardCharsets.UTF_8.name())
    case _ => Map.empty

  case class Part(headers: Map[String, String], body: String)

  def multipart: Map[String, Seq[Part]] = header.contentType match
    case Some(s"multipart/form-data; boundary=${boundary}") =>
      parseMultipartFormData(requestContent, boundary)
    case _ => Map.empty
