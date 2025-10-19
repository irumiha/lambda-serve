package net.lambdaserve.server.jetty

import net.lambdaserve.types.MultiMap
import org.eclipse.jetty.http.HttpFields

import scala.jdk.CollectionConverters.*

extension (fields: HttpFields)
  def toMultiMap: MultiMap = 
    val pairs = fields.asScala.flatMap { f =>
      f.getValueList.asScala.map(v => f.getName -> v)
    }
    MultiMap(pairs.toSeq: _*)
