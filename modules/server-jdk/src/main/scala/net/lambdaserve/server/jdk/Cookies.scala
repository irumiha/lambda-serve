package net.lambdaserve.server.jdk

import com.sun.net.httpserver.Request
import net.lambdaserve.http.Cookie

import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object Cookies:

  def extractCookies(request: Request): Map[String, Cookie] =
    val finalCookies = mutable.Map[String, Cookie]()

    request.getRequestHeaders.forEach { (headerName: String, values: util.List[String]) =>
      if headerName.equalsIgnoreCase("Cookie") then
        values.asScala.foreach { cookieHeader =>
          // Parse Cookie header format: "name1=value1; name2=value2; ..."
          cookieHeader.split(";").foreach { pair =>
            val trimmedPair = pair.trim
            val equalIndex = trimmedPair.indexOf('=')

            if equalIndex > 0 then
              val name = trimmedPair.substring(0, equalIndex).trim
              val value = if equalIndex < trimmedPair.length - 1
              then trimmedPair.substring(equalIndex + 1).trim
              else ""

              if name.nonEmpty then
                finalCookies(name) = Cookie(name = name, value = value)
          }
        }
    }
    finalCookies.toMap

  def serializeCookieToHeader(cookie: Cookie): (String, String) =
    import net.lambdaserve.http.SameSite
    import java.time.ZoneOffset
    import java.time.format.DateTimeFormatter

    val builder = new StringBuilder()

    // Basic name=value pair
    builder.append(cookie.name).append("=").append(cookie.value)

    // Path attribute
    cookie.path.foreach { p =>
      builder.append("; Path=").append(p)
    }

    // Domain attribute
    cookie.domain.foreach { d =>
      builder.append("; Domain=").append(d)
    }

    // Max-Age attribute (takes precedence over Expires)
    cookie.maxAge.foreach { ma =>
      builder.append("; Max-Age=").append(ma)
    }

    // Expires attribute (only if Max-Age is not set)
    if cookie.maxAge.isEmpty then
      cookie.expires.foreach { exp =>
        // Format: Wdy, DD Mon YYYY HH:MM:SS GMT (RFC 1123 format)
        val formatter = DateTimeFormatter.RFC_1123_DATE_TIME
        val formatted = exp.atZone(ZoneOffset.UTC).format(formatter)
        builder.append("; Expires=").append(formatted)
      }

    // Secure flag
    if cookie.secure then
      builder.append("; Secure")

    // HttpOnly flag
    if cookie.httpOnly then
      builder.append("; HttpOnly")

    // SameSite attribute
    cookie.sameSite.foreach { ss =>
      builder.append("; SameSite=").append(ss match
        case SameSite.Strict => "Strict"
        case SameSite.Lax => "Lax"
        case SameSite.None => "None"
      )
    }

    // Partitioned flag
    if cookie.partitioned then
      builder.append("; Partitioned")

    ("Set-Cookie", builder.toString)

end Cookies