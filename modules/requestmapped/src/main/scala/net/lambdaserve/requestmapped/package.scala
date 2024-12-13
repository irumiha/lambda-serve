package net.lambdaserve.requestmapped

import net.lambdaserve.codec.EntityDecoder
import net.lambdaserve.http.{Request, Response}
import net.lambdaserve.mapextract.MapExtract

trait Combined[T]:
  def mapValues(request: Request): T

object Combined:
  given mapExtractCombined[T](using m: MapExtract[T]): Combined[T] with
    override def mapValues(request: Request): T = map1(request, m)

  given entityDecoderCombined[T](using m: EntityDecoder[T]): Combined[T] with
    override def mapValues(request: Request): T = m.readBody(request)

  private def map1[T](request: Request, m: MapExtract[T]): T =
    m.projectMaps(
      Seq(request.pathParams, request.query, request.form, request.headers)
    )

extension [T](handler: T => Response)(using m: Combined[T])
  inline def mapped(request: Request): Response =
    handler(m.mapValues(request))

extension [T](handler: (T, Request) => Response)(using m: Combined[T])
  inline def mapped(request: Request): Response =
    handler(m.mapValues(request), request)

extension [T1, T2](
  handler: (T1, T2) => Response
)(using m1: Combined[T1], m2: Combined[T2])
  inline def mapped(request: Request): Response =
    handler(m1.mapValues(request), m2.mapValues(request))

extension [T1, T2](
  handler: (T1, T2, Request) => Response
)(using m1: Combined[T1], m2: Combined[T2])
  inline def mapped(request: Request): Response =
    handler(m1.mapValues(request), m2.mapValues(request), request)

extension [T1, T2, T3](
  handler: (T1, T2, T3) => Response
)(using m1: Combined[T1], m2: Combined[T2], m3: Combined[T3])
  inline def mapped(request: Request): Response =
    handler(m1.mapValues(request), m2.mapValues(request), m3.mapValues(request))

extension [T1, T2, T3](
  handler: (T1, T2, T3, Request) => Response
)(using m1: Combined[T1], m2: Combined[T2], m3: Combined[T3])
  inline def mapped(request: Request): Response =
    handler(
      m1.mapValues(request),
      m2.mapValues(request),
      m3.mapValues(request),
      request
    )

extension [T1, T2, T3, T4](
  handler: (T1, T2, T3, T4) => Response
)(using m1: Combined[T1], m2: Combined[T2], m3: Combined[T3], m4: Combined[T4])
  inline def mapped(request: Request): Response =
    handler(
      m1.mapValues(request),
      m2.mapValues(request),
      m3.mapValues(request),
      m4.mapValues(request)
    )

extension [T1, T2, T3, T4](
  handler: (T1, T2, T3, T4, Request) => Response
)(using m1: Combined[T1], m2: Combined[T2], m3: Combined[T3], m4: Combined[T4])
  inline def mapped(request: Request): Response =
    handler(
      m1.mapValues(request),
      m2.mapValues(request),
      m3.mapValues(request),
      m4.mapValues(request),
      request
    )
