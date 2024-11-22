package net.lambdaserve.filters

import net.lambdaserve.http.{Request, Response}

enum FilterInResponse:
  case Continue(request: Request)
  case Stop(response: Response)
  case Wrap(request: Request, outFilter: Response => FilterOutResponse)

enum FilterOutResponse:
  case Continue(response: Response)
  case Stop(response: Response)
