package net.lambdaserve.jwt

import org.jose4j.jwa.AlgorithmConstraints
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType
import org.jose4j.jwe.{
  ContentEncryptionAlgorithmIdentifiers,
  JsonWebEncryption,
  KeyManagementAlgorithmIdentifiers
}
import org.jose4j.jwk.OctetKeyPairJsonWebKey
import org.jose4j.jws.{AlgorithmIdentifiers, JsonWebSignature}
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.{InvalidJwtException, JwtConsumerBuilder}

import scala.jdk.CollectionConverters.*

case class JwtConfig(
  signingJwk: OctetKeyPairJsonWebKey,
  encryptionJwk: OctetKeyPairJsonWebKey,
  issuer: String,
  audience: String,
  expirationMinutes: Int,
  notBeforeMinutes: Int
)

class JwtUtil(config: JwtConfig):

  def createToken(
    subject: String,
    audience: List[String],
    claims: Map[String, String | List[String] | Map[String, String]]
  ): String =
    val innerJwt: String = createSignedToken(subject, audience, claims)
    encryptToken(innerJwt)
  end createToken

  private def encryptToken(innerJwt: String) =
    // The outer JWT is a JWE
    val jwe = new JsonWebEncryption()

    // The output of the X25519 ECDH-ES key agreement and KDF will be the content encryption key
    jwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.ECDH_ES)

    // The content encryption key is used to encrypt the payload
    // with a composite AES-CBC / HMAC SHA2 encryption algorithm
    val encAlg = ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256
    jwe.setEncryptionMethodHeaderParameter(encAlg)

    // We encrypt to the receiver using their public key
    jwe.setKey(config.encryptionJwk.getPublicKey)
    jwe.setKeyIdHeaderValue(config.encryptionJwk.getKeyId)

    // A nested JWT requires that the cty (Content Type) header be set to "JWT" in the outer JWT
    jwe.setContentTypeHeaderValue("JWT")

    // The inner JWT is the payload of the outer JWT
    jwe.setPayload(innerJwt)

    // Produce the JWE compact serialization, which is the complete JWT/JWE representation,
    // which is a string consisting of five dot ('.') separated
    // base64url-encoded parts in the form Header.EncryptedKey.IV.Ciphertext.AuthenticationTag
    jwe.getCompactSerialization

  private def createSignedToken(
    subject: String,
    audience: List[String],
    claims: Map[String, JwtUtil.TokenClaimValue]
  ) =
    val claimsInJwt = new JwtClaims()
    claimsInJwt.setIssuer(config.issuer) // who creates the token and signs it
    claimsInJwt.setAudience(
      audience.asJava
    ) // to whom the token is intended to be sent
    claimsInJwt.setExpirationTimeMinutesInTheFuture(config.expirationMinutes)
    claimsInJwt.setGeneratedJwtId()
    claimsInJwt.setIssuedAtToNow()

    // time before which the token is not yet valid (2 minutes ago)
    claimsInJwt.setNotBeforeMinutesInThePast(config.notBeforeMinutes)

    claimsInJwt.setSubject(
      subject
    ) // the subject/principal is whom the token is about

    claims.foreach((key, value) =>
      value match
        case s: String =>
          claimsInJwt.setClaim(key, s)
        case l: List[String] =>
          claimsInJwt.setStringListClaim(key, l.asJava)
        case m: Map[String, String] =>
          claimsInJwt.setClaim(key, m.asJava)
    )

    val jws = new JsonWebSignature()

    // The payload of the JWS is JSON content of the JWT Claims
    jws.setPayload(claimsInJwt.toJson)

    // The JWT is signed using the sender's private key
    jws.setKey(config.signingJwk.getPrivateKey)

    jws.setKeyIdHeaderValue(config.signingJwk.getKeyId)

    // Set the signature algorithm on the JWT/JWS that will integrity protect the claims
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.EDDSA)

    // Sign the JWS and produce the compact serialization, which will be the inner JWT/JWS
    // representation, which is a string consisting of three dot ('.') separated
    // base64url-encoded parts in the form Header.Payload.Signature
    val innerJwt = jws.getCompactSerialization
    innerJwt

  def loadToken(payload: String): Either[InvalidJwtException, Jwt] =

    val jwsAlgConstraints =
      new AlgorithmConstraints(
        ConstraintType.PERMIT,
        AlgorithmIdentifiers.EDDSA
      )

    val jweAlgConstraints = new AlgorithmConstraints(
      ConstraintType.PERMIT,
      KeyManagementAlgorithmIdentifiers.ECDH_ES
    )

    val jweEncConstraints = new AlgorithmConstraints(
      ConstraintType.PERMIT,
      ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256
    )

    val jwtConsumer =
      new JwtConsumerBuilder()
        .setRequireExpirationTime() // the JWT must have an expiration time.setMaxFutureValidityInMinutes(300)
        .setRequireSubject() // the JWT must have a subject claim
        .setExpectedIssuer(
          config.issuer
        ) // whom the JWT needs to have been issued by
        .setExpectedAudience(config.audience) // to whom the JWT is intended for
        .setDecryptionKey(
          config.encryptionJwk.getPrivateKey
        ) // decrypt with the receiver's private key
        .setVerificationKey(
          config.signingJwk.getPublicKey
        ) // verify the signature with the sender's public key
        .setJwsAlgorithmConstraints(
          jwsAlgConstraints
        ) // limits the acceptable signature algorithm(s)
        .setJweAlgorithmConstraints(
          jweAlgConstraints
        ) // limits acceptable encryption key establishment algorithm(s)
        .setJweContentEncryptionAlgorithmConstraints(
          jweEncConstraints
        ) // limits acceptable content encryption algorithm(s)
        .build()

    try
      //  Validate the JWT and process it to the Claims
      val jwtClaims: JwtClaims = jwtConsumer.processToClaims(payload)
      Right(Jwt.fromJwtClaims(jwtClaims))
    catch
      case e: InvalidJwtException =>
        Left(e)
  end loadToken
end JwtUtil

object JwtUtil:
  type TokenClaimValue = String | List[String] | Map[String, String]

  def main(args: Array[String]): Unit =
    val config = JwtConfig(
      signingJwk = Jwk.generateSigningJWK(),
      encryptionJwk = Jwk.generateEncryptionJWK(),
      issuer = "https://example.com",
      audience = "https://example.com",
      expirationMinutes = 5,
      notBeforeMinutes = 2
    )
    val jwt = JwtUtil(config)
    val token = jwt.createToken(
      subject = "subject",
      audience = List("https://example.com"),
      claims = Map(
        "key"     -> "value",
        "groups"  -> List("group1", "group2"),
        "address" -> Map("street" -> "str1", "city" -> "city1")
      )
    )
    println(token)
    println(jwt.loadToken(token))

end JwtUtil
