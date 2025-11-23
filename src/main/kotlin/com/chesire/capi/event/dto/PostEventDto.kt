package com.chesire.capi.event.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

// Example: {"key":"challenge_failed","userId":"123"}
data class PostEventDto(
    @field:NotBlank(message = "Key is required and cannot be blank")
    @field:Size(min = 1, max = 30, message = "Key must be between 1 and 30 characters")
    val key: String,

    @field:NotBlank(message = "UserId is required and cannot be blank")
    @field:Pattern(
        regexp = "^\\d{7,20}$",
        message = "User ID must be a valid Discord ID (between 7 and 20 digits)"
    )
    val userId: String,
)
