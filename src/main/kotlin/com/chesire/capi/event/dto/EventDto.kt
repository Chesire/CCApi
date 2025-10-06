package com.chesire.capi.event.dto

import java.time.LocalDateTime

data class EventDto(
    val key: String,
    val value: String,
    val userId: Long,
    val timestamp: LocalDateTime,
)
