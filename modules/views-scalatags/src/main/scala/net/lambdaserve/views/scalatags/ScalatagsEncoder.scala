package net.lambdaserve.views.scalatags
import net.lambdaserve.core.codec.EntityEncoder
import scalatags.text.Frag

import java.io.OutputStream

object ScalatagsEncoder:
  given tagEncoder[T <: Frag]: EntityEncoder[T] with
    def bodyWriter(responseEntity: T): OutputStream => Unit =
      os =>
        val finalHtml = "<!DOCTYPE html>\n" + responseEntity.render
        os.write(finalHtml.getBytes)

    override def contentTypeHeader: String = "text/html"
