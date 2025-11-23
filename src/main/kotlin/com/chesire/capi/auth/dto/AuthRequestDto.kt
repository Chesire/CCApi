package com.chesire.capi.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class AuthRequestDto(
    // Discord's smallest user ID is 4194304
    @field:NotBlank(message = "UserId is required and cannot be blank")
    @field:Pattern(
        regexp = "^\\d{7,20}$",
        message = "User ID must be a valid Discord ID (between 7 and 20 digits)"
    )
    val userId: String,

    // Discord's smallest guild ID is 4194304
    @field:NotBlank(message = "GuildId is required and cannot be blank")
    @field:Pattern(
        regexp = "^\\d{7,20}$",
        message = "Guild ID must be a valid Discord ID (between 7 and 20 digits)"
    )
    val guildId: String,
)
