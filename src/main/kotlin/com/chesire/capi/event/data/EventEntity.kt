package com.chesire.capi.event.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.NaturalId

@Entity
@Table(name = "events")
data class EventEntity(
    @Id
    @GeneratedValue
    val pk: Long = 0,

    @Column(name = "user_id")
    @NaturalId
    val userId: String,

    @Column(name = "guild_id")
    @NaturalId
    val guildId: String,

    @Column(name = "event_name")
    @NaturalId
    val eventName: String,

    @Column(name = "event_year")
    @NaturalId
    val year: Int,

    @Column(name = "count")
    val count: Int
) {
    val id: String get() = "$userId-$guildId-$eventName-$year"
}
