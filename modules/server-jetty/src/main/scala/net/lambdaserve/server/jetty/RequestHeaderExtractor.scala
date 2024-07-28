package net.lambdaserve.server.jetty

import net.lambdaserve.core.http.RequestHeader
import net.lambdaserve.core.http.RequestHeader.parseQuery
import net.lambdaserve.core.http.Method
import org.eclipse.jetty.http.HttpFields

import scala.jdk.CollectionConverters.*

class DelegatingMap private (
  underlying: HttpFields,
  updateStore: Map[String, IndexedSeq[String]],
  removedKeys: Set[String]
) extends Map[String, IndexedSeq[String]]:

  override def removed(key: String): Map[String, IndexedSeq[String]] =
    DelegatingMap(underlying, updateStore, removedKeys + key)

  override def updated[V1 >: IndexedSeq[String]](
    key: String,
    value: V1
  ): Map[String, IndexedSeq[String]] =
    val newRemoved = removedKeys - key
    val newUpdated: Map[String, IndexedSeq[String]] =
      updateStore + (key -> value.asInstanceOf[IndexedSeq[String]])

    DelegatingMap(underlying, newUpdated, newRemoved)

  override def get(key: String): Option[IndexedSeq[String]] =
    if removedKeys.contains(key) then None
    else if updateStore.contains(key) then Some(updateStore(key))
    else if underlying.contains(key) then
      Some(underlying.getFields(key).asScala.map(_.getValue).toIndexedSeq)
    else None

  override def iterator: Iterator[(String, IndexedSeq[String])] =
    underlying
      .asScala
      .view
      .groupBy(_.getLowerCaseName)
      .map(pair => pair._1 -> pair._2.map(_.getValue).toIndexedSeq)
      .concat(updateStore)
      .removedAll(removedKeys)
      .iterator

object DelegatingMap:
  def make(fields: HttpFields): DelegatingMap =
    DelegatingMap(fields, Map.empty, Set.empty)

object RequestHeaderExtractor:
  def apply(
    scheme: String,
    method: String,
    path: String,
    headers: HttpFields,
    queryString: Option[String]
  ): RequestHeader =
    val requestQuery: Map[String, IndexedSeq[String]] =
      queryString.fold(Map.empty[String, IndexedSeq[String]])(parseQuery)

    val contentType =
      headers.getFields("Content-Type").asScala.headOption.map(_.getValue)

    val contentLength =
      headers.getFields("Content-Length").asScala.headOption.map(_.getLongValue)

    val contentEncoding =
      headers.getFields("Content-Encoding").asScala.headOption.map(_.getValue)

    val headersMap: Map[String, IndexedSeq[String]] =
      DelegatingMap.make(headers)

    RequestHeader(
      scheme = scheme,
      method = Method.valueOf(method),
      path = path,
      pathParams = Map.empty,
      headers = headersMap,
      query = requestQuery,
      contentType = contentType,
      contentLength = contentLength,
      contentEncoding = contentEncoding
    )
