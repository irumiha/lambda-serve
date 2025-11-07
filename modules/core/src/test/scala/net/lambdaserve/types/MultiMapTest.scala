package net.lambdaserve.types

import munit.FunSuite

class MultiMapTest extends FunSuite:

  test("MultiMap.apply creates empty map from no arguments"):
    val map = MultiMap()

    assertEquals(map.get("key"), Seq())

  test("MultiMap.apply creates map with single key-value pair"):
    val map = MultiMap("key" -> "value")

    assertEquals(map.get("key"), Seq("value"))

  test("MultiMap.apply creates map with multiple different keys"):
    val map = MultiMap("key1" -> "value1", "key2" -> "value2")

    assertEquals(map.get("key1"), Seq("value1"))
    assertEquals(map.get("key2"), Seq("value2"))

  test("MultiMap.apply accumulates multiple values for same key"):
    val map = MultiMap("key" -> "value1", "key" -> "value2", "key" -> "value3")

    assertEquals(map.get("key"), Seq("value1", "value2", "value3"))

  test("MultiMap.apply handles mix of single and multiple values per key"):
    val map = MultiMap(
      "single"  -> "value",
      "multi"   -> "value1",
      "multi"   -> "value2",
      "another" -> "value3"
    )

    assertEquals(map.get("single"), Seq("value"))
    assertEquals(map.get("multi"), Seq("value1", "value2"))
    assertEquals(map.get("another"), Seq("value3"))

  test("MultiMap get returns Seq() for missing key"):
    val map = MultiMap("key" -> "value")

    assertEquals(map.get("missing"), Seq())

  test("MultiMap get returns Some with values for existing key"):
    val map = MultiMap("key" -> "value1", "key" -> "value2")

    assertEquals(map.get("key"), Seq("value1", "value2"))

  test("MultiMap remove returns new map without specified key"):
    val map     = MultiMap("key1" -> "value1", "key2" -> "value2")
    val updated = map.remove("key1")

    assertEquals(updated.get("key1"), Seq())
    assertEquals(updated.get("key2"), Seq("value2"))

  test("MultiMap remove does not modify original map"):
    val original = MultiMap("key" -> "value")
    val updated  = original.remove("key")

    assertEquals(original.get("key"), Seq("value"))
    assertEquals(updated.get("key"), Seq())

  test("MultiMap remove on non-existent key returns unchanged map"):
    val map     = MultiMap("key" -> "value")
    val updated = map.remove("missing")

    assertEquals(updated.get("key"), Seq("value"))

  test("MultiMap remove on empty map returns empty map"):
    val map     = MultiMap()
    val updated = map.remove("key")

    assertEquals(updated.get("key"), Seq())

  test("MultiMap update appends value to existing key"):
    val map     = MultiMap("key" -> "value1", "key" -> "value2")
    val updated = map.update("key", "value3")

    assertEquals(updated.get("key"), Seq("value1", "value2", "value3"))

  test("MultiMap update adds new key-value pair"):
    val map     = MultiMap("existing" -> "value")
    val updated = map.update("new", "value")

    assertEquals(map.get("new"), Seq())
    assertEquals(updated.get("new"), Seq("value"))

  test("MultiMap update does not modify original map"):
    val original = MultiMap("key" -> "value")
    val updated  = original.update("key", "newValue")

    assertEquals(original.get("key"), Seq("value"))

  test("MultiMap iterator yields all key-value pairs"):
    val map   = MultiMap("key1" -> "value1", "key2" -> "value2")
    val pairs = map.iterator.toSeq

    assertEquals(pairs.length, 2)
    assert(pairs.contains("key1" -> "value1"))
    assert(pairs.contains("key2" -> "value2"))

  test("MultiMap iterator expands multiple values for same key"):
    val map = MultiMap("key" -> "value1", "key" -> "value2", "key" -> "value3")
    val pairs = map.iterator.toSeq

    assertEquals(pairs.length, 3)
    assert(pairs.contains("key" -> "value1"))
    assert(pairs.contains("key" -> "value2"))
    assert(pairs.contains("key" -> "value3"))

  test("MultiMap iterator on empty map yields no pairs"):
    val map   = MultiMap()
    val pairs = map.iterator.toSeq

    assertEquals(pairs.length, 0)

  test("MultiMap map transforms key-value pairs"):
    val map    = MultiMap("key1" -> "value1", "key2" -> "value2")
    val result = map.map((k, v) => s"$k=$v")

    assertEquals(result.length, 2)
    assert(result.contains("key1=value1"))
    assert(result.contains("key2=value2"))

  test("MultiMap map handles multiple values per key"):
    val map    = MultiMap("key" -> "a", "key" -> "b")
    val result = map.map((k, v) => s"$k:$v")

    assertEquals(result.length, 2)
    assert(result.contains("key:a"))
    assert(result.contains("key:b"))

  test("MultiMap map can transform to different types"):
    val map    = MultiMap("key1" -> "1", "key2" -> "2")
    val result = map.map((k, v) => v.toInt)

    assertEquals(result, Seq(1, 2))

  test("MultiMap map on empty map returns empty sequence"):
    val map    = MultiMap()
    val result = map.map((k, v) => s"$k=$v")

    assertEquals(result, Seq.empty)

  test("MultiMap forEach applies function to all pairs"):
    val map   = MultiMap("key1" -> "value1", "key2" -> "value2")
    var count = 0
    var pairs = List.empty[(String, String)]

    map.forEach { (k, v) =>
      count += 1
      pairs = pairs :+ (k -> v)
    }

    assertEquals(count, 2)
    assert(pairs.contains("key1" -> "value1"))
    assert(pairs.contains("key2" -> "value2"))

  test("MultiMap forEach handles multiple values per key"):
    val map   = MultiMap("key" -> "a", "key" -> "b", "key" -> "c")
    var count = 0

    map.forEach { (k, v) =>
      count += 1
    }

    assertEquals(count, 3)

  test("MultiMap forEach on empty map does not execute function"):
    val map      = MultiMap()
    var executed = false

    map.forEach { (k, v) =>
      executed = true
    }

    assertEquals(executed, false)

  test("MultiMap handles empty string keys"):
    val map = MultiMap("" -> "value")

    assertEquals(map.get(""), Seq("value"))

  test("MultiMap handles empty string values"):
    val map = MultiMap("key" -> "")

    assertEquals(map.get("key"), Seq(""))

  test("MultiMap handles special characters in keys"):
    val map =
      MultiMap("key with spaces" -> "value", "key-with-dashes" -> "value2")

    assertEquals(map.get("key with spaces"), Seq("value"))
    assertEquals(map.get("key-with-dashes"), Seq("value2"))

  test("MultiMap handles special characters in values"):
    val map =
      MultiMap("key" -> "value with spaces", "key2" -> "value-with-dashes")

    assertEquals(map.get("key"), Seq("value with spaces"))
    assertEquals(map.get("key2"), Seq("value-with-dashes"))

  test("MultiMap preserves order of values for same key"):
    val map = MultiMap("key" -> "first", "key" -> "second", "key" -> "third")

    assertEquals(map.get("key"), Seq("first", "second", "third"))

  test("MultiMap chaining remove operations"):
    val map =
      MultiMap("key1" -> "value1", "key2" -> "value2", "key3" -> "value3")
    val updated = map.remove("key1").remove("key2")

    assertEquals(updated.get("key1"), Seq())
    assertEquals(updated.get("key2"), Seq())
    assertEquals(updated.get("key3"), Seq("value3"))

  test("MultiMap chaining update operations"):
    val map = MultiMap("key" -> "value1")
      .update("key", "value2")
      .update("key", "value3")

    assertEquals(map.get("key"), Seq("value1", "value2", "value3"))

  test("MultiMap update on empty map creates single-element sequence"):
    val map     = MultiMap()
    val updated = map.update("key", "value")

    assertEquals(updated.get("key"), Seq("value"))

  test("MultiMap complex workflow with multiple operations"):
    val map = MultiMap("a" -> "1", "b" -> "2", "c" -> "3")
      .remove("b")
      .update("d", "4")
      .update("a", "2")

    assertEquals(map.get("a"), Seq("1", "2"))
    assertEquals(map.get("b"), Seq())
    assertEquals(map.get("c"), Seq("3"))
    assertEquals(map.get("d"), Seq("4"))

  test("MultiMap apply returns values for existing key"):
    val map = MultiMap("key" -> "value1", "key" -> "value2")

    assertEquals(map("key"), Seq("value1", "value2"))

  test("MultiMap apply throws exception for missing key"):
    val map = MultiMap("key" -> "value")

    intercept[NoSuchElementException] {
      map("missing")
    }

  test("MultiMap isEmpty returns true for empty map"):
    val map = MultiMap()

    assertEquals(map.isEmpty, true)

  test("MultiMap isEmpty returns false for non-empty map"):
    val map = MultiMap("key" -> "value")

    assertEquals(map.isEmpty, false)

  test("MultiMap isEmpty returns true after removing all keys"):
    val map   = MultiMap("key" -> "value")
    val empty = map.remove("key")

    assertEquals(empty.isEmpty, true)

  test("MultiMap contains returns true for existing key"):
    val map = MultiMap("key1" -> "value1", "key2" -> "value2")

    assertEquals(map.contains("key1"), true)
    assertEquals(map.contains("key2"), true)

  test("MultiMap contains returns false for missing key"):
    val map = MultiMap("key" -> "value")

    assertEquals(map.contains("missing"), false)

  test("MultiMap contains returns false for removed key"):
    val map     = MultiMap("key" -> "value")
    val updated = map.remove("key")

    assertEquals(updated.contains("key"), false)

  test("MultiMap contains returns true after update"):
    val map     = MultiMap()
    val updated = map.update("key", "value")

    assertEquals(updated.contains("key"), true)

  test("MultiMap size returns 0 for empty map"):
    val map = MultiMap()

    assertEquals(map.size, 0)

  test("MultiMap size returns number of distinct keys"):
    val map =
      MultiMap("key1" -> "value1", "key2" -> "value2", "key3" -> "value3")

    assertEquals(map.size, 3)

  test("MultiMap size counts keys with multiple values as one"):
    val map = MultiMap("key" -> "value1", "key" -> "value2", "key" -> "value3")

    assertEquals(map.size, 1)

  test("MultiMap size decreases when key is removed"):
    val map     = MultiMap("key1" -> "value1", "key2" -> "value2")
    val updated = map.remove("key1")

    assertEquals(updated.size, 1)

  test("MultiMap size increases when new key is added"):
    val map     = MultiMap("key1" -> "value1")
    val updated = map.update("key2", "value2")

    assertEquals(updated.size, 2)

  test("MultiMap size does not increase when updating existing key"):
    val map     = MultiMap("key" -> "value1")
    val updated = map.update("key", "value2")

    assertEquals(updated.size, 1)

  test("MultiMap toRawMap returns underlying map structure"):
    val map = MultiMap("key1" -> "value1", "key2" -> "value2")
    val raw = map.toRawMap

    assertEquals(raw("key1"), IndexedSeq("value1"))
    assertEquals(raw("key2"), IndexedSeq("value2"))

  test("MultiMap toRawMap preserves multiple values"):
    val map = MultiMap("key" -> "value1", "key" -> "value2", "key" -> "value3")
    val raw = map.toRawMap

    assertEquals(raw("key"), IndexedSeq("value1", "value2", "value3"))

  test("MultiMap toRawMap returns empty map for empty MultiMap"):
    val map = MultiMap()
    val raw = map.toRawMap

    assertEquals(raw, Map.empty)

  test("MultiMap toRawMap is immutable snapshot"):
    val map     = MultiMap("key" -> "value1")
    val raw1    = map.toRawMap
    val updated = map.update("key", "value2")
    val raw2    = updated.toRawMap

    assertEquals(raw1("key"), IndexedSeq("value1"))
    assertEquals(raw2("key"), IndexedSeq("value1", "value2"))

  test("MultiMap apply with contains check pattern"):
    val map = MultiMap("key1" -> "value1", "key2" -> "value2")

    if map.contains("key1") then assertEquals(map("key1"), Seq("value1"))

    assertEquals(map.get("key3"), Seq())

  test("MultiMap isEmpty with size zero equivalence"):
    val emptyMap    = MultiMap()
    val nonEmptyMap = MultiMap("key" -> "value")

    assertEquals(emptyMap.isEmpty, emptyMap.size == 0)
    assertEquals(nonEmptyMap.isEmpty, nonEmptyMap.size == 0)

  test("MultiMap toRawMap roundtrip preserves structure"):
    val original = MultiMap(
      "a" -> "1",
      "a" -> "2",
      "b" -> "3",
      "c" -> "4",
      "c" -> "5",
      "c" -> "6"
    )
    val raw = original.toRawMap

    assertEquals(raw.size, 3)
    assertEquals(raw("a").length, 2)
    assertEquals(raw("b").length, 1)
    assertEquals(raw("c").length, 3)

  test("MultiMap extend combines two maps with different keys"):
    val map1     = MultiMap("a" -> "1", "b" -> "2")
    val map2     = MultiMap("c" -> "3", "d" -> "4")
    val combined = map1.extend(map2)

    assertEquals(combined.get("a"), Seq("1"))
    assertEquals(combined.get("b"), Seq("2"))
    assertEquals(combined.get("c"), Seq("3"))
    assertEquals(combined.get("d"), Seq("4"))
    assertEquals(combined.size, 4)

  test("MultiMap extend appends values for overlapping keys"):
    val map1     = MultiMap("a" -> "1", "b" -> "2")
    val map2     = MultiMap("a" -> "3", "b" -> "4")
    val combined = map1.extend(map2)

    assertEquals(combined.get("a"), Seq("1", "3"))
    assertEquals(combined.get("b"), Seq("2", "4"))
    assertEquals(combined.size, 2)

  test("MultiMap extend with empty map returns original"):
    val map      = MultiMap("a" -> "1", "b" -> "2")
    val empty    = MultiMap()
    val combined = map.extend(empty)

    assertEquals(combined.get("a"), Seq("1"))
    assertEquals(combined.get("b"), Seq("2"))
    assertEquals(combined.size, 2)

  test("MultiMap extend empty map with non-empty returns other"):
    val empty    = MultiMap()
    val map      = MultiMap("a" -> "1", "b" -> "2")
    val combined = empty.extend(map)

    assertEquals(combined.get("a"), Seq("1"))
    assertEquals(combined.get("b"), Seq("2"))
    assertEquals(combined.size, 2)

  test("MultiMap extend with multiple values per key"):
    val map1     = MultiMap("a" -> "1", "a" -> "2")
    val map2     = MultiMap("a" -> "3", "a" -> "4")
    val combined = map1.extend(map2)

    assertEquals(combined.get("a"), Seq("1", "2", "3", "4"))
    assertEquals(combined.size, 1)

  test("MultiMap extend preserves value order"):
    val map1     = MultiMap("key" -> "first", "key" -> "second")
    val map2     = MultiMap("key" -> "third", "key" -> "fourth")
    val combined = map1.extend(map2)

    assertEquals(combined.get("key"), Seq("first", "second", "third", "fourth"))

  test("MultiMap extend does not modify original maps"):
    val map1     = MultiMap("a" -> "1")
    val map2     = MultiMap("b" -> "2")
    val combined = map1.extend(map2)

    assertEquals(map1.get("a"), Seq("1"))
    assertEquals(map1.get("b"), Seq())
    assertEquals(map2.get("a"), Seq())
    assertEquals(map2.get("b"), Seq("2"))
    assertEquals(combined.get("a"), Seq("1"))
    assertEquals(combined.get("b"), Seq("2"))

  test("MultiMap extend can be chained"):
    val map1     = MultiMap("a" -> "1")
    val map2     = MultiMap("b" -> "2")
    val map3     = MultiMap("c" -> "3")
    val combined = map1.extend(map2).extend(map3)

    assertEquals(combined.get("a"), Seq("1"))
    assertEquals(combined.get("b"), Seq("2"))
    assertEquals(combined.get("c"), Seq("3"))
    assertEquals(combined.size, 3)

  test("MultiMap extend with complex overlapping scenario"):
    val map1     = MultiMap("shared" -> "v1", "shared" -> "v2", "only1" -> "x")
    val map2     = MultiMap("shared" -> "v3", "only2" -> "y", "only2" -> "z")
    val combined = map1.extend(map2)

    assertEquals(combined.get("shared"), Seq("v1", "v2", "v3"))
    assertEquals(combined.get("only1"), Seq("x"))
    assertEquals(combined.get("only2"), Seq("y", "z"))
    assertEquals(combined.size, 3)

  test("MultiMap extend both maps empty"):
    val empty1   = MultiMap()
    val empty2   = MultiMap()
    val combined = empty1.extend(empty2)

    assertEquals(combined.isEmpty, true)
    assertEquals(combined.size, 0)

  test("MultiMap extend with special characters in keys and values"):
    val map1     = MultiMap("key with spaces" -> "value 1")
    val map2     = MultiMap("key-with-dashes" -> "value-2")
    val combined = map1.extend(map2)

    assertEquals(combined.get("key with spaces"), Seq("value 1"))
    assertEquals(combined.get("key-with-dashes"), Seq("value-2"))

  test("MultiMap extend merging headers use case"):
    val defaultHeaders =
      MultiMap("User-Agent" -> "MyApp/1.0", "Accept" -> "application/json")
    val requestHeaders =
      MultiMap("Authorization" -> "Bearer token123", "Accept" -> "text/html")
    val allHeaders = defaultHeaders.extend(requestHeaders)

    assertEquals(allHeaders.get("User-Agent"), Seq("MyApp/1.0"))
    assertEquals(allHeaders.get("Accept"), Seq("application/json", "text/html"))
    assertEquals(allHeaders.get("Authorization"), Seq("Bearer token123"))

end MultiMapTest
