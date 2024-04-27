package net.lambdaserve.core

import net.lambdaserve.core.http.{Request, Response}

trait RouteHandler:
  def handle(request: Request): Response
