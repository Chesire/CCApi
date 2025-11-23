package com.chesire.capi.event.dto

data class EventDto(
    val userId: String,
    val guildId: String,
    val key: String,
    val year: Int,
    val count: Int
)
