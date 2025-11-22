package com.chesire.capi.event.data

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "events")
data class EventEntity(
    @EmbeddedId
    val id: EventCountId,
    @Column(name = "count")
    val count: Int
) {
    val userId: Long get() = id.userId
    val guildId: Long get() = id.guildId
    val eventName: String get() = id.eventName
    val year: Int get() = id.year
}

@Embeddable
data class EventCountId(
    @Column(name = "user_id")
    val userId: Long,
    @Column(name = "guild_id")
    val guildId: Long,
    @Column(name = "event_name")
    val eventName: String,
    @Column(name = "year")
    val year: Int
) : Serializable
