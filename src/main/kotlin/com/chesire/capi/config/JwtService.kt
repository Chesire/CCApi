package com.chesire.capi.config

import com.chesire.capi.error.JwtConfigurationException
import com.chesire.capi.error.JwtGenerationException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.InvalidKeyException
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService {
    @Value("\${jwt.secret}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration}") // 24 hours in milliseconds
    private var jwtExpiration: Long = 86400000

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun generateToken(userId: Long): String =
        try {
            val claims = mutableMapOf<String, Any>(USER_ID to userId)

            val token =
                Jwts
                    .builder()
                    .claims(claims)
                    .subject(userId.toString())
                    .issuedAt(Date(System.currentTimeMillis()))
                    .expiration(Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(key)
                    .compact()

            logger.debug("Successfully generated JWT token for userId={}", userId)
            token
        } catch (ex: InvalidKeyException) {
            logger.error("JWT key configuration error when generating token for userId={}", userId, ex)
            throw JwtConfigurationException("JWT key configuration is invalid", ex)
        } catch (ex: IllegalArgumentException) {
            logger.error("Invalid JWT parameters for userId={}", userId, ex)
            throw JwtGenerationException("Invalid parameters for token generation", ex)
        } catch (ex: Exception) {
            logger.error("Unexpected error generating JWT token for userId={}", userId, ex)
            throw JwtConfigurationException("Token generation system error", ex)
        }

    fun extractUserId(token: String): Long? =
        try {
            val claims = extractAllClaims(token)
            claims[USER_ID]?.toString()?.toLongOrNull()
        } catch (ex: Exception) {
            null
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
        private val logger = LoggerFactory.getLogger(JwtService::class.java)
    }
}
