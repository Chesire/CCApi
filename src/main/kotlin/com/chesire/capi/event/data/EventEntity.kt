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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val eventKey: String,
    val eventValue: String,
    val userId: Long,
    val guildId: Long,
    @CreationTimestamp
    val createdAt: LocalDateTime? = null,
)
