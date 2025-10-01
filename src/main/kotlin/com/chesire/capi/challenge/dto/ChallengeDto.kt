package com.chesire.capi.challenge.dto

import com.chesire.capi.models.TimeFrame

data class ChallengeDto(
    val id: Long,
    val name: String,
    val description: String,
    val timeFrame: TimeFrame,
    val allowPauses: Boolean,
    val cheats: Int,
)
