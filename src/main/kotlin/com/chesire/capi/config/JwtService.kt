package com.chesire.capi.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService {

    @Value("\${jwt.secret:myDefaultSecretKeyThatShouldBeChangedInProduction}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration:86400000}") // 24 hours in milliseconds
    private var jwtExpiration: Long = 86400000

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    fun generateToken(userId: Long): String {
        val claims = mutableMapOf<String, Any>(USER_ID to userId)

        return Jwts.builder()
            .claims(claims)
            .subject(userId.toString())
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key)
            .compact()
    }

    fun extractUserId(token: String): Long? {
        return try {
            val claims = extractAllClaims(token)
            claims[USER_ID]?.toString()?.toLongOrNull()
        } catch (ex: Exception) {
            null
        }
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            extractAllClaims(token)
            true
        } catch (ex: Exception) {
            false
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    companion object {
        const val USER_ID = "userId"
    }
}
