package net.lambdaserve.server.jetty

import net.lambdaserve.core.http.RequestHeader
import net.lambdaserve.core.http.RequestHeader.parseQuery
import net.lambdaserve.core.http.Util.HttpMethod
import org.eclipse.jetty.http.HttpFields

import scala.jdk.CollectionConverters.*

object RequestHeaderExtractor:
  def apply(
    scheme: String,
    method: String,
    path: String,
    headers: HttpFields,
    queryString: Option[String]
  ): RequestHeader =
    val requestQuery: () => Map[String, IndexedSeq[String]] =
      () => queryString.fold(Map.empty[String, IndexedSeq[String]])(parseQuery)

    val contentType: () => Option[String] =
      () => headers.getFields("Content-Type").asScala.headOption.map(_.getValue)

    val contentLength =
      headers.getFields("Content-Length").asScala.headOption.map(_.getLongValue)

    val contentEncoding =
      headers.getFields("Content-Encoding").asScala.headOption.map(_.getValue)

    val headersMap =
      () =>
        headers
          .listIterator()
          .asScala
          .map { h =>
            (h.getName, h.getValueList.asScala.toIndexedSeq)
          }
          .toMap

    new RequestHeader(
      scheme = scheme,
      method = HttpMethod.valueOf(method),
      path = path,
      pathParams = () => Map.empty,
      headers = headersMap,
      query = requestQuery,
      contentType = contentType,
      contentLength = contentLength,
      contentEncoding = contentEncoding
    )
