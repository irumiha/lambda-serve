package net.lambdaserve.jwt

import org.jose4j.jwt.{JwtClaims, ReservedClaimNames}

import scala.jdk.CollectionConverters.*

case class Jwt(
  jwtId: String,
  issuer: String,
  subject: String,
  issuedAt: Long,
  notBefore: Long,
  expirationTime: Long,
  audience: List[String],
  claims: Map[String, String | List[String] | Map[String, String]]
)

object Jwt:
  
  def fromJwtClaims(jwtClaims: JwtClaims): Jwt =
    Jwt(
      jwtId = jwtClaims.getJwtId,
      issuer = jwtClaims.getIssuer,
      subject = jwtClaims.getSubject,
      issuedAt = jwtClaims.getIssuedAt.getValue,
      notBefore = jwtClaims.getNotBefore.getValue,
      expirationTime = jwtClaims.getExpirationTime.getValue,
      audience = jwtClaims.getAudience.asScala.toList,
      claims = jwtClaims.getClaimsMap.asScala.subtractAll(
        ReservedClaimNames.INITIAL_REGISTERED_CLAIM_NAMES.asScala
      ).map {
        case (key, value) if jwtClaims.isClaimValueString(key) =>
          key -> value.asInstanceOf[String]
        case (key, value) if jwtClaims.isClaimValueStringList(key) =>
          key -> value.asInstanceOf[java.util.List[String]].asScala.toList
        case (key, value) if jwtClaims.isClaimValueOfType(key, classOf[java.util.Map[String, String]]) =>
          key -> value.asInstanceOf[java.util.Map[String, String]].asScala.toMap
      }.toMap
    )
