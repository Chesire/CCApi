package com.chesire.capi.auth.dto

import jakarta.validation.constraints.Min

data class AuthRequestDto(
    // Discord's smallest user ID is 4194304
    @field:Min(value = 4194304, message = "Invalid Discord user ID format")
    val userId: Long,
    // Discord's smallest guild ID is 4194304
    @field:Min(value = 4194304, message = "Invalid Discord guild ID")
    val guildId: Long,
)
