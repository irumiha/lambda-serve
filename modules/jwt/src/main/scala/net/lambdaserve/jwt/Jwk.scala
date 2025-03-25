package net.lambdaserve.jwt

import org.jose4j.jwk.JsonWebKey.OutputControlLevel
import org.jose4j.jwk.{JsonWebKey, OctetKeyPairJsonWebKey, OkpJwkGenerator}

import java.util.{Base64, UUID}

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

  def jwkFromJson(json: String): OctetKeyPairJsonWebKey =
    JsonWebKey.Factory.newJwk(json).asInstanceOf[OctetKeyPairJsonWebKey]

  def jwkFromJsonBase64(base64Json: String): OctetKeyPairJsonWebKey =
    val decodedJson = Base64.getDecoder.decode(base64Json)
    jwkFromJson(String(decodedJson))

  @main
  def newSigningJWK(): Unit =
    val jwkJson = toJson(generateSigningJWK())
    val encoded = Base64.getEncoder.encodeToString(jwkJson.getBytes())

    println(s"JWK: $jwkJson")
    println(s"Base64: $encoded")
    println(Base64.getDecoder.decode(encoded) sameElements jwkJson.getBytes())

  @main
  def newEncryptionJWK(): Unit =
    val jwkJson = toJson(generateEncryptionJWK())
    val encoded = Base64.getEncoder.encodeToString(jwkJson.getBytes())

    println(s"JWK: $jwkJson")
    println(s"Base64: $encoded")
    println(Base64.getDecoder.decode(encoded) sameElements jwkJson.getBytes())

end Jwk
