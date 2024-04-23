package net.lambdaserve.json.jsoniter
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, writeToStream}
import net.lambdaserve.core.codec.JsonEncoder

import java.io.OutputStream

object JsoniterEncoder:
  given [R](using c: JsonValueCodec[R]): JsonEncoder[R] with
    def bodyWriter(responseEntity: R): OutputStream => Unit =
      os => writeToStream(responseEntity, os)
