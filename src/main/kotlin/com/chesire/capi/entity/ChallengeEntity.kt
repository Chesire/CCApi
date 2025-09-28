package com.chesire.capi.entity

import com.chesire.capi.models.TimeFrame
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class ChallengeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val description: String,
    val timeFrame: TimeFrame,
    val allowPauses: Boolean,
    val cheats: Int,
)
