package com.chesire.capi.event.data

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import org.hibernate.annotations.CreationTimestamp

@Entity
data class EventEntity(
    @Id
    val userId: Long,
    @Id
    val guildId: Long,
    @Id
    val eventName: String,
    @Id
    val year: Int,
    val count: Int,
    var lastUpdated: LocalDateTime = LocalDateTime.now()
)

@Embeddable
data class EventCountId(
    val userId: Long,
    val guildId: Long,
    val eventName: String,
    val year: Int
) : Serializable
