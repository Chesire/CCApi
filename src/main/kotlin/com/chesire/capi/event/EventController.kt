package com.chesire.capi.event

import com.chesire.capi.config.getAuthenticatedUser
import com.chesire.capi.event.dto.EventDto
import com.chesire.capi.event.dto.PostEventDto
import com.chesire.capi.event.service.CreateEventResult
import com.chesire.capi.event.service.EventService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventService: EventService,
) {
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createEvent(
        @Valid @RequestBody data: PostEventDto,
    ): ResponseEntity<EventDto> {
        val guildId = getAuthenticatedUser().guildId

        logger.info("Creating event: {}", data.key)
        return when (val result = eventService.createEvent(data, guildId)) {
            is CreateEventResult.Success -> {
                logger.info("Successfully created event: {}", result.event.key)
                ResponseEntity.ok(result.event)
            }

            CreateEventResult.UnknownError -> {
                logger.error("Unknown error creating event: {}", data.key)
                ResponseEntity.internalServerError().build()
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EventController::class.java)
    }
}
