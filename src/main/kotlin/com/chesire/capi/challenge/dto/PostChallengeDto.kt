package com.chesire.capi.challenge.dto

import com.chesire.capi.models.TimeFrame
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class PostChallengeDto(
    @field:NotBlank(message = "Challenge name is required and cannot be blank")
    @field:Size(min = 3, max = 20, message = "Challenge name must be between 3 and 20 characters")
    val name: String,
    @field:NotBlank(message = "Challenge description is required and cannot be blank")
    @field:Size(min = 1, max = 200, message = "Challenge description must be between 1 and 200 characters")
    val description: String,
    @field:NotNull(message = "Time frame is required")
    val timeFrame: TimeFrame,
    @field:NotNull(message = "Allow pauses setting is required")
    val allowPauses: Boolean,
    @field:Min(value = 0, message = "Number of cheats cannot be negative")
    @field:Max(value = 4, message = "Number of cheats cannot exceed 4")
    val cheats: Int,
)
