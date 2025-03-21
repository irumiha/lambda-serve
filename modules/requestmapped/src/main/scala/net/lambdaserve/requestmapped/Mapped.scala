package net.lambdaserve.requestmapped

import net.lambdaserve.http.{Request, Response}
import net.lambdaserve.mapextract.{MapExtract, SourceName}

type MappedHandler[T] = Request ?=> Combined[T] ?=> T => Response

@main def testMapped(): Unit =
  case class HouseCommand(
    id: String,
    name: String,
    @SourceName("User-Agent") userAgent: String
  ) derives MapExtract

  val homeHandler: Request ?=> HouseCommand => Response = (param: HouseCommand) =>
      val r = summon[Request]
      Response.Ok(s"Here we go, with headers: ${r.headers}!")
