package com.chesire.capi.event.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository : JpaRepository<EventEntity, Long> {

    fun findByKey(key: String): List<EventEntity>
}
