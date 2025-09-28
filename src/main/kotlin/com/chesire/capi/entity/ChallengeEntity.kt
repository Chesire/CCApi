package com.chesire.capi.entity

import com.chesire.capi.models.TimeFrame

// To insert into DB
data class ChallengeEntity(
    val id: Long,
    val name: String,
    val description: String,
    val timeFrame: TimeFrame,
    val allowPauses: Boolean,
    val cheats: Int,
)
