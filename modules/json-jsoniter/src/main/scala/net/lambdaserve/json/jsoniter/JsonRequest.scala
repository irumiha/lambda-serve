package net.lambdaserve.json.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromStream}
import net.lambdaserve.core.http.Request

object JsonRequest {
  extension (r: Request)
    def jsonBody[B: JsonValueCodec]: B = readFromStream[B](r.requestContent)
}
