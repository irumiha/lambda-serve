package net.lambdaserve.core.codec

import net.lambdaserve.core.http.Request

trait JsonDecoder[R]:
  def readBody(request: Request): R
