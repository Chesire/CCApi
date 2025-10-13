package com.chesire.capi.auth.dto

import jakarta.validation.constraints.Positive

data class AuthRequestDto(
    @field:Positive(message = "User ID must be positive")
    val userId: Long
)
