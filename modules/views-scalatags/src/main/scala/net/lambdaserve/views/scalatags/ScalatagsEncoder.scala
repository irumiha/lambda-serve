package net.lambdaserve.views.scalatags

import net.lambdaserve.codec.EntityEncoder
import scalatags.text.Frag

import java.io.OutputStream

trait ScalatagsEncoder:
  private val documentPreamble: Array[Byte] = "<!DOCTYPE html>\n".getBytes()
  val contentType = "text/html; charset=UTF-8"

  given tagEncoder[T <: Frag]: EntityEncoder[T] with
    def bodyWriter(responseEntity: T): OutputStream => Unit =
      os =>
        os.write(documentPreamble)
        responseEntity.writeBytesTo(os)

    override val contentTypeHeader: String = contentType

object ScalatagsEncoder extends ScalatagsEncoder
