package net.lambdaserve.requestmapped

import net.lambdaserve.core.http.{Request, Response}
import net.lambdaserve.mapextract.MapExtract

def joinMaps(r: Request) = r.pathParams ++ r.header.query ++ r.form ++ r.headers

def mapped[T](h: T => Response)(using m: MapExtract[T]): Request => Response =
  request =>
    val handlerParam = m.projectMap(joinMaps(request))
    h(handlerParam)

def mapped[T](h: (T, Request) => Response)(using
  m: MapExtract[T]
): Request => Response =
  request =>
    val handlerParam = m.projectMap(joinMaps(request))
    h(handlerParam, request)

def mapped[T1, T2](
  h: (T1, T2) => Response
)(using m1: MapExtract[T1], m2: MapExtract[T2]): Request => Response =
  request =>
    val joinedMaps = joinMaps(request)
    val p1         = m1.projectMap(joinedMaps)
    val p2         = m2.projectMap(joinedMaps)
    h(p1, p2)

def mapped[T1, T2](
  h: (T1, T2, Request) => Response
)(using m1: MapExtract[T1], m2: MapExtract[T2]): Request => Response =
  request =>
    val joinedMaps = joinMaps(request)
    val p1         = m1.projectMap(joinedMaps)
    val p2         = m2.projectMap(joinedMaps)
    h(p1, p2, request)

def mapped[T1, T2, T3](h: (T1, T2, T3) => Response)(using
  m1: MapExtract[T1],
  m2: MapExtract[T2],
  m3: MapExtract[T3]
): Request => Response =
  request =>
    val joinedMaps = joinMaps(request)
    val p1         = m1.projectMap(joinedMaps)
    val p2         = m2.projectMap(joinedMaps)
    val p3         = m3.projectMap(joinedMaps)
    h(p1, p2, p3)

def mapped[T1, T2, T3](h: (T1, T2, T3, Request) => Response)(using
  m1: MapExtract[T1],
  m2: MapExtract[T2],
  m3: MapExtract[T3]
): Request => Response =
  request =>
    val joinedMaps = joinMaps(request)
    val p1         = m1.projectMap(joinedMaps)
    val p2         = m2.projectMap(joinedMaps)
    val p3         = m3.projectMap(joinedMaps)
    h(p1, p2, p3, request)
