package net.lambdaserve.jwt

import org.jose4j.jwk.JsonWebKey.OutputControlLevel
import org.jose4j.jwk.{JsonWebKey, OctetKeyPairJsonWebKey, OkpJwkGenerator}

import java.util.UUID

object Jwk:
  def generateSigningJWK(): OctetKeyPairJsonWebKey =
    val jwk = OkpJwkGenerator
      .generateJwk(OctetKeyPairJsonWebKey.SUBTYPE_ED25519)
    jwk.setKeyId(UUID.randomUUID().toString)
    jwk.setUse("sig")
    jwk

  def generateEncryptionJWK(): OctetKeyPairJsonWebKey =
    val jwk = OkpJwkGenerator
      .generateJwk(OctetKeyPairJsonWebKey.SUBTYPE_X25519)
    jwk.setKeyId(UUID.randomUUID().toString)
    jwk.setUse("enc")
    jwk

  def toJson(jwk: OctetKeyPairJsonWebKey): String =
    jwk.toJson(OutputControlLevel.INCLUDE_PRIVATE)

  def toPublicJson(jwk: OctetKeyPairJsonWebKey): String =
    jwk.toJson(OutputControlLevel.PUBLIC_ONLY)

  def jwkFromJson(json: String): JsonWebKey =
    JsonWebKey.Factory.newJwk(json)

  @main
  def newSigningJWK(): Unit =
    println(toJson(generateSigningJWK()))

  @main
  def newEncryptionJWK(): Unit =
    println(toJson(generateEncryptionJWK()))

end Jwk
