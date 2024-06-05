package net.lambdaserve.requestmapped

import net.lambdaserve.core.RouteHandler
import net.lambdaserve.core.codec.EntityDecoder
import net.lambdaserve.core.http.{Request, Response}
import net.lambdaserve.mapextract.MapExtract

type Combined[R] = MapExtract[R] | EntityDecoder[R]

extension (request: Request)
  inline def mapFromRequest[T: MapExtract]: T =
    val m = summon[MapExtract[T]]
    m.projectMaps(
      Seq(
        request.pathParams(),
        request.query(),
        request.form,
        request.headers()
      )
    )

  inline def bodyAs[T: EntityDecoder]: T =
    val d = summon[EntityDecoder[T]]
    d.readBody(request)

def map1[T](request: Request, m: Combined[T]) =
  val handlerParam = m match
    case me: MapExtract[T] =>
      me.projectMaps(
        Seq(
          request.pathParams(),
          request.query(),
          request.form,
          request.headers()
        )
      )
    case jvc: EntityDecoder[T] => jvc.readBody(request)
  handlerParam

extension [T](handler: T => Response)(using m: Combined[T])
  inline def mapped(request: Request): Response =
    val handlerParam: T = map1(request, m)
    handler(handlerParam)

def mapped[T: Combined](h: T => Response): RouteHandler = h.mapped

extension [T](handler: (T, Request) => Response)(using m: Combined[T])
  inline def mapped(request: Request): Response =
    val handlerParam = map1(request, m)
    handler(handlerParam, request)

def mapped[T: Combined](h: (T, Request) => Response): RouteHandler = h.mapped

extension [T1, T2](
  h: (T1, T2) => Response
)(using m1: Combined[T1], m2: Combined[T2])
  inline def mapped(request: Request): Response =
    val p1 = map1(request, m1)
    val p2 = map1(request, m2)

    h(p1, p2)

def mapped[T1: Combined, T2: Combined](h: (T1, T2) => Response): RouteHandler =
  h.mapped

extension [T1, T2](
  h: (T1, T2, Request) => Response
)(using m1: Combined[T1], m2: Combined[T2])
  inline def mapped(request: Request): Response =
    val p1 = map1(request, m1)
    val p2 = map1(request, m2)

    h(p1, p2, request)

def mapped[T1: Combined, T2: Combined](
  h: (T1, T2, Request) => Response
): RouteHandler =
  h.mapped

extension [T1, T2, T3](
  h: (T1, T2, T3) => Response
)(using m1: Combined[T1], m2: Combined[T2], m3: Combined[T3])
  inline def mapped(request: Request): Response =
    val p1 = map1(request, m1)
    val p2 = map1(request, m2)
    val p3 = map1(request, m3)

    h(p1, p2, p3)

def mapped[T1: Combined, T2: Combined, T3: Combined](
  h: (T1, T2, T3) => Response
) =
  h.mapped

extension [T1, T2, T3](
  h: (T1, T2, T3, Request) => Response
)(using m1: Combined[T1], m2: Combined[T2], m3: Combined[T3])
  inline def mapped(request: Request): Response =
    val p1 = map1(request, m1)
    val p2 = map1(request, m2)
    val p3 = map1(request, m3)

    h(p1, p2, p3, request)

def mapped[T1: Combined, T2: Combined, T3: Combined](
  h: (T1, T2, T3, Request) => Response
): RouteHandler =
  h.mapped
