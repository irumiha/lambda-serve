package net.lambdaserve.json.jsoniter
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, writeToStringReentrant}
import net.lambdaserve.core.http.Response
import net.lambdaserve.core.http.Response.Ok
import net.lambdaserve.core.http.Util.HttpHeader

import java.nio.charset.Charset

object JsonResponse:
    def OkJson[R: JsonValueCodec](value: R): Response =
      Ok(
        writeToStringReentrant(value),
        Charset.defaultCharset(),
        Map(HttpHeader.ContentType.name -> Seq("application/json"))
      )

    def OkJson[R: JsonValueCodec](value: R, headers: Map[String, Seq[String]]): Response =
      Ok(
        writeToStringReentrant(value),
        Charset.defaultCharset(),
        headers + (HttpHeader.ContentType.name -> Seq("application/json"))
      )
