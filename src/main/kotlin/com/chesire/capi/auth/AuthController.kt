package com.chesire.capi.auth

import com.chesire.capi.auth.dto.AuthRequestDto
import com.chesire.capi.auth.dto.AuthResponseDto
import com.chesire.capi.config.JwtService
import com.chesire.capi.config.TokenRateLimiter
import com.chesire.capi.error.TokenRateLimitException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val jwtService: JwtService,
    private val rateLimiter: TokenRateLimiter,
) {

    @PostMapping("/token")
    fun generateToken(
        @Valid @RequestBody request: AuthRequestDto,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponseDto> {
        val clientId = getClientId(httpRequest)

        if (!rateLimiter.isAllowed(clientId)) {
            throw TokenRateLimitException("Rate limit exceeded for client: $clientId")
        }

        val token = jwtService.generateToken(request.userId)
        return ResponseEntity.ok(AuthResponseDto(token))
    }

    private fun getClientId(request: HttpServletRequest): String {
        val forwardedFor = request.getHeader("X-Forwarded-For")
        return if (forwardedFor.isNullOrBlank()) {
            request.remoteAddr
        } else {
            forwardedFor.split(",")[0].trim()
        }
    }
}

