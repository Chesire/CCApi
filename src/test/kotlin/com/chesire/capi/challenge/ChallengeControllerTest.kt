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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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

    private fun authenticatedDelete(url: String, vararg uriVariables: Any) =
        mockMvc.perform(
            delete(url, *uriVariables)
                .header("Authorization", "Bearer $validToken")
        )

    @Nested
    @DisplayName("GET /challenges/{challengeId}")
    inner class GetChallengeById {

        @Test
        @DisplayName("Should return 200 with challenge when found")
        fun shouldReturnChallengeWhenFound() {
            `when`(challengeService.getChallenge(1L, TEST_GUILD_ID))
                .thenReturn(GetChallengeResult.Success(ChallengeDto(1L, "Test", "Desc", TimeFrame.DAILY, true, 2)))

            authenticatedGet("/api/v1/challenges/{challengeId}", 1L)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("Should return 204 when challenge not found")
        fun shouldReturnNoContentWhenNotFound() {
            `when`(challengeService.getChallenge(999L, TEST_GUILD_ID))
                .thenReturn(GetChallengeResult.NotFound)

            authenticatedGet("/api/v1/challenges/{challengeId}", 999L)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("Should return 403 when unauthorized")
        fun shouldReturnForbiddenWhenUnauthorized() {
            `when`(challengeService.getChallenge(1L, TEST_GUILD_ID))
                .thenReturn(GetChallengeResult.Unauthorized)

            authenticatedGet("/api/v1/challenges/{challengeId}", 1L)
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("Should return 500 on unknown error")
        fun shouldReturnInternalServerErrorOnUnknownError() {
            `when`(challengeService.getChallenge(1L, TEST_GUILD_ID))
                .thenReturn(GetChallengeResult.UnknownError)

            authenticatedGet("/api/v1/challenges/{challengeId}", 1L)
                .andExpect(status().isInternalServerError)
        }

        @Test
        @DisplayName("Should return 400 for negative ID")
        fun shouldReturnBadRequestForNegativeId() {
            authenticatedGet("/api/v1/challenges/{challengeId}", "-1")
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Should return 400 for zero ID")
        fun shouldReturnBadRequestForZeroId() {
            authenticatedGet("/api/v1/challenges/{challengeId}", "0")
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /challenges/user/{userId}")
    inner class GetChallengesByUser {

        @Test
        @DisplayName("Should return 200 with challenges when found")
        fun shouldReturnChallengesWhenFound() {
            `when`(challengeService.getChallenges(TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(
                    GetChallengesResult.Success(
                        listOf(
                            ChallengeDto(1L, "C1", "D1", TimeFrame.DAILY, true, 2)
                        )
                    )
                )

            authenticatedGet("/api/v1/challenges/user/{userId}", TEST_USER_ID)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("Should return 204 when no challenges found")
        fun shouldReturnNoContentWhenNoChallengesFound() {
            `when`(challengeService.getChallenges(TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(GetChallengesResult.NotFound)

            authenticatedGet("/api/v1/challenges/user/{userId}", TEST_USER_ID)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("Should return 500 on unknown error")
        fun shouldReturnInternalServerErrorOnUnknownError() {
            `when`(challengeService.getChallenges(TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(GetChallengesResult.UnknownError)

            authenticatedGet("/api/v1/challenges/user/{userId}", TEST_USER_ID)
                .andExpect(status().isInternalServerError)
        }

        @Test
        @DisplayName("Should return 400 for negative ID")
        fun shouldReturnBadRequestForNegativeId() {
            authenticatedGet("/api/v1/challenges/user/{userId}", "-1")
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Should return 400 for zero ID")
        fun shouldReturnBadRequestForZeroId() {
            authenticatedGet("/api/v1/challenges/user/{userId}", "0")
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /challenges")
    inner class CreateChallenge {

        @Test
        @DisplayName("Should return 200 when challenge created successfully")
        fun shouldReturnOkWhenChallengeCreated() {
            val postDto = PostChallengeDto("Test", "Description", TimeFrame.DAILY, true, 2)
            `when`(challengeService.addChallenge(postDto, TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(
                    PostChallengeResult.Success(
                        ChallengeDto(
                            1L,
                            "Test",
                            "Description",
                            TimeFrame.DAILY,
                            true,
                            2
                        )
                    )
                )

            authenticatedPost("/api/v1/challenges", postDto)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("Should return 400 for invalid data")
        fun shouldReturnBadRequestForInvalidData() {
            val postDto = PostChallengeDto("Test", "Description", TimeFrame.DAILY, true, 2)
            `when`(challengeService.addChallenge(postDto, TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(PostChallengeResult.InvalidData)

            authenticatedPost("/api/v1/challenges", postDto)
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Should return 204 when related entity not found")
        fun shouldReturnNoContentWhenRelatedEntityNotFound() {
            val postDto = PostChallengeDto("Test", "Description", TimeFrame.DAILY, true, 2)
            `when`(challengeService.addChallenge(postDto, TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(PostChallengeResult.NotFound)

            authenticatedPost("/api/v1/challenges", postDto)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("Should return 500 on unknown error")
        fun shouldReturnInternalServerErrorOnUnknownError() {
            val postDto = PostChallengeDto("Test", "Description", TimeFrame.DAILY, true, 2)
            `when`(challengeService.addChallenge(postDto, TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(PostChallengeResult.UnknownError)

            authenticatedPost("/api/v1/challenges", postDto)
                .andExpect(status().isInternalServerError)
        }
    }

    @Nested
    @DisplayName("DELETE /challenges/{challengeId}")
    inner class DeleteChallenge {

        @Test
        @DisplayName("Should return 204 when challenge deleted successfully")
        fun shouldReturnNoContentWhenDeleted() {
            `when`(challengeService.deleteChallenge(1L, TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(DeleteChallengeResult.Success)

            authenticatedDelete("/api/v1/challenges/{challengeId}", 1L)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("Should return 204 when challenge not found")
        fun shouldReturnNoContentWhenNotFound() {
            `when`(challengeService.deleteChallenge(999L, TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(DeleteChallengeResult.NotFound)

            authenticatedDelete("/api/v1/challenges/{challengeId}", 999L)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("Should return 403 when unauthorized")
        fun shouldReturnForbiddenWhenUnauthorized() {
            `when`(challengeService.deleteChallenge(1L, TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(DeleteChallengeResult.Unauthorized)

            authenticatedDelete("/api/v1/challenges/{challengeId}", 1L)
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("Should return 500 on unknown error")
        fun shouldReturnInternalServerErrorOnUnknownError() {
            `when`(challengeService.deleteChallenge(1L, TEST_USER_ID, TEST_GUILD_ID))
                .thenReturn(DeleteChallengeResult.UnknownError)

            authenticatedDelete("/api/v1/challenges/{challengeId}", 1L)
                .andExpect(status().isInternalServerError)
        }

        @Test
        @DisplayName("Should return 400 for negative ID")
        fun shouldReturnBadRequestForNegativeId() {
            authenticatedDelete("/api/v1/challenges/{challengeId}", "-1")
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Should return 400 for zero ID")
        fun shouldReturnBadRequestForZeroId() {
            authenticatedDelete("/api/v1/challenges/{challengeId}", "0")
                .andExpect(status().isBadRequest)
        }
    }

    companion object {
        private const val TEST_USER_ID = "123456789"
        private const val TEST_GUILD_ID = "987654321"
    }
}
