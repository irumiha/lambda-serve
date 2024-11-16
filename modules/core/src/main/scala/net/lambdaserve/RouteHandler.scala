package net.lambdaserve

import net.lambdaserve.http.{Request, Response}

@FunctionalInterface
trait RouteHandler:
  def handle(request: Request): Response
