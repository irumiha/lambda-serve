package net.lambdaserve.server.jetty

import munit.FunSuite
import org.eclipse.jetty.http.{HttpField, HttpFields}

import java.util

class DelegatingMapTest extends FunSuite:

  private val hf = new HttpFields:
    override def listIterator(index: Int): util.ListIterator[HttpField] =
      util.Collections.emptyListIterator[HttpField]

  test("test Get on empty HttpFields"):
    val map = DelegatingMap.make(hf)
    assertEquals(map.get("key"), None)

  test("test Get on HttpFields with one value"):
    val hf = new HttpFields:
      override def listIterator(index: Int): util.ListIterator[HttpField] =
        util.Collections
          .singletonList(new HttpField("key", "value"))
          .listIterator()

    val map = DelegatingMap.make(hf)
    assertEquals(map.get("key"), Some(Vector("value")))
