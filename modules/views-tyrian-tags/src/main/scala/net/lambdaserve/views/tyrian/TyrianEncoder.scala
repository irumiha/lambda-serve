package net.lambdaserve.views.tyrian

import net.lambdaserve.codec.EntityEncoder
import tyrian.Elem

import java.io.OutputStream
import java.nio.charset.StandardCharsets

trait TyrianEncoder:
  private val documentPreamble: Array[Byte] = "<!DOCTYPE html>\n".getBytes()
  val contentType = "text/html; charset=UTF-8"

  given tagEncoder[T <: Elem[?]]: EntityEncoder[T] with
    def bodyWriter(responseEntity: T): OutputStream => Unit =
      os =>
        os.write(documentPreamble)
        val content = responseEntity.toString()
        os.write(content.getBytes(StandardCharsets.UTF_8))
        os.flush()
        os.close()

    override val contentTypeHeader: String = contentType

object TyrianEncoder extends TyrianEncoder
