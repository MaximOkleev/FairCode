package com.team.antiplagiat.config

import com.team.antiplagiat.models.User
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

data class TokenPayload(
    val userId: Long,
    val login: String,
    val email: String,
    val role: String
)

@Service
class TokenService(private val env: Environment) {
    private val secret: String = env.getProperty("app.security.jwt.secret", "dev-secret-change-me")
    private val expirationSeconds: Long = env.getProperty("app.security.jwt.expiration-seconds", Long::class.java, 86400L)
    private val algorithm: Algorithm = Algorithm.HMAC256(secret.toByteArray())
    private val verifier: JWTVerifier = JWT.require(algorithm).build()

    fun generateToken(user: User): String {
        val now = Instant.now()
        val expiresAt = Date.from(now.plusSeconds(expirationSeconds))

        return JWT.create()
            .withSubject(user.login)
            .withClaim("userId", user.id)
            .withClaim("email", user.email)
            .withClaim("role", user.role.name)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    fun parseToken(token: String): TokenPayload? {
        return try {
            val jwt: DecodedJWT = verifier.verify(token)
            val userId = jwt.getClaim("userId").asLong() ?: return null
            val login = jwt.subject ?: return null
            val email = jwt.getClaim("email").asString() ?: ""
            val role = jwt.getClaim("role").asString() ?: "BASIC"
            TokenPayload(userId = userId, login = login, email = email, role = role)
        } catch (ex: JWTVerificationException) {
            null
        }
    }
}

