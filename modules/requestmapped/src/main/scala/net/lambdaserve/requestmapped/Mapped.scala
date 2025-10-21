package net.lambdaserve.requestmapped

import net.lambdaserve.http.{Request, HttpResponse}
import net.lambdaserve.mapextract.{MapExtract, SourceName}

type MappedHandler[T] = Request ?=> Combined[T] ?=> T => HttpResponse

@main def testMapped(): Unit =
  case class HouseCommand(
    id: String,
    name: String,
    @SourceName("User-Agent") userAgent: String
  ) derives MapExtract

  val homeHandler: Request ?=> HouseCommand => HttpResponse =
    (param: HouseCommand) =>
      val r = summon[Request]
      HttpResponse.Ok(s"Here we go, with headers: ${r.headers}!")
