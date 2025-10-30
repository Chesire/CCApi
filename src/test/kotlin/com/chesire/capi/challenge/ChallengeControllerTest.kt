package com.chesire.capi.challenge

import com.chesire.capi.challenge.dto.ChallengeDto
import com.chesire.capi.challenge.dto.PostChallengeDto
import com.chesire.capi.challenge.service.ChallengeService
import com.chesire.capi.challenge.service.DeleteChallengeResult
import com.chesire.capi.challenge.service.GetChallengeResult
import com.chesire.capi.challenge.service.GetChallengesResult
import com.chesire.capi.challenge.service.PostChallengeResult
import com.chesire.capi.config.jwt.JwtService
import com.chesire.capi.models.TimeFrame
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Challenge Controller Tests")
class ChallengeControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var jwtService: JwtService

    @MockBean
    private lateinit var challengeService: ChallengeService

    private lateinit var validToken: String

    @BeforeEach
    fun setup() {
        validToken = jwtService.generateToken(userId = TEST_USER_ID, guildId = TEST_GUILD_ID)
    }

    // GET /challenges/{challengeId} tests
    @Test
    @DisplayName("GET challenge by ID: 200 success")
    fun testGetChallengeSuccess() {
        `when`(challengeService.getChallenge(1L, TEST_GUILD_ID))
            .thenReturn(GetChallengeResult.Success(ChallengeDto(1L, "Test", "Desc", TimeFrame.DAILY, true, 2)))

        val response = mockMvc.perform(
            get("/api/v1/challenges/{challengeId}", 1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(200, response.response.status)
    }

    companion object {
        private const val TEST_USER_ID = 123L
        private const val TEST_GUILD_ID = 456L
    }

    @Test
    @DisplayName("GET challenge by ID: 204 not found")
    fun testGetChallengeNotFound() {
        `when`(challengeService.getChallenge(999L, TEST_GUILD_ID))
            .thenReturn(GetChallengeResult.NotFound)

        val response = mockMvc.perform(
            get("/api/v1/challenges/{challengeId}", 999L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(204, response.response.status)
    }

    @Test
    @DisplayName("GET challenge by ID: 403 unauthorized")
    fun testGetChallengeUnauthorized() {
        `when`(challengeService.getChallenge(1L, TEST_GUILD_ID))
            .thenReturn(GetChallengeResult.Unauthorized)

        val response = mockMvc.perform(
            get("/api/v1/challenges/{challengeId}", 1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(403, response.response.status)
    }

    @Test
    @DisplayName("GET challenge by ID: 500 error")
    fun testGetChallengeError() {
        `when`(challengeService.getChallenge(1L, TEST_GUILD_ID))
            .thenReturn(GetChallengeResult.UnknownError)

        val response = mockMvc.perform(
            get("/api/v1/challenges/{challengeId}", 1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(500, response.response.status)
    }

    @Test
    @DisplayName("GET challenge by ID: 400 negative")
    fun testGetChallengeNegativeId() {
        val response = mockMvc.perform(
            get("/api/v1/challenges/{challengeId}", -1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(400, response.response.status)
    }

    @Test
    @DisplayName("GET challenge by ID: 400 zero")
    fun testGetChallengeZeroId() {
        val response = mockMvc.perform(
            get("/api/v1/challenges/{challengeId}", 0L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(400, response.response.status)
    }

    // GET /challenges/user/{userId} tests
    @Test
    @DisplayName("GET user challenges: 200 success")
    fun testGetUserChallengesSuccess() {
        `when`(challengeService.getChallenges(1L, TEST_GUILD_ID))
            .thenReturn(GetChallengesResult.Success(listOf(
                ChallengeDto(1L, "C1", "D1", TimeFrame.DAILY, true, 2)
            )))

        val response = mockMvc.perform(
            get("/api/v1/challenges/user/{userId}", 1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(200, response.response.status)
    }

    @Test
    @DisplayName("GET user challenges: 204 not found")
    fun testGetUserChallengesNotFound() {
        `when`(challengeService.getChallenges(1L, TEST_GUILD_ID))
            .thenReturn(GetChallengesResult.NotFound)

        val response = mockMvc.perform(
            get("/api/v1/challenges/user/{userId}", 1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(204, response.response.status)
    }

    @Test
    @DisplayName("GET user challenges: 500 error")
    fun testGetUserChallengesError() {
        `when`(challengeService.getChallenges(1L, TEST_GUILD_ID))
            .thenReturn(GetChallengesResult.UnknownError)

        val response = mockMvc.perform(
            get("/api/v1/challenges/user/{userId}", 1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(500, response.response.status)
    }

    @Test
    @DisplayName("GET user challenges: 400 negative")
    fun testGetUserChallengesNegativeId() {
        val response = mockMvc.perform(
            get("/api/v1/challenges/user/{userId}", -1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(400, response.response.status)
    }

    @Test
    @DisplayName("GET user challenges: 400 zero")
    fun testGetUserChallengesZeroId() {
        val response = mockMvc.perform(
            get("/api/v1/challenges/user/{userId}", 0L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(400, response.response.status)
    }

    // POST /challenges tests
    @Test
    @DisplayName("POST challenge: 200 created")
    fun testCreateChallengeSuccess() {
        val postDto = PostChallengeDto("Test", "Description", TimeFrame.DAILY, true, 2)
        `when`(challengeService.addChallenge(postDto, TEST_USER_ID, TEST_GUILD_ID))
            .thenReturn(PostChallengeResult.Success(ChallengeDto(1L, "Test", "Description", TimeFrame.DAILY, true, 2)))

        val response = mockMvc.perform(
            post("/api/v1/challenges")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        ).andReturn()

        assertEquals(200, response.response.status)
    }

    @Test
    @DisplayName("POST challenge: 400 invalid data")
    fun testCreateChallengeInvalid() {
        val postDto = PostChallengeDto("Test", "Description", TimeFrame.DAILY, true, 2)
        `when`(challengeService.addChallenge(postDto, TEST_USER_ID, TEST_GUILD_ID))
            .thenReturn(PostChallengeResult.InvalidData)

        val response = mockMvc.perform(
            post("/api/v1/challenges")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        ).andReturn()

        assertEquals(400, response.response.status)
    }

    @Test
    @DisplayName("POST challenge: 204 not found")
    fun testCreateChallengeNotFound() {
        val postDto = PostChallengeDto("Test", "Description", TimeFrame.DAILY, true, 2)
        `when`(challengeService.addChallenge(postDto, TEST_USER_ID, TEST_GUILD_ID))
            .thenReturn(PostChallengeResult.NotFound)

        val response = mockMvc.perform(
            post("/api/v1/challenges")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        ).andReturn()

        assertEquals(204, response.response.status)
    }

    @Test
    @DisplayName("POST challenge: 500 error")
    fun testCreateChallengeError() {
        val postDto = PostChallengeDto("Test", "Description", TimeFrame.DAILY, true, 2)
        `when`(challengeService.addChallenge(postDto, TEST_USER_ID, TEST_GUILD_ID))
            .thenReturn(PostChallengeResult.UnknownError)

        val response = mockMvc.perform(
            post("/api/v1/challenges")
                .header("Authorization", "Bearer $validToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        ).andReturn()

        assertEquals(500, response.response.status)
    }

    // DELETE /challenges/{challengeId} tests
    @Test
    @DisplayName("DELETE challenge: 204 success")
    fun testDeleteChallengeSuccess() {
        `when`(challengeService.deleteChallenge(1L, TEST_USER_ID, TEST_GUILD_ID))
            .thenReturn(DeleteChallengeResult.Success)

        val response = mockMvc.perform(
            delete("/api/v1/challenges/{challengeId}", 1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(204, response.response.status)
    }

    @Test
    @DisplayName("DELETE challenge: 204 not found")
    fun testDeleteChallengeNotFound() {
        `when`(challengeService.deleteChallenge(999L, TEST_USER_ID, TEST_GUILD_ID))
            .thenReturn(DeleteChallengeResult.NotFound)

        val response = mockMvc.perform(
            delete("/api/v1/challenges/{challengeId}", 999L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(204, response.response.status)
    }

    @Test
    @DisplayName("DELETE challenge: 403 unauthorized")
    fun testDeleteChallengeUnauthorized() {
        `when`(challengeService.deleteChallenge(1L, TEST_USER_ID, TEST_GUILD_ID))
            .thenReturn(DeleteChallengeResult.Unauthorized)

        val response = mockMvc.perform(
            delete("/api/v1/challenges/{challengeId}", 1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(403, response.response.status)
    }

    @Test
    @DisplayName("DELETE challenge: 500 error")
    fun testDeleteChallengeError() {
        `when`(challengeService.deleteChallenge(1L, TEST_USER_ID, TEST_GUILD_ID))
            .thenReturn(DeleteChallengeResult.UnknownError)

        val response = mockMvc.perform(
            delete("/api/v1/challenges/{challengeId}", 1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(500, response.response.status)
    }

    @Test
    @DisplayName("DELETE challenge: 400 negative")
    fun testDeleteChallengeNegativeId() {
        val response = mockMvc.perform(
            delete("/api/v1/challenges/{challengeId}", -1L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(400, response.response.status)
    }

    @Test
    @DisplayName("DELETE challenge: 400 zero")
    fun testDeleteChallengeZeroId() {
        val response = mockMvc.perform(
            delete("/api/v1/challenges/{challengeId}", 0L)
                .header("Authorization", "Bearer $validToken")
        ).andReturn()

        assertEquals(400, response.response.status)
    }
}
