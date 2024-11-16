package net.lambdaserve.http

import java.io.InputStream

case class MultiPart(
  name: Option[String],
  fileName: Option[String],
  headers: Map[String, IndexedSeq[String]],
  content: InputStream
)
