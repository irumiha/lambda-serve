package net.lambdaserve.requestmapped.json

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import net.lambdaserve.core.http.{Request, Response}
import net.lambdaserve.mapextract.MapExtract
import net.lambdaserve.json.jsoniter.JsonRequest.*

def joinMaps(r: Request) = r.pathParams ++ r.header.query ++ r.form ++ r.headers


