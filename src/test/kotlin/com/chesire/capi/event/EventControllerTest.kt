package com.chesire.capi.event

import com.chesire.capi.config.SecurityConfig
import com.chesire.capi.event.dto.EventDto
import com.chesire.capi.event.dto.PostEventDto
import com.chesire.capi.event.service.CreateEventResult
import com.chesire.capi.event.service.EventService
import com.chesire.capi.event.service.GetEventsResult
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(
    controllers = [EventController::class]
)
@DisplayName("EventController Tests")
class EventControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var eventService: EventService

    @MockBean
    private lateinit var jwtService: com.chesire.capi.config.jwt.JwtService

    @MockBean
    private lateinit var jwtAuthenticationFilter: com.chesire.capi.config.jwt.JwtAuthenticationFilter

    @MockBean
    private lateinit var tokenRateLimiter: com.chesire.capi.config.TokenRateLimiter

    @MockBean
    private lateinit var requestCorrelationFilter: com.chesire.capi.config.RequestCorrelationFilter

    private fun createValidPostEventDto(
        key: String = "challenge_completed",
        value: String = "gym_challenge",
        userId: Long = 123L,
    ) = PostEventDto(
        key = key,
        value = value,
        userId = userId,
    )

    private fun createValidEventDto(
        key: String = "challenge_completed",
        value: String = "gym_challenge",
        userId: Long = 123L,
        timestamp: LocalDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0),
    ) = EventDto(
        key = key,
        value = value,
        userId = userId,
        timestamp = timestamp,
    )

    @Test
    @WithMockJwtAuthentication
    @DisplayName("Should create event with valid data")
    fun shouldCreateEventWithValidData() {
        val postDto = createValidPostEventDto()
        val createdEvent = createValidEventDto()
        val guildId = 1000L

        `when`(eventService.createEvent(postDto, guildId))
            .thenReturn(CreateEventResult.Success(createdEvent))

        mockMvc.perform(
            post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)),
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.key").value("challenge_completed"))
            .andExpect(jsonPath("$.value").value("gym_challenge"))
            .andExpect(jsonPath("$.userId").value(123))
            .andExpect(jsonPath("$.timestamp").value("2024-01-01T12:00:00"))
    }

    @Test
    @DisplayName("Should return internal server error on unknown error for create event")
    fun shouldReturnInternalServerErrorOnUnknownErrorForCreateEvent() {
        val postDto = createValidPostEventDto()
        val guildId = 1000L

        `when`(eventService.createEvent(postDto, guildId))
            .thenReturn(CreateEventResult.UnknownError)

        mockMvc.perform(
            post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)),
        )
            .andExpect(status().isInternalServerError())
    }

    @Test
    @DisplayName("Should return bad request for invalid event data")
    fun shouldReturnBadRequestForInvalidEventData() {
        val invalidDto =
            mapOf(
                "key" to "",
                "value" to "",
                "userId" to 0,
            )

        mockMvc.perform(
            post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)),
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should return bad request for missing required fields")
    fun shouldReturnBadRequestForMissingRequiredFields() {
        val incompleteDto = mapOf("key" to "test_key")

        mockMvc.perform(
            post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incompleteDto)),
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should return bad request for malformed JSON")
    fun shouldReturnBadRequestForMalformedJson() {
        mockMvc.perform(
            post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"),
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should return events by key when found")
    fun shouldReturnEventsByKeyWhenFound() {
        val key = "challenge_completed"
        val guildId = 1000L
        val events =
            listOf(
                createValidEventDto(
                    key = key,
                    value = "gym_challenge",
                    userId = 123L,
                    timestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0),
                ),
                createValidEventDto(
                    key = key,
                    value = "running_challenge",
                    userId = 456L,
                    timestamp = LocalDateTime.of(2024, 1, 2, 14, 30, 0),
                ),
            )

        `when`(eventService.getEventsByKey(key, guildId))
            .thenReturn(GetEventsResult.Success(events))

        mockMvc.perform(get("/api/v1/events/{key}", key))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].key").value("challenge_completed"))
            .andExpect(jsonPath("$[0].value").value("gym_challenge"))
            .andExpect(jsonPath("$[0].userId").value(123))
            .andExpect(jsonPath("$[0].timestamp").value("2024-01-01T12:00:00"))
            .andExpect(jsonPath("$[1].key").value("challenge_completed"))
            .andExpect(jsonPath("$[1].value").value("running_challenge"))
            .andExpect(jsonPath("$[1].userId").value(456))
            .andExpect(jsonPath("$[1].timestamp").value("2024-01-02T14:30:00"))
    }

    @Test
    @DisplayName("Should return empty list when no events found for key")
    fun shouldReturnEmptyListWhenNoEventsFoundForKey() {
        val key = "nonexistent_key"
        val guildId = 1000L

        `when`(eventService.getEventsByKey(key, guildId))
            .thenReturn(GetEventsResult.Success(emptyList()))

        mockMvc.perform(get("/api/v1/events/{key}", key))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @DisplayName("Should return internal server error on unknown error for get events")
    fun shouldReturnInternalServerErrorOnUnknownErrorForGetEvents() {
        val key = "test_key"
        val guildId = 1000L

        `when`(eventService.getEventsByKey(key, guildId))
            .thenReturn(GetEventsResult.UnknownError)

        mockMvc.perform(get("/api/v1/events/{key}", key))
            .andExpect(status().isInternalServerError())
    }

    @Test
    @DisplayName("Should return bad request for invalid key format")
    fun shouldReturnBadRequestForInvalidKeyFormat() {
        val invalidKey = "a".repeat(31) // Exceeds max length of 30

        mockMvc.perform(get("/api/v1/events/{key}", invalidKey))
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should handle special characters in key")
    fun shouldHandleSpecialCharactersInKey() {
        val keyWithSpecialChars = "user_action-123"
        val guildId = 1000L
        val events = listOf(createValidEventDto(key = keyWithSpecialChars))

        `when`(eventService.getEventsByKey(keyWithSpecialChars, guildId))
            .thenReturn(GetEventsResult.Success(events))

        mockMvc.perform(get("/api/v1/events/{key}", keyWithSpecialChars))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].key").value(keyWithSpecialChars))
    }

    @Test
    @DisplayName("Should validate key length boundaries")
    fun shouldValidateKeyLengthBoundaries() {
        val guildId = 1000L

        // Test minimum valid length (1 character)
        val minKey = "a"
        val events = listOf(createValidEventDto(key = minKey))

        `when`(eventService.getEventsByKey(minKey, guildId))
            .thenReturn(GetEventsResult.Success(events))

        mockMvc.perform(get("/api/v1/events/{key}", minKey))
            .andExpect(status().isOk())

        // Test maximum valid length (30 characters)
        val maxKey = "a".repeat(30)
        val maxEvents = listOf(createValidEventDto(key = maxKey))

        `when`(eventService.getEventsByKey(maxKey, guildId))
            .thenReturn(GetEventsResult.Success(maxEvents))

        mockMvc.perform(get("/api/v1/events/{key}", maxKey))
            .andExpect(status().isOk())
    }

    @Test
    @DisplayName("Should create event with different field combinations")
    fun shouldCreateEventWithDifferentFieldCombinations() {
        val guildId = 1000L
        val testCases =
            listOf(
                createValidPostEventDto(key = "a", value = "x", userId = 1L),
                createValidPostEventDto(key = "a".repeat(30), value = "b".repeat(200), userId = Long.MAX_VALUE),
                createValidPostEventDto(key = "test_key", value = "JSON: {\"id\":123}", userId = 999L),
            )

        testCases.forEach { postDto ->
            val createdEvent =
                createValidEventDto(
                    key = postDto.key,
                    value = postDto.value,
                    userId = postDto.userId,
                )

            `when`(eventService.createEvent(postDto, guildId))
                .thenReturn(CreateEventResult.Success(createdEvent))

            mockMvc.perform(
                post("/api/v1/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(postDto)),
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(postDto.key))
                .andExpect(jsonPath("$.value").value(postDto.value))
                .andExpect(jsonPath("$.userId").value(postDto.userId))
        }
    }

    @Test
    @DisplayName("Should reject event creation with validation errors")
    fun shouldRejectEventCreationWithValidationErrors() {
        val invalidCases =
            listOf(
                mapOf("key" to "", "value" to "valid", "userId" to 123),
                mapOf("key" to "valid", "value" to "", "userId" to 123),
                mapOf("key" to "valid", "value" to "valid", "userId" to 0),
                mapOf("key" to "valid", "value" to "valid", "userId" to -1),
                mapOf("key" to "a".repeat(31), "value" to "valid", "userId" to 123),
                mapOf("key" to "valid", "value" to "b".repeat(201), "userId" to 123),
            )

        invalidCases.forEach { invalidDto ->
            mockMvc.perform(
                post("/api/v1/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidDto)),
            )
                .andExpect(status().isBadRequest())
        }
    }
}
