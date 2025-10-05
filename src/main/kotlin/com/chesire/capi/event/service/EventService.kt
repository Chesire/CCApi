package com.chesire.capi.event.service

import com.chesire.capi.event.data.EventEntity
import com.chesire.capi.event.data.EventRepository
import com.chesire.capi.event.dto.EventDto
import com.chesire.capi.event.dto.PostEventDto
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EventService(private val repository: EventRepository) {

    fun createEvent(data: PostEventDto): CreateEventResult {
        return try {
            val result = repository.save(data.toEntity())
            CreateEventResult.Success(result.toDto())
        } catch (ex: Exception) {
            CreateEventResult.UnknownError
        }
    }

    private fun PostEventDto.toEntity(): EventEntity {
        return EventEntity(
            key = key,
            value = value,
            userId = userId
        )
    }

    private fun EventEntity.toDto(): EventDto {
        return EventDto(
            key = key,
            value = value,
            userId = userId,
            timestamp = createdAt ?: LocalDateTime.now() // just do now for now
        )
    }
}

sealed interface CreateEventResult {
    data class Success(val event: EventDto) : CreateEventResult
    object UnknownError : CreateEventResult
}
