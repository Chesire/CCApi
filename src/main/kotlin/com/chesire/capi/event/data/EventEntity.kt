package com.chesire.capi.event.data

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
data class EventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val eventKey: String,
    val eventValue: String,
    val userId: Long,
    @CreationTimestamp
    val createdAt: LocalDateTime? = null,
)
