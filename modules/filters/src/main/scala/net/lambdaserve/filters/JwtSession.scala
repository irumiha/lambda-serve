package net.lambdaserve.filters

import net.lambdaserve.core.filters.*
import net.lambdaserve.core.http.{Request, TypedKey}
import net.lambdaserve.jwt.{Jwt, JwtUtil}
import org.slf4j.LoggerFactory

case class JwtSessionFilterConfig(cookieName: String)

/** Filter that extracts the JWT session from a cookie. Will only extract the
  * session data if the cookie is present and the JWT is valid. Does not enforce
  * the presence of a valid session cookie. This can be done using some other
  * mechanism.
  *
  * @param config
  *   The filter config.
  * @param jwtUtil
  *   The JWT Utility class.
  */
class JwtSession(config: JwtSessionFilterConfig, jwtUtil: JwtUtil)
    extends Filter:
  private val logger = LoggerFactory.getLogger(this.getClass)

  override def handle(request: Request): FilterInResponse =
    val jwt: Option[Jwt] = request.cookies
      .get(config.cookieName)
      .flatMap { c =>
        jwtUtil.loadToken(c.value) match
          case Left(error) =>
            logger.warn("Session JWT token not loaded", error)
            None
          case Right(jwt) => Some(jwt)
      }

    jwt match
      case Some(jwt) =>
        FilterInResponse.Continue(
          request.copy(data = JwtSession.JwtKey.set(request.data, jwt))
        )
      case None =>
        FilterInResponse.Continue(request)
  end handle

end JwtSession

object JwtSession:
  val JwtKey: TypedKey[Jwt] = TypedKey[Jwt]()
