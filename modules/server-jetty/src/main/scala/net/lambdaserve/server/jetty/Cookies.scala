package net.lambdaserve.server.jetty

import net.lambdaserve.core.http.Cookie
import org.eclipse.jetty.http.HttpCookie
import org.eclipse.jetty.server.Request

import scala.jdk.CollectionConverters.*

object Cookies:

  def extractCookies(request: Request): Map[String, Cookie] =
    Request
      .getCookies(request)
      .stream()
      .map(fromJettyCookie)
      .toList
      .asScala
      .toMap

  def fromJettyCookie(cookie: HttpCookie): (String, Cookie) =
    cookie.getName -> Cookie(
      name = cookie.getName,
      value = cookie.getValue,
      path = Option(cookie.getPath),
      secure = cookie.isSecure,
      domain = Option(cookie.getDomain),
      comment = Option(cookie.getComment),
      maxAge = Option(cookie.getMaxAge),
      expires = Option(cookie.getExpires),
      httpOnly = cookie.isHttpOnly,
      partitioned = cookie.isHttpOnly
    )

  def toJettyCookie(source: Cookie): HttpCookie =
    val builder = HttpCookie.build(source.name, source.value, 1)
    source.path.foreach(builder.path)
    builder.secure(source.secure)
    source.sameSite.foreach(ss =>
      builder.sameSite(HttpCookie.SameSite.from(ss.toString))
    )
    source.domain.foreach(builder.domain)
    source.comment.foreach(builder.comment)
    source.maxAge.foreach(builder.maxAge)
    source.expires.foreach(builder.expires)
    builder.httpOnly(source.httpOnly)
    builder.partitioned(source.partitioned)
    builder.build()
