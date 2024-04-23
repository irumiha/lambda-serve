package net.lambdaserve.core.codec

import java.io.OutputStream

trait JsonEncoder[R]:
  def bodyWriter(responseEntity: R): OutputStream => Unit
