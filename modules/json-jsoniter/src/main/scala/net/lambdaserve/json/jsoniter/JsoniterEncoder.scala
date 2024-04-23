package net.lambdaserve.json.jsoniter
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, writeToStream}
import net.lambdaserve.core.codec.EntityEncoder

import java.io.OutputStream

object JsoniterEncoder:
  given [R](using c: JsonValueCodec[R]): EntityEncoder[R] with
    def bodyWriter(responseEntity: R): OutputStream => Unit =
      os => writeToStream(responseEntity, os)
