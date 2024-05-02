package net.lambdaserve.core.filters

import net.lambdaserve.core.http.Request

trait Filter:
  def handle(request: Request): FilterResponse
