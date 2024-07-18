package net.lambdaserve.core.codec

import java.io.OutputStream

trait EntityEncoder[R]:
  def bodyWriter(responseEntity: R): OutputStream => Unit
  def contentTypeHeader: String

object EntityEncoder:
  given stringEncoder: EntityEncoder[String] with
    override def bodyWriter(responseEntity: String): OutputStream => Unit =
      os => os.write(responseEntity.getBytes)

    override val contentTypeHeader: String = "text/plain; charset=utf-8"
