package com.chesire.capi.event

import com.chesire.capi.config.jwt.JwtService
import com.chesire.capi.event.dto.EventDto
import com.chesire.capi.event.dto.PostEventDto
import com.chesire.capi.event.service.CreateEventResult
import com.chesire.capi.event.service.EventService

import com.fasterxml.jackson.databind.ObjectMapper
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
        userId: Long = TEST_USER_ID,
    ) = PostEventDto(
        key = key,
        userId = userId,
    )

    private fun createValidEventDto(
        key: String = "challenge_completed",
        userId: Long = TEST_USER_ID,
        guildId: Long = TEST_GUILD_ID,
        year: Int = 2024,
        count: Int = 0,
    ) = EventDto(
        key = key,
        userId = userId,
        guildId = guildId,
        year = year,
        count = count,
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
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.guildId").value(TEST_GUILD_ID))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.count").value(0))
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
}
