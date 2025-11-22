package com.chesire.capi.event.data

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.io.Serializable

@Entity
data class EventEntity(
    @EmbeddedId
    val id: EventCountId,
    @Id
    val userId: Long,
    @Id
    val guildId: Long,
    @Id
    val eventName: String,
    @Id
    val year: Int,
    val count: Int
)

@Embeddable
data class EventCountId(
    val userId: Long,
    val guildId: Long,
    val eventName: String,
    val year: Int
) : Serializable
