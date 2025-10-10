package com.chesire.capi.event.service

import com.chesire.capi.event.data.EventEntity
import com.chesire.capi.event.data.EventRepository
import com.chesire.capi.event.dto.EventDto
import com.chesire.capi.event.dto.PostEventDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EventService(private val repository: EventRepository) {
    fun createEvent(data: PostEventDto): CreateEventResult {
        logger.info("Starting event creation: key='{}'", data.key)
        val startTime = System.currentTimeMillis()

        return try {
            val entity = data.toEntity()
            val saveStartTime = System.currentTimeMillis()
            val result = repository.save(entity)
            val saveTime = System.currentTimeMillis() - saveStartTime
            logger.debug("Database save completed in {}ms: eventId={}", saveTime, result.id)

            val dto = result.toDto()
            val totalTime = System.currentTimeMillis() - startTime
            logger.info(
                "Successfully created event: eventId={}, key='{}' in {}ms",
                result.id,
                result.eventKey,
                totalTime
            )

            CreateEventResult.Success(dto)
        } catch (ex: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            logger.error(
                "Error creating event key='{}' after {}ms: {} - {}",
                data.key,
                totalTime,
                ex.javaClass.simpleName,
                ex.message,
                ex
            )
            CreateEventResult.UnknownError
        }
    }

    fun getEventsByKey(key: String): GetEventsResult {
        logger.debug("Starting getEventsByKey for key='{}'", key)
        val startTime = System.currentTimeMillis()

        return try {
            val events = repository.findByEventKey(key)
            val queryTime = System.currentTimeMillis() - startTime
            logger.debug(
                "Database query completed in {}ms for key='{}', found {} events",
                queryTime,
                key,
                events.size
            )

            val eventDtos = events.map { it.toDto() }
            val totalTime = System.currentTimeMillis() - startTime
            logger.info(
                "Successfully retrieved and mapped {} events for key='{}' in {}ms",
                eventDtos.size,
                key,
                totalTime
            )

            GetEventsResult.Success(eventDtos)
        } catch (ex: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            logger.error(
                "Error retrieving events for key='{}' after {}ms: {} - {}",
                key,
                totalTime,
                ex.javaClass.simpleName,
                ex.message,
                ex
            )
            GetEventsResult.UnknownError
        }
    }

    private fun PostEventDto.toEntity(): EventEntity {
        return EventEntity(
            eventKey = key,
            eventValue = value,
            userId = userId,
        )
    }

    private fun EventEntity.toDto(): EventDto {
        return EventDto(
            key = eventKey,
            value = eventValue,
            userId = userId,
            timestamp = createdAt ?: LocalDateTime.now(),
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EventService::class.java)
    }
}

sealed interface CreateEventResult {
    data class Success(val event: EventDto) : CreateEventResult
    object UnknownError : CreateEventResult
}

sealed interface GetEventsResult {
    data class Success(val events: List<EventDto>) : GetEventsResult
    object UnknownError : GetEventsResult
}
