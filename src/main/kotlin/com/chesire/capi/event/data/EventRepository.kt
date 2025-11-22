package com.chesire.capi.event.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository : JpaRepository<EventEntity, EventCountId> {
    fun findByEventKeyAndGuildId(eventKey: String, guildId: Long): List<EventEntity>
}
