package com.chesire.capi.event.service

import com.chesire.capi.event.data.EventEntity
import com.chesire.capi.event.data.EventRepository
import com.chesire.capi.event.dto.EventDto
import com.chesire.capi.event.dto.PostEventDto
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventService(
    private val repository: EventRepository,
) {
    fun createEvent(data: PostEventDto, guildId: Long): CreateEventResult {
        logger.info("Starting event creation: key='{}'", data.key)
        val startTime = System.currentTimeMillis()
        val year = LocalDateTime.now().year

        return try {
            val previousEntity = getPreviousEntity(data = data, guildId = guildId, year = year)
            val entity = previousEntity
                ?.copy(count = previousEntity.count + 1)
                ?: data.toEntity(guildId = guildId, year = year, count = 0)

            val saveStartTime = System.currentTimeMillis()
            val result = repository.save(entity)
            val saveTime = System.currentTimeMillis() - saveStartTime
            logger.debug("Database save completed in {}ms: id={}", saveTime, result.id)

            val dto = result.toDto()
            val totalTime = System.currentTimeMillis() - startTime
            logger.info("Successfully created event: id={} in {}ms", result.id, totalTime)

            CreateEventResult.Success(dto)
        } catch (ex: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            logger.error(
                "Error creating event key='{}' after {}ms: {} - {}",
                data.key,
                totalTime,
                ex.javaClass.simpleName,
                ex.message,
                ex,
            )
            CreateEventResult.UnknownError
        }
    }

    private fun getPreviousEntity(data: PostEventDto, guildId: Long, year: Int): EventEntity? {
        val retrieveStartTime = System.currentTimeMillis()
        logger.info("Finding previous entity: userId='{}', guildId='{}', key='{}'", data.userId, guildId, data.key)
        val previousEntity = repository.findByUserIdAndGuildIdAndEventNameAndYear(
            userId = data.userId,
            guildId = guildId,
            eventName = data.key,
            year = year
        )
        val retrieveTime = System.currentTimeMillis() - retrieveStartTime
        logger.info("Found previous entity: id='{}' in {}ms", previousEntity?.pk, retrieveTime)

        return previousEntity
    }

    private fun PostEventDto.toEntity(guildId: Long, year: Int, count: Int = 0): EventEntity =
        EventEntity(
            userId = userId,
            guildId = guildId,
            eventName = key,
            year = year,
            count = count
        )

    private fun EventEntity.toDto(): EventDto =
        EventDto(
            userId = userId,
            guildId = guildId,
            key = eventName,
            count = count,
            year = year
        )

    companion object {
        private val logger = LoggerFactory.getLogger(EventService::class.java)
    }
}

sealed interface CreateEventResult {
    data class Success(
        val event: EventDto,
    ) : CreateEventResult

    object UnknownError : CreateEventResult
}
