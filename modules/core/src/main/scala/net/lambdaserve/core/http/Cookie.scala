package net.lambdaserve.core.http

import java.time.Instant

enum SameSite:
  case Strict, Lax, None

case class Cookie(
  name: String,
  value: String,
  path: Option[String] = None,
  secure: Option[Boolean] = None,
  version: Option[Int] = None,
  domain: Option[String] = None,
  comment: Option[String] = None,
  maxAge: Option[Long] = None,
  expires: Option[Instant] = None,
  httpOnly: Option[Boolean] = None,
  partitioned: Option[Boolean] = None,
  sameSite: Option[SameSite] = None
)
