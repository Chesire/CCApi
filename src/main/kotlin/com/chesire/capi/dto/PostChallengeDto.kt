package com.chesire.capi.dto

import com.chesire.capi.models.TimeFrame

data class PostChallengeDto(
    val name: String,
    val description: String,
    val timeFrame: TimeFrame,
    val allowPauses: Boolean,
    val cheats: Int,
)
