package com.chesire.capi.event

import com.chesire.capi.event.dto.EventDto
import com.chesire.capi.event.dto.PostEventDto
import com.chesire.capi.event.service.CreateEventResult
import com.chesire.capi.event.service.EventService
import com.chesire.capi.event.service.GetEventsResult
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
        logger.info("Creating event: {}", data.key)
        return when (val result = eventService.createEvent(data)) {
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

    @GetMapping("/{key}")
    fun getEventsByKey(
        @PathVariable @Size(min = 1, max = 30, message = "Key must be between 1 and 30 characters") key: String,
    ): ResponseEntity<List<EventDto>> {
        // Add pagination at some point
        // Maybe add a from timeframe as well?
        logger.info("Fetching events: {}", key)
        return when (val result = eventService.getEventsByKey(key)) {
            is GetEventsResult.Success -> {
                logger.info("Successfully fetched {} events", result.events.size)
                ResponseEntity.ok(result.events)
            }

            GetEventsResult.UnknownError -> {
                logger.error("Unknown error fetching events: {}", key)
                ResponseEntity.internalServerError().build()
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EventController::class.java)
    }
}
