package net.lambdaserve.requestmapped

import net.lambdaserve.core.Route
import net.lambdaserve.core.http.{Request, Response}
import net.lambdaserve.core.http.Util.HttpMethod
import net.lambdaserve.mapextract.MapExtract

import scala.util.matching.Regex

object AutoMappedRoute:
  def GET[R: MapExtract](path: Regex)(handler: R => Response): Route =
    Route(HttpMethod.GET, path, mapped(handler))

  def GET[R: MapExtract](path: Regex)(handler: (R, Request) => Response): Route =
    Route(HttpMethod.GET, path, mapped(handler))

  def GET[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2) => Response): Route =
    Route(HttpMethod.GET, path, mapped(handler))

  def GET[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2, Request) => Response): Route =
    Route(HttpMethod.GET, path, mapped(handler))

  def GET[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3) => Response): Route =
    Route(HttpMethod.GET, path, mapped(handler))

  def GET[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3, Request) => Response): Route =
    Route(HttpMethod.GET, path, mapped(handler))

  def POST[R: Combined](path: Regex)(handler: R => Response): Route =
    Route(HttpMethod.POST, path, mapped(handler))

  def POST[R: Combined](path: Regex)(handler: (R, Request) => Response): Route =
    Route(HttpMethod.POST, path, mapped(handler))

  def POST[R1: Combined, R2: Combined](path: Regex)(handler: (R1, R2) => Response): Route =
    Route(HttpMethod.POST, path, mapped(handler))

  def POST[R1: Combined, R2: Combined](path: Regex)(handler: (R1, R2, Request) => Response): Route =
    Route(HttpMethod.POST, path, mapped(handler))

  def POST[R1: Combined, R2: Combined, R3: Combined](path: Regex)(handler: (R1, R2, R3) => Response): Route =
    Route(HttpMethod.POST, path, mapped(handler))

  def POST[R1: Combined, R2: Combined, R3: Combined](path: Regex)(handler: (R1, R2, R3, Request) => Response): Route =
    Route(HttpMethod.POST, path, mapped(handler))

  def PUT[R: Combined](path: Regex)(handler: R => Response): Route =
    Route(HttpMethod.PUT, path, mapped(handler))

  def PUT[R: Combined](path: Regex)(handler: (R, Request) => Response): Route =
    Route(HttpMethod.PUT, path, mapped(handler))

  def PUT[R1: Combined, R2: Combined](path: Regex)(handler: (R1, R2) => Response): Route =
    Route(HttpMethod.PUT, path, mapped(handler))

  def PUT[R1: Combined, R2: Combined](path: Regex)(handler: (R1, R2, Request) => Response): Route =
    Route(HttpMethod.PUT, path, mapped(handler))

  def PUT[R1: Combined, R2: Combined, R3: Combined](path: Regex)(handler: (R1, R2, R3) => Response): Route =
    Route(HttpMethod.PUT, path, mapped(handler))

  def PUT[R1: Combined, R2: Combined, R3: Combined](path: Regex)(handler: (R1, R2, R3, Request) => Response): Route =
    Route(HttpMethod.PUT, path, mapped(handler))

  def DELETE[R: MapExtract](path: Regex)(handler: R => Response): Route =
    Route(HttpMethod.DELETE, path, mapped(handler))

  def DELETE[R: MapExtract](path: Regex)(handler: (R, Request) => Response): Route =
    Route(HttpMethod.DELETE, path, mapped(handler))

  def DELETE[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2) => Response): Route =
    Route(HttpMethod.DELETE, path, mapped(handler))

  def DELETE[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2, Request) => Response): Route =
    Route(HttpMethod.DELETE, path, mapped(handler))

  def DELETE[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3) => Response): Route =
    Route(HttpMethod.DELETE, path, mapped(handler))

  def DELETE[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3, Request) => Response): Route =
    Route(HttpMethod.DELETE, path, mapped(handler))

  def PATCH[R: Combined](path: Regex)(handler: R => Response): Route =
    Route(HttpMethod.PATCH, path, mapped(handler))

  def PATCH[R: Combined](path: Regex)(handler: (R, Request) => Response): Route =
    Route(HttpMethod.PATCH, path, mapped(handler))

  def PATCH[R1: Combined, R2: Combined](path: Regex)(handler: (R1, R2) => Response): Route =
    Route(HttpMethod.PATCH, path, mapped(handler))

  def PATCH[R1: Combined, R2: Combined](path: Regex)(handler: (R1, R2, Request) => Response): Route =
    Route(HttpMethod.PATCH, path, mapped(handler))

  def PATCH[R1: Combined, R2: Combined, R3: Combined](path: Regex)(handler: (R1, R2, R3) => Response): Route =
    Route(HttpMethod.PATCH, path, mapped(handler))

  def PATCH[R1: Combined, R2: Combined, R3: Combined](path: Regex)(handler: (R1, R2, R3, Request) => Response): Route =
    Route(HttpMethod.PATCH, path, mapped(handler))

  def HEAD[R: MapExtract](path: Regex)(handler: R => Response): Route =
    Route(HttpMethod.HEAD, path, mapped(handler))

  def HEAD[R: MapExtract](path: Regex)(handler: (R, Request) => Response): Route =
    Route(HttpMethod.HEAD, path, mapped(handler))

  def HEAD[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2) => Response): Route =
    Route(HttpMethod.HEAD, path, mapped(handler))

  def HEAD[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2, Request) => Response): Route =
    Route(HttpMethod.HEAD, path, mapped(handler))

  def HEAD[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3) => Response): Route =
    Route(HttpMethod.HEAD, path, mapped(handler))

  def HEAD[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3, Request) => Response): Route =
    Route(HttpMethod.HEAD, path, mapped(handler))

  def OPTIONS[R: MapExtract](path: Regex)(handler: R => Response): Route =
    Route(HttpMethod.OPTIONS, path, mapped(handler))

  def OPTIONS[R: MapExtract](path: Regex)(handler: (R, Request) => Response): Route =
    Route(HttpMethod.OPTIONS, path, mapped(handler))

  def OPTIONS[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2) => Response): Route =
    Route(HttpMethod.OPTIONS, path, mapped(handler))

  def OPTIONS[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2, Request) => Response): Route =
    Route(HttpMethod.OPTIONS, path, mapped(handler))

  def OPTIONS[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3) => Response): Route =
    Route(HttpMethod.OPTIONS, path, mapped(handler))

  def OPTIONS[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3, Request) => Response): Route =
    Route(HttpMethod.OPTIONS, path, mapped(handler))

  def TRACE[R: MapExtract](path: Regex)(handler: R => Response): Route =
    Route(HttpMethod.TRACE, path, mapped(handler))

  def TRACE[R: MapExtract](path: Regex)(handler: (R, Request) => Response): Route =
    Route(HttpMethod.TRACE, path, mapped(handler))

  def TRACE[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2) => Response): Route =
    Route(HttpMethod.TRACE, path, mapped(handler))

  def TRACE[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2, Request) => Response): Route =
    Route(HttpMethod.TRACE, path, mapped(handler))

  def TRACE[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3) => Response): Route =
    Route(HttpMethod.TRACE, path, mapped(handler))

  def TRACE[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3, Request) => Response): Route =
    Route(HttpMethod.TRACE, path, mapped(handler))

  def CONNECT[R: MapExtract](path: Regex)(handler: R => Response): Route =
    Route(HttpMethod.CONNECT, path, mapped(handler))

  def CONNECT[R: MapExtract](path: Regex)(handler: (R, Request) => Response): Route =
    Route(HttpMethod.CONNECT, path, mapped(handler))
  
  def CONNECT[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2) => Response): Route =
    Route(HttpMethod.CONNECT, path, mapped(handler))
  
  def CONNECT[R1: MapExtract, R2: MapExtract](path: Regex)(handler: (R1, R2, Request) => Response): Route =
    Route(HttpMethod.CONNECT, path, mapped(handler))
  
  def CONNECT[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3) => Response): Route =
    Route(HttpMethod.CONNECT, path, mapped(handler))
  
  def CONNECT[R1: MapExtract, R2: MapExtract, R3: MapExtract](path: Regex)(handler: (R1, R2, R3, Request) => Response): Route =
    Route(HttpMethod.CONNECT, path, mapped(handler))
