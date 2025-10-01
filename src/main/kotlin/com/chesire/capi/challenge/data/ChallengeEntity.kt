package com.chesire.capi.challenge.data

import com.chesire.capi.models.TimeFrame
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
    @Enumerated(EnumType.STRING)
    val timeFrame: TimeFrame,
    val allowPauses: Boolean,
    val cheats: Int,
)
