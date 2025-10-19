package net.lambdaserve.http

import net.lambdaserve.types.MultiMap

import java.io.InputStream

case class MultiPart(
  name: Option[String],
  fileName: Option[String],
  headers: MultiMap,
  content: InputStream
)
