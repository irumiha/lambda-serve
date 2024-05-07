package net.lambdaserve.core

import net.lambdaserve.core.http.{Request, Response}

@FunctionalInterface
trait RouteHandler:
  def handle(request: Request): Response
