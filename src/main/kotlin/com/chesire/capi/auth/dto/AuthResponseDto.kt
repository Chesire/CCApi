package com.chesire.capi.auth.dto

data class AuthResponseDto(
    val token: String,
    val expiresIn: Long,
)
