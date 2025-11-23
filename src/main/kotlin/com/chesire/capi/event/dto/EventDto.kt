package com.chesire.capi.event.dto

data class EventDto(
    val userId: Long,
    val guildId: Long,
    val key: String,
    val year: Int,
    val count: Int
)
