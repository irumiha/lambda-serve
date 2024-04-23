package net.lambdaserve.json.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromStream}
import net.lambdaserve.core.codec.EntityDecoder
import net.lambdaserve.core.http.Request

object JsoniterDecoder:
  given [R](using c: JsonValueCodec[R]): EntityDecoder[R] with
    def readBody(request: Request): R = readFromStream[R](request.requestContent)
