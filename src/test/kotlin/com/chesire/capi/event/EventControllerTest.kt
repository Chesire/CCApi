package com.chesire.capi.event

import com.chesire.capi.config.jwt.JwtService
import com.chesire.capi.event.dto.EventDto
import com.chesire.capi.event.dto.PostEventDto
import com.chesire.capi.event.service.CreateEventResult
import com.chesire.capi.event.service.EventService
import com.chesire.capi.event.service.GetEventsResult
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("EventController Tests")
class EventControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var jwtService: JwtService

    @MockBean
    private lateinit var eventService: EventService

    private lateinit var validToken: String

    @BeforeEach
    fun setup() {
        validToken = jwtService.generateToken(userId = TEST_USER_ID, guildId = TEST_GUILD_ID)
    }

    private fun authenticatedGet(url: String, vararg uriVariables: Any) =
        mockMvc.perform(
            get(url, *uriVariables)
                .header("Authorization", "Bearer $validToken")
        )

    private fun authenticatedPost(url: String, body: Any) =
        mockMvc.perform(
            post(url)
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )

    private fun createValidPostEventDto(
        key: String = "challenge_completed",
        value: String = "gym_challenge",
        userId: Long = TEST_USER_ID,
    ) = PostEventDto(
        key = key,
        value = value,
        userId = userId,
    )

    private fun createValidEventDto(
        key: String = "challenge_completed",
        value: String = "gym_challenge",
        userId: Long = TEST_USER_ID,
        timestamp: LocalDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0),
    ) = EventDto(
        key = key,
        value = value,
        userId = userId,
        timestamp = timestamp,
    )

    companion object {
        private const val TEST_USER_ID = 123L
        private const val TEST_GUILD_ID = 1000L
    }

    @Nested
    @DisplayName("POST /events")
    inner class CreateEvent {

        @Test
        @DisplayName("Should return 200 with created event when valid data provided")
        fun shouldCreateEventWithValidData() {
            val postDto = createValidPostEventDto()
            val createdEvent = createValidEventDto()

            `when`(eventService.createEvent(postDto, TEST_GUILD_ID))
                .thenReturn(CreateEventResult.Success(createdEvent))

            authenticatedPost("/api/v1/events", postDto)
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.key").value("challenge_completed"))
                .andExpect(jsonPath("$.value").value("gym_challenge"))
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.timestamp").value("2024-01-01T12:00:00"))
        }

        @Test
        @DisplayName("Should return 500 on unknown error")
        fun shouldReturnInternalServerErrorOnUnknownError() {
            val postDto = createValidPostEventDto()

            `when`(eventService.createEvent(postDto, TEST_GUILD_ID))
                .thenReturn(CreateEventResult.UnknownError)

            authenticatedPost("/api/v1/events", postDto)
                .andExpect(status().isInternalServerError)
        }

        @Test
        @DisplayName("Should return 400 for invalid event data")
        fun shouldReturnBadRequestForInvalidEventData() {
            val invalidDto =
                mapOf(
                    "key" to "",
                    "value" to "",
                    "userId" to 0,
                )

            authenticatedPost("/api/v1/events", invalidDto)
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Should return 400 for missing required fields")
        fun shouldReturnBadRequestForMissingRequiredFields() {
            val incompleteDto = mapOf("key" to "test_key")

            authenticatedPost("/api/v1/events", incompleteDto)
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Should return 400 for malformed JSON")
        fun shouldReturnBadRequestForMalformedJson() {
            mockMvc.perform(
                post("/api/v1/events")
                    .header("Authorization", "Bearer $validToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"),
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /events/{key}")
    inner class GetEventsByKey {

        @Test
        @DisplayName("Should return 200 with events when found")
        fun shouldReturnEventsByKeyWhenFound() {
            val key = "challenge_completed"
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

            `when`(eventService.getEventsByKey(key, TEST_GUILD_ID))
                .thenReturn(GetEventsResult.Success(events))

            authenticatedGet("/api/v1/events/{key}", key)
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray)
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
        @DisplayName("Should return 200 with empty list when no events found")
        fun shouldReturnEmptyListWhenNoEventsFoundForKey() {
            val key = "nonexistent_key"

            `when`(eventService.getEventsByKey(key, TEST_GUILD_ID))
                .thenReturn(GetEventsResult.Success(emptyList()))

            authenticatedGet("/api/v1/events/{key}", key)
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(0))
        }

        @Test
        @DisplayName("Should return 500 on unknown error")
        fun shouldReturnInternalServerErrorOnUnknownError() {
            val key = "test_key"

            `when`(eventService.getEventsByKey(key, TEST_GUILD_ID))
                .thenReturn(GetEventsResult.UnknownError)

            authenticatedGet("/api/v1/events/{key}", key)
                .andExpect(status().isInternalServerError)
        }

        @Test
        @DisplayName("Should return 400 for invalid key format")
        fun shouldReturnBadRequestForInvalidKeyFormat() {
            val invalidKey = "a".repeat(31) // Exceeds max length of 30

            authenticatedGet("/api/v1/events/{key}", invalidKey)
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Should handle special characters in key")
        fun shouldHandleSpecialCharactersInKey() {
            val keyWithSpecialChars = "user_action-123"
            val events = listOf(createValidEventDto(key = keyWithSpecialChars))

            `when`(eventService.getEventsByKey(keyWithSpecialChars, TEST_GUILD_ID))
                .thenReturn(GetEventsResult.Success(events))

            authenticatedGet("/api/v1/events/{key}", keyWithSpecialChars)
                .andExpect(status().isOk)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].key").value(keyWithSpecialChars))
        }

        @Test
        @DisplayName("Should accept minimum valid key length (1 character)")
        fun shouldAcceptMinimumKeyLength() {
            val minKey = "a"
            val events = listOf(createValidEventDto(key = minKey))

            `when`(eventService.getEventsByKey(minKey, TEST_GUILD_ID))
                .thenReturn(GetEventsResult.Success(events))

            authenticatedGet("/api/v1/events/{key}", minKey)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("Should accept maximum valid key length (30 characters)")
        fun shouldAcceptMaximumKeyLength() {
            val maxKey = "a".repeat(30)
            val maxEvents = listOf(createValidEventDto(key = maxKey))

            `when`(eventService.getEventsByKey(maxKey, TEST_GUILD_ID))
                .thenReturn(GetEventsResult.Success(maxEvents))

            authenticatedGet("/api/v1/events/{key}", maxKey)
                .andExpect(status().isOk)
        }
    }
}
