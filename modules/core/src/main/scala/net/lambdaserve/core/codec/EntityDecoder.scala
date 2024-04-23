package net.lambdaserve.core.codec

import net.lambdaserve.core.http.Request

trait EntityDecoder[R]:
  def readBody(request: Request): R
