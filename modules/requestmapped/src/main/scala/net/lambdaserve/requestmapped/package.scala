package net.lambdaserve.requestmapped

import net.lambdaserve.core.RouteHandler
import net.lambdaserve.core.codec.EntityDecoder
import net.lambdaserve.core.http.{Request, Response}
import net.lambdaserve.mapextract.MapExtract

def joinMaps(r: Request) = r.pathParams ++ r.header.query ++ r.form ++ r.headers

type Combined[R] = MapExtract[R] | EntityDecoder[R]

def mapped[T](h: T => Response)(using
  m: Combined[T]
): RouteHandler =
  request =>
    val handlerParam = m match
      case me: MapExtract[T]     => me.projectMap(joinMaps(request))
      case jvc: EntityDecoder[T] => jvc.readBody(request)
    h(handlerParam)

def mapped[T](h: (T, Request) => Response)(using
  m: Combined[T]
): RouteHandler =
  request =>
    val handlerParam = m match
      case me: MapExtract[T]     => me.projectMap(joinMaps(request))
      case jvc: EntityDecoder[T] => jvc.readBody(request)
    h(handlerParam, request)    

def mapped[T1, T2](
  h: (T1, T2) => Response
)(using m1: Combined[T1], m2: Combined[T2]): RouteHandler =
  request =>
    val joinedMaps = joinMaps(request)
    
    val p1 = m1 match
      case me: MapExtract[T1]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T1] => jvc.readBody(request)
    
    val p2         = m2 match
      case me: MapExtract[T2]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T2] => jvc.readBody(request)

    h(p1, p2)

def mapped[T1, T2](
  h: (T1, T2, Request) => Response
)(using m1: Combined[T1], m2: Combined[T2]): RouteHandler =
  request =>
    val joinedMaps = joinMaps(request)
    
    val p1 = m1 match
      case me: MapExtract[T1]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T1] => jvc.readBody(request)
    
    val p2         = m2 match
      case me: MapExtract[T2]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T2] => jvc.readBody(request)

    h(p1, p2, request)

def mapped[T1, T2, T3](h: (T1, T2, T3) => Response)(using
 m1: Combined[T1], 
 m2: Combined[T2], 
 m3: Combined[T3]
): RouteHandler =
  request =>
    val joinedMaps = joinMaps(request)
    
    val p1 = m1 match
      case me: MapExtract[T1]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T1] => jvc.readBody(request)
    
    val p2         = m2 match
      case me: MapExtract[T2]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T2] => jvc.readBody(request)

    val p3         = m3 match
      case me: MapExtract[T3]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T3] => jvc.readBody(request)

    h(p1, p2, p3)

def mapped[T1, T2, T3](h: (T1, T2, T3, Request) => Response)(using
 m1: Combined[T1], 
 m2: Combined[T2], 
 m3: Combined[T3]
): RouteHandler =
  request =>
    val joinedMaps = joinMaps(request)
    val p1 = m1 match
      case me: MapExtract[T1]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T1] => jvc.readBody(request)
    
    val p2         = m2 match
      case me: MapExtract[T2]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T2] => jvc.readBody(request)

    val p3         = m3 match
      case me: MapExtract[T3]     => me.projectMap(joinedMaps)
      case jvc: EntityDecoder[T3] => jvc.readBody(request)
    h(p1, p2, p3, request)
