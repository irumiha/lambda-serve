package net.lambdaserve.json.jsoniter
import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, writeToStreamReentrant}
import net.lambdaserve.core.http.Util.{HttpHeader, HttpStatus}
import net.lambdaserve.core.http.{Response, ResponseHeader}

object JsonResponse:
  def Ok[R: JsonValueCodec](value: R): Response =
    Ok(value, Map.empty)

  def Ok[R: JsonValueCodec](
    value: R,
    headers: Map[String, Seq[String]]
  ): Response =
    Response(
      ResponseHeader(HttpStatus.OK, headers + (HttpHeader.ContentType.name -> Seq("application/json"))),
      os => writeToStreamReentrant(value, os),
      None
    )
