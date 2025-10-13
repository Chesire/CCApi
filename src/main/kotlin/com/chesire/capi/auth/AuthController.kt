package com.chesire.capi.auth

import com.chesire.capi.auth.dto.AuthRequestDto
import com.chesire.capi.auth.dto.AuthResponseDto
import com.chesire.capi.config.JwtService
import com.chesire.capi.config.TokenRateLimiter
import com.chesire.capi.error.TokenRateLimitException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
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

    @PostMapping("/token")
    fun generateToken(
        @Valid @RequestBody request: AuthRequestDto,
        @RequestHeader("X-API-Key") apiKey: String,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<AuthResponseDto> {
        if (!isValidApiKey(apiKey)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API Key")
        }
        logger.info("Token requested for user: {}, in guild: {}", request.userId, request.guildId)

        val clientId = getClientId(httpRequest)

        if (!rateLimiter.isAllowed(clientId)) {
            throw TokenRateLimitException("Rate limit exceeded for client: $clientId")
        }

        val token = jwtService.generateToken(request.userId, request.guildId)
        logger.info("Token generated successfully for user: {}, in guild: {}", request.userId, request.guildId)
        return ResponseEntity.ok(AuthResponseDto(token))
    }

    private fun isValidApiKey(providedKey: String): Boolean {
        return providedKey == configuredApiKey
    }

    private fun getClientId(request: HttpServletRequest): String {
        val forwardedFor = request.getHeader("X-Forwarded-For")
        return if (forwardedFor.isNullOrBlank()) {
            request.remoteAddr
        } else {
            forwardedFor.split(",")[0].trim()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthController::class.java)
    }
}
