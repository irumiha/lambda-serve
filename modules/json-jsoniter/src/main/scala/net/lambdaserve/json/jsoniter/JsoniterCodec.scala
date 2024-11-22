package net.lambdaserve.json.jsoniter
import com.github.plokhotnyuk.jsoniter_scala.core.{
  JsonValueCodec,
  readFromStream,
  writeToStream
}
import net.lambdaserve.codec.{EntityDecoder, EntityEncoder}
import net.lambdaserve.http.Request
import java.io.OutputStream

object JsoniterCodec:
  given [R](using c: JsonValueCodec[R]): EntityEncoder[R] with
    def bodyWriter(responseEntity: R): OutputStream => Unit =
      os => writeToStream(responseEntity, os)

    override val contentTypeHeader: String = "application/json"

  given [R](using c: JsonValueCodec[R]): EntityDecoder[R] with
    def readBody(request: Request): R =
      readFromStream[R](request.requestContent)
