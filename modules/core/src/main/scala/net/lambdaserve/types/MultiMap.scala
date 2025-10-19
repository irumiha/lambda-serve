package net.lambdaserve.types

import scala.collection.mutable

/** A map-like collection that can store multiple values for a single key. Each
  * key is associated with a sequence of string values.
  *
  * @param underlying
  *   The backing map storing key-value pairs where values are sequences of
  *   strings
  */
class MultiMap(
  private val underlying: Map[String, IndexedSeq[String]] =
    Map.empty[String, IndexedSeq[String]]
):
  /** Retrieves all values associated with the specified key.
    *
    * @param key
    *   The key to look up
    * @return
    *   The sequence of values associated with the key
    * @throws NoSuchElementException
    *   if the key does not exist in the map
    */
  def apply(key: String): Seq[String] = underlying(key)

  /** Removes the specified key and its associated values from the map.
    *
    * @param key
    *   The key to remove
    * @return
    *   A new MultiMap without the specified key
    */
  def remove(key: String): MultiMap =
    new MultiMap(underlying.removed(key))

  /** Updates the value associated with the specified key.
    *
    * @param key
    *   The key to update
    * @param value
    *   The value to associate with the key
    * @return
    *   A new MultiMap with the updated key-value pair
    */
  def update(key: String, value: String): MultiMap =
    underlying.get(key) match
      case Some(values) =>
        new MultiMap(underlying.updated(key, values :+ value))
      case None => new MultiMap(underlying.updated(key, IndexedSeq(value)))

  /** Combines this MultiMap with another MultiMap by adding all key-value pairs
    * from the other map.
    *
    * @param other
    *   The MultiMap to combine with this one
    * @return
    *   A new MultiMap containing all key-value pairs from both maps
    */
  def extend(other: MultiMap): MultiMap =
    other.iterator.foldLeft(this) { case (map, (key, value)) =>
      map.update(key, value)
    }

  /** Retrieves the sequence of values associated with the specified key.
    *
    * @param key
    *   The key to look up
    * @return
    *   Some sequence of values if the key exists, None otherwise
    */
  def get(key: String): Seq[String] =
    underlying.getOrElse(key, Seq())

  /** Retrieves the first value associated with the specified key from the map
    * or returns the provided default value if the key does not exist or has no
    * associated values.
    *
    * @param key
    *   The key to look up in the map.
    * @param default
    *   The default value to return if the key is not present, or if it has no
    *   associated values.
    * @return
    *   The first value associated with the key, or the default value if no
    *   value is found.
    */
  def get(key: String, default: String): String =
    underlying.get(key).flatMap(_.headOption).getOrElse(default)

  /** Returns an iterator over all key-value pairs, where each value in the
    * sequence is paired with its key individually.
    *
    * @return
    *   An iterator over (key, value) pairs
    */
  def iterator: Iterator[(String, String)] =
    underlying.iterator.flatMap { case (key, values) =>
      values.map(key -> _)
    }

  /** Maps each key-value pair to a value of type B using the provided function.
    *
    * @param f
    *   The mapping function to apply to each key-value pair
    * @return
    *   A sequence containing the mapped values
    */
  def map[B](f: (String, String) => B): Seq[B] =
    iterator.map(f.tupled).toSeq

  /** Applies the given function to each key-value pair in the map.
    *
    * @param f
    *   The function to apply to each key-value pair
    */
  def forEach(f: (String, String) => Unit): Unit =
    iterator.foreach(f.tupled)

  /** Checks if the MultiMap contains no key-value pairs.
    *
    * @return
    *   true if the map is empty, false otherwise
    */
  def isEmpty: Boolean = underlying.isEmpty

  /** Checks if the MultiMap contains the specified key.
    *
    * @param key
    *   The key to check
    * @return
    *   true if the key exists in the map, false otherwise
    */
  def contains(key: String): Boolean = underlying.contains(key)

  /** Returns the number of distinct keys in the MultiMap.
    *
    * @return
    *   The number of keys in the map
    */
  def size: Int = underlying.size

  /** Returns the underlying Map representation of this MultiMap.
    *
    * @return
    *   The raw Map containing all key-value pairs
    */
  def toRawMap: Map[String, IndexedSeq[String]] = underlying

object MultiMap:
  /** Creates a new MultiMap from a sequence of key-value pairs.
    *
    * @param kv
    *   A variable number of key-value tuples to initialize the map with
    * @return
    *   A new MultiMap containing all the provided key-value pairs
    */
  def apply(kv: (String, String)*): MultiMap =
    val initial =
      mutable.Map
        .empty[String, IndexedSeq[String]]
        .withDefaultValue(IndexedSeq.empty)

    for (key, value) <- kv do initial(key) = initial(key) :+ value

    new MultiMap(initial.toMap)
