package com.chesire.capi.event.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository : JpaRepository<EventEntity, EventCountId> {
    fun findByUserIdAndGuildIdAndEventNameAndYear(
        userId: Long,
        guildId: Long,
        eventName: String,
        year: Int
    ): EventEntity?
}
