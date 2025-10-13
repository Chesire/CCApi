package com.chesire.capi.auth

import com.chesire.capi.auth.dto.AuthRequestDto
import com.chesire.capi.auth.dto.AuthResponseDto
import com.chesire.capi.config.JwtService
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
class AuthController(private val jwtService: JwtService) {

    @PostMapping("/token")
    fun generateToken(@Valid @RequestBody request: AuthRequestDto): ResponseEntity<AuthResponseDto> {
        val token = jwtService.generateToken(request.userId)
        return ResponseEntity.ok(AuthResponseDto(token))
    }
}

