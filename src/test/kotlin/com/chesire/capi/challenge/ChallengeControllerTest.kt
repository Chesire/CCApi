package com.chesire.capi.challenge

import com.chesire.capi.challenge.dto.ChallengeDto
import com.chesire.capi.challenge.dto.PostChallengeDto
import com.chesire.capi.challenge.service.ChallengeService
import com.chesire.capi.challenge.service.DeleteChallengeResult
import com.chesire.capi.challenge.service.GetChallengeResult
import com.chesire.capi.challenge.service.GetChallengesResult
import com.chesire.capi.challenge.service.PostChallengeResult
import com.chesire.capi.models.TimeFrame
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.http.MediaType

@WebMvcTest(ChallengeController::class)
@DisplayName("ChallengeController Tests")
class ChallengeControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var challengeService: ChallengeService

    private fun createValidPostChallengeDto(
        name: String = "Test Challenge",
        description: String = "A test challenge description", 
        timeFrame: TimeFrame = TimeFrame.DAILY,
        allowPauses: Boolean = true,
        cheats: Int = 2
    ) = PostChallengeDto(
        name = name,
        description = description,
        timeFrame = timeFrame,
        allowPauses = allowPauses,
        cheats = cheats
    )

    private fun createValidChallengeDto(
        id: Long = 1L,
        name: String = "Test Challenge",
        description: String = "A test challenge description",
        timeFrame: TimeFrame = TimeFrame.DAILY,
        allowPauses: Boolean = true,
        cheats: Int = 2
    ) = ChallengeDto(
        id = id,
        name = name,
        description = description,
        timeFrame = timeFrame,
        allowPauses = allowPauses,
        cheats = cheats
    )

    @Test
    @DisplayName("Should return challenge when found by ID")
    fun shouldReturnChallengeWhenFoundById() {
        val challengeId = 1L
        val expectedChallenge = createValidChallengeDto()

        `when`(challengeService.getChallenge(challengeId))
            .thenReturn(GetChallengeResult.Success(expectedChallenge))

        val result = mockMvc.perform(get("/api/v1/challenges/{challengeId}", challengeId))

        result.andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Challenge"))
            .andExpect(jsonPath("$.description").value("A test challenge description"))
            .andExpect(jsonPath("$.timeFrame").value("DAILY"))
            .andExpect(jsonPath("$.allowPauses").value(true))
            .andExpect(jsonPath("$.cheats").value(2))
    }

    @Test
    @DisplayName("Should return no content when challenge not found")
    fun shouldReturnNoContentWhenChallengeNotFound() {
        val challengeId = 999L

        `when`(challengeService.getChallenge(challengeId))
            .thenReturn(GetChallengeResult.NotFound)

        mockMvc.perform(get("/api/v1/challenges/{challengeId}", challengeId))
            .andExpect(status().isNoContent())
    }

    @Test
    @DisplayName("Should return internal server error on unknown error")
    fun shouldReturnInternalServerErrorOnUnknownError() {
        val challengeId = 1L

        `when`(challengeService.getChallenge(challengeId))
            .thenReturn(GetChallengeResult.UnknownError)

        mockMvc.perform(get("/api/v1/challenges/{challengeId}", challengeId))
            .andExpect(status().isInternalServerError())
    }

    @Test
    @DisplayName("Should return bad request for negative challenge ID")
    fun shouldReturnBadRequestForNegativeChallengeId() {
        mockMvc.perform(get("/api/v1/challenges/{challengeId}", -1L))
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should return bad request for zero challenge ID")
    fun shouldReturnBadRequestForZeroChallengeId() {
        mockMvc.perform(get("/api/v1/challenges/{challengeId}", 0L))
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should return list of challenges for valid user")
    fun shouldReturnListOfChallengesForValidUser() {
        val userId = 1L
        val challenges = listOf(
            createValidChallengeDto(id = 1L, name = "Challenge 1"),
            createValidChallengeDto(id = 2L, name = "Challenge 2", timeFrame = TimeFrame.WEEKLY, allowPauses = false, cheats = 0)
        )

        `when`(challengeService.getChallenges(userId))
            .thenReturn(GetChallengesResult.Success(challenges))

        mockMvc.perform(get("/api/v1/challenges/user/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Challenge 1"))
            .andExpect(jsonPath("$[0].timeFrame").value("DAILY"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Challenge 2"))
            .andExpect(jsonPath("$[1].timeFrame").value("WEEKLY"))
            .andExpect(jsonPath("$[1].allowPauses").value(false))
    }

    @Test
    @DisplayName("Should return no content when user has no challenges")
    fun shouldReturnNoContentWhenUserHasNoChallenges() {
        val userId = 1L

        `when`(challengeService.getChallenges(userId))
            .thenReturn(GetChallengesResult.NotFound)

        mockMvc.perform(get("/api/v1/challenges/user/{userId}", userId))
            .andExpect(status().isNoContent())
    }

    @Test
    @DisplayName("Should return internal server error on unknown error for user challenges")
    fun shouldReturnInternalServerErrorOnUnknownErrorForUserChallenges() {
        val userId = 1L

        `when`(challengeService.getChallenges(userId))
            .thenReturn(GetChallengesResult.UnknownError)

        mockMvc.perform(get("/api/v1/challenges/user/{userId}", userId))
            .andExpect(status().isInternalServerError())
    }

    @Test
    @DisplayName("Should return bad request for negative user ID")
    fun shouldReturnBadRequestForNegativeUserId() {
        mockMvc.perform(get("/api/v1/challenges/user/{userId}", -1L))
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should return bad request for zero user ID")
    fun shouldReturnBadRequestForZeroUserId() {
        mockMvc.perform(get("/api/v1/challenges/user/{userId}", 0L))
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should create challenge with valid data")
    fun shouldCreateChallengeWithValidData() {
        val postDto = createValidPostChallengeDto()
        val createdChallenge = createValidChallengeDto()

        `when`(challengeService.addChallenge(postDto, 0L))
            .thenReturn(PostChallengeResult.Success(createdChallenge))

        mockMvc.perform(
            post("/api/v1/challenges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Test Challenge"))
            .andExpect(jsonPath("$.description").value("A test challenge description"))
            .andExpect(jsonPath("$.timeFrame").value("DAILY"))
            .andExpect(jsonPath("$.allowPauses").value(true))
            .andExpect(jsonPath("$.cheats").value(2))
    }

    @Test
    @DisplayName("Should return bad request on invalid data response from service")
    fun shouldReturnBadRequestOnInvalidDataResponseFromService() {
        val postDto = createValidPostChallengeDto()

        `when`(challengeService.addChallenge(postDto, 0L))
            .thenReturn(PostChallengeResult.InvalidData)

        mockMvc.perform(
            post("/api/v1/challenges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        )
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should return no content on not found response from service")
    fun shouldReturnNoContentOnNotFoundResponseFromService() {
        val postDto = createValidPostChallengeDto()

        `when`(challengeService.addChallenge(postDto, 0L))
            .thenReturn(PostChallengeResult.NotFound)

        mockMvc.perform(
            post("/api/v1/challenges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        )
            .andExpect(status().isNoContent())
    }

    @Test
    @DisplayName("Should return internal server error on unknown error for create challenge")
    fun shouldReturnInternalServerErrorOnUnknownErrorForCreateChallenge() {
        val postDto = createValidPostChallengeDto()

        `when`(challengeService.addChallenge(postDto, 0L))
            .thenReturn(PostChallengeResult.UnknownError)

        mockMvc.perform(
            post("/api/v1/challenges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        )
            .andExpect(status().isInternalServerError())
    }

    @Test
    @DisplayName("Should delete challenge successfully")
    fun shouldDeleteChallengeSuccessfully() {
        val challengeId = 1L

        `when`(challengeService.deleteChallenge(challengeId))
            .thenReturn(DeleteChallengeResult.Success)

        mockMvc.perform(delete("/api/v1/challenges/{challengeId}", challengeId))
            .andExpect(status().isNoContent())
    }

    @Test
    @DisplayName("Should return no content when challenge to delete not found")
    fun shouldReturnNoContentWhenChallengeToDeleteNotFound() {
        val challengeId = 999L

        `when`(challengeService.deleteChallenge(challengeId))
            .thenReturn(DeleteChallengeResult.NotFound)

        mockMvc.perform(delete("/api/v1/challenges/{challengeId}", challengeId))
            .andExpect(status().isNoContent())
    }

    @Test
    @DisplayName("Should return internal server error on unknown error for delete challenge")  
    fun shouldReturnInternalServerErrorOnUnknownErrorForDeleteChallenge() {
        val challengeId = 1L

        `when`(challengeService.deleteChallenge(challengeId))
            .thenReturn(DeleteChallengeResult.UnknownError)

        mockMvc.perform(delete("/api/v1/challenges/{challengeId}", challengeId))
            .andExpect(status().isInternalServerError())
    }

    @Test
    @DisplayName("Should return bad request for negative challenge ID on delete")
    fun shouldReturnBadRequestForNegativeChallengeIdOnDelete() {
        mockMvc.perform(delete("/api/v1/challenges/{challengeId}", -1L))
            .andExpect(status().isBadRequest())
    }

    @Test
    @DisplayName("Should return bad request for zero challenge ID on delete")  
    fun shouldReturnBadRequestForZeroChallengeIdOnDelete() {
        mockMvc.perform(delete("/api/v1/challenges/{challengeId}", 0L))
            .andExpect(status().isBadRequest())
    }
}