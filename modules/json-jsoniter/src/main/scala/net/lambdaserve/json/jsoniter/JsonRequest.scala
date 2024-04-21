package net.lambdaserve.json.jsoniter

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromStream}
import net.lambdaserve.core.Route
import net.lambdaserve.core.http.Util.HttpMethod
import net.lambdaserve.core.http.{Request, Response}

import scala.util.matching.Regex

object JsonRequest:
  extension (r: Request)
    def jsonBody[B: JsonValueCodec]: B = readFromStream[B](r.requestContent)

object JsonRoute:
  import JsonRequest.*

  def POST[T: JsonValueCodec](path: Regex)(handler: T => Response): Route =
    Route(HttpMethod.POST, path, {req => handler(req.jsonBody)})

  def PUT[T: JsonValueCodec](path: Regex)(handler: T => Response): Route =
    Route(HttpMethod.PUT, path, {req => handler(req.jsonBody)})

  def PATCH[T: JsonValueCodec](path: Regex)(handler: T => Response): Route =
    Route(HttpMethod.PATCH, path, {req => handler(req.jsonBody)})
