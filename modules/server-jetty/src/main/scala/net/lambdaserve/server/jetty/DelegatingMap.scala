package net.lambdaserve.server.jetty

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
    underlying.asScala.view
      .groupBy(_.getLowerCaseName)
      .view
      .mapValues(_.map(_.getValue).toIndexedSeq)
      .toMap
      .concat(updateStore)
      .removedAll(removedKeys)
      .iterator

object DelegatingMap:
  def make(fields: HttpFields): DelegatingMap =
    DelegatingMap(fields, Map.empty, Set.empty)
