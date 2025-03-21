package net.lambdaserve

import net.lambdaserve.http.{Request, Response}

type RouteHandler = Request => Response
