package com.chesire.capi.auth.dto

data class AuthResponseDto(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
)
