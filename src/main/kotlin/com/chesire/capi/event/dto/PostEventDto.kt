package com.chesire.capi.event.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

// Example: {"key":"challenge_failed","value":"gym_challenge","userId":123}
data class PostEventDto(
    @field:NotBlank(message = "Key is required and cannot be blank")
    @field:Size(min = 1, max = 30, message = "Key must be between 1 and 30 characters")
    val key: String,
    @field:NotBlank(message = "Value is required and cannot be blank")
    @field:Size(min = 1, max = 200, message = "Value must be between 1 and 200 characters")
    val value: String,
    /**
     * User this event is associated with.
     */
    @field:Positive
    val userId: Long,
)
