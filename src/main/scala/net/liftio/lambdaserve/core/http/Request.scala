package net.liftio
package lambdaserve.core.http

import lambdaserve.core.http.Util.HttpMethod

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromStream}
import org.eclipse.jetty.http.HttpFields

import java.io.InputStream
import java.net.URLDecoder
import scala.io.Source
import scala.jdk.CollectionConverters.*
import scala.util.matching.Regex

case class RequestHeader(
  scheme: String,
  method: HttpMethod,
  path: String,
  headers: Map[String, Seq[String]],
  query: Map[String, Seq[String]],
  contentType: Option[String],
  contentLength: Option[Long],
  contentEncoding: Option[String]
)

object RequestHeader:
  private def parseQuery(query: String): Map[String, Seq[String]] =
    if query == null || query.isBlank then Map.empty
    else
      query
        .split("&")
        .map(_.split("="))
        .groupBy(_(0))
        .map { case (k, v) => k -> v.map(_(1)).toSeq }

  def apply(
    scheme: String,
    method: String,
    path: String,
    headers: HttpFields,
    queryString: Option[String]
  ): RequestHeader =
    val requestQuery    = queryString.fold(Map.empty[String, Seq[String]])(parseQuery)
    val contentType     = headers.getFields("Content-Type").asScala.headOption.map(_.getValue)
    val contentLength   = headers.getFields("Content-Length").asScala.headOption.map(_.getLongValue)
    val contentEncoding = headers.getFields("Content-Encoding").asScala.headOption.map(_.getValue)

    val headersMap = headers.listIterator().asScala.map{h =>
      (h.getName, h.getValueList.asScala.toSeq)
    }.toMap

    new RequestHeader(
      scheme,
      HttpMethod.valueOf(method),
      path,
      headersMap,
      requestQuery,
      contentType,
      contentLength,
      contentEncoding
    )

case class Request(header: RequestHeader, requestContent: InputStream):
  export header.*

  private def parseFormBody(body: InputStream, charset: String): Map[String, Seq[String]] =
    val stringBody = Source.fromInputStream(body, charset).mkString
    URLDecoder
      .decode(stringBody, "UTF-8")
      .split("&")
      .map(_.split("="))
      .groupBy(_(0))
      .map { case (k, v) => k -> v.map(_(1)).toSeq }

  private def parseMultipartFormData(input: InputStream, boundary: String): Map[String, Seq[Part]] =
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
    bodyCharset.fold(Source.fromInputStream(requestContent).mkString) { charset =>
      Source.fromInputStream(requestContent, charset).mkString
    }

  def jsonBody[B: JsonValueCodec]: B = readFromStream[B](requestContent)

  def formBody: Map[String, Seq[String]] = header.contentType match
    case Some("application/x-www-form-urlencoded") =>
      parseFormBody(requestContent, "UTF-8")
    case _ => Map.empty

  case class Part(headers: Map[String, String], body: String)

  def multipartBody: Map[String, Seq[Part]] = header.contentType match
    case Some(s"multipart/form-data; boundary=${boundary}") =>
      parseMultipartFormData(requestContent, boundary)
    case _ => Map.empty
