package net.lambdaserve.core.codec

import java.io.OutputStream

trait EntityEncoder[R]:
  def bodyWriter(responseEntity: R): OutputStream => Unit
