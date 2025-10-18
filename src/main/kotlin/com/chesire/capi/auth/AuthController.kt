package com.chesire.capi.auth

import com.chesire.capi.auth.dto.AuthRequestDto
import com.chesire.capi.auth.dto.AuthResponseDto
import com.chesire.capi.config.JwtService
import com.chesire.capi.config.TokenRateLimiter
import com.chesire.capi.error.TokenRateLimitException
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import java.security.MessageDigest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Validated
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val jwtService: JwtService,
    private val rateLimiter: TokenRateLimiter,
    @Value("\${capi.auth.api-key}") private val configuredApiKey: String,
) {

    @PostMapping("/token", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun generateToken(
        @Valid @RequestBody request: AuthRequestDto,
        @RequestHeader("X-API-Key")
        @Size(min = 20, max = 100, message = "Invalid API key format")
        apiKey: String,
    ): ResponseEntity<AuthResponseDto> {
        if (!isValidApiKey(apiKey)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API Key")
        }
        logger.info("Token requested for user: {}, in guild: {}", request.userId, request.guildId)

        if (!rateLimiter.isAllowed(apiKey, request.userId, request.guildId)) {
            throw TokenRateLimitException("Rate limit exceeded")
        }

        val token = jwtService.generateToken(request.userId, request.guildId)
        logger.info("Token generated successfully for user: {}, in guild: {}", request.userId, request.guildId)
        return ResponseEntity.ok()
            .header(HttpHeaders.CACHE_CONTROL, NO_CACHE_CONTROL)
            .header(HttpHeaders.PRAGMA, NO_CACHE_PRAGMA)
            .header(HttpHeaders.EXPIRES, EXPIRED)
            .body(
                AuthResponseDto(
                    token = token,
                    tokenType = "Bearer",
                    expiresIn = jwtService.jwtExpirationSeconds
                )
            )
    }

    private fun isValidApiKey(providedKey: String): Boolean {
        val isValid = MessageDigest.isEqual(
            providedKey.toByteArray(Charsets.UTF_8),
            configuredApiKey.toByteArray(Charsets.UTF_8)
        )
        if (!isValid) {
            logger.warn(
                "Authentication failed - invalid API key attempt. Key prefix: {}, Length: {}",
                providedKey.take(8).padEnd(8, '*'),
                providedKey.length
            )
        }
        return isValid
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthController::class.java)
        private const val NO_CACHE_CONTROL = "no-store, no-cache, must-revalidate, private"
        private const val NO_CACHE_PRAGMA = "no-cache"
        private const val EXPIRED = "0"
    }
}
