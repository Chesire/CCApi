package com.chesire.capi.config.jwt

import com.chesire.capi.error.JwtConfigurationException
import com.chesire.capi.error.JwtGenerationException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.InvalidKeyException
import io.jsonwebtoken.security.Keys
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class JwtService {
    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration}") // 24 hours in milliseconds
    private var jwtExpiration: Long = 86400000
    val jwtExpirationSeconds = TimeUnit.MILLISECONDS.toSeconds(jwtExpiration)

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun generateToken(userId: Long, guildId: Long): String =
        try {
            val claims = mutableMapOf<String, Any>(
                USER_ID to userId,
                GUILD_ID to guildId,
                SCOPE to SCOPE_VALUE,
                Claims.ISSUER to ISSUER,
                Claims.AUDIENCE to AUDIENCE,
                Claims.ID to UUID.randomUUID().toString()
            )

            val token = Jwts
                .builder()
                .claims(claims)
                .subject("bot-user-$userId")
                .issuedAt(Date(System.currentTimeMillis()))
                .expiration(Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact()

            logger.debug("Successfully generated JWT token for userId={}, guildId={}", userId, guildId)
            token
        } catch (ex: InvalidKeyException) {
            logger.error(
                "JWT key configuration error when generating token for userId={}, guildId={}",
                userId,
                guildId,
                ex
            )
            throw JwtConfigurationException("JWT key configuration is invalid", ex)
        } catch (ex: IllegalArgumentException) {
            logger.error("Invalid JWT parameters for userId={}, guildId={}", userId, guildId, ex)
            throw JwtGenerationException("Invalid parameters for token generation", ex)
        } catch (ex: Exception) {
            logger.error("Unexpected error generating JWT token for userId={}, guildId={}", userId, guildId, ex)
            throw JwtConfigurationException("Token generation system error", ex)
        }

    fun extractUserId(token: String): Long? =
        try {
            val claims = extractAllClaims(token)
            claims[USER_ID]?.toString()?.toLongOrNull()
        } catch (ex: Exception) {
            null
        }

    fun extractGuildId(token: String): Long? =
        try {
            val claims = extractAllClaims(token)
            claims[GUILD_ID]?.toString()?.toLongOrNull()
        } catch (ex: Exception) {
            null
        }

    fun extractScope(token: String): String? =
        try {
            val claims = extractAllClaims(token)
            claims[SCOPE]?.toString()
        } catch (ex: Exception) {
            null
        }

    fun extractJwtId(token: String): String? =
        try {
            val claims = extractAllClaims(token)
            claims[Claims.ID]?.toString()
        } catch (ex: Exception) {
            null
        }

    fun hasScope(token: String, requiredScope: String): Boolean =
        try {
            val scope = extractScope(token)
            scope == requiredScope
        } catch (ex: Exception) {
            false
        }

    fun isTokenValid(token: String): Boolean =
        try {
            extractAllClaims(token)
            true
        } catch (ex: Exception) {
            false
        }

    private fun extractAllClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload

    companion object {
        const val USER_ID = "userId"
        const val GUILD_ID = "guildId"
        const val SCOPE = "scope"
        const val SCOPE_VALUE = "bot:discord:user"
        const val ISSUER = "capi-auth-service"
        const val AUDIENCE = "capi-api"

        private val logger = LoggerFactory.getLogger(JwtService::class.java)
    }
}
