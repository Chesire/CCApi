package com.chesire.capi.event

import com.chesire.capi.event.dto.EventDto
import com.chesire.capi.event.dto.PostEventDto
import com.chesire.capi.event.service.CreateEventResult
import com.chesire.capi.event.service.EventService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/events")
class EventController(private val eventService: EventService) {

    @PostMapping
    fun createEvent(@Valid @RequestBody data: PostEventDto): ResponseEntity<EventDto> {
        return when (val result = eventService.createEvent(data)) {
            is CreateEventResult.Success -> ResponseEntity.ok(result.event)
            CreateEventResult.UnknownError -> ResponseEntity.internalServerError().build()
        }
    }
}
