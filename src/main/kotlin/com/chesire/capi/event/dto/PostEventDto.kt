package com.chesire.capi.event.dto

data class PostEventDto(
    val key: String,
    val value: String,
    /**
     * User this event is associated with.
     */
    val userId: Long
)
