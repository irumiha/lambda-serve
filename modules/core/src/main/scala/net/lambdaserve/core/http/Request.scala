package net.lambdaserve.core.http

import net.lambdaserve.core.http.Util.HttpMethod

import java.io.InputStream
import java.net.URLDecoder
import scala.io.Source
import scala.util.matching.Regex

case class RequestHeader(
  scheme: String,
  method: HttpMethod,
  path: String,
  pathParams: Map[String, Seq[String]] = Map(),
  headers: Map[String, Seq[String]],
  query: Map[String, Seq[String]],
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
      .decode(stringBody, "UTF-8")
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

  def stringBody: String =
    bodyCharset.fold(Source.fromInputStream(requestContent).mkString) {
      charset =>
        Source.fromInputStream(requestContent, charset).mkString
    }

  def formBody: Map[String, Seq[String]] = header.contentType match
    case Some("application/x-www-form-urlencoded") =>
      parseFormBody(requestContent, "UTF-8")
    case _ => Map.empty

  case class Part(headers: Map[String, String], body: String)

  def multipartBody: Map[String, Seq[Part]] = header.contentType match
    case Some(s"multipart/form-data; boundary=${boundary}") =>
      parseMultipartFormData(requestContent, boundary)
    case _ => Map.empty
