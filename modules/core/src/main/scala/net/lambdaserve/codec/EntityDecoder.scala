package net.lambdaserve.codec

import net.lambdaserve.http.Request

trait EntityDecoder[R]:
  def readBody(request: Request): R
