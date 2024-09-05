package net.lambdaserve.core.http

import java.time.Instant

enum SameSite:
  case Strict, Lax, None

case class Cookie(
  name: String,
  value: String,
  path: Option[String] = None,
  secure: Boolean = false,
  domain: Option[String] = None,
  comment: Option[String] = None,
  maxAge: Option[Long] = None,
  expires: Option[Instant] = None,
  httpOnly: Boolean = false,
  partitioned: Boolean = false,
  sameSite: Option[SameSite] = None
)
