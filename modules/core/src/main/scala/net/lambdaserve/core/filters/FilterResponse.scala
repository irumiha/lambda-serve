package net.lambdaserve.core.filters

import net.lambdaserve.core.http.{Request, Response}

enum FilterResponse:
  case Continue(request: Request)
  case Stop(response: Response)
  case Wrap(request: Request, responseWrapper: Response => WrapperResponse)


enum WrapperResponse:
  case Continue(response: Response)
  case Stop(response: Response)
