package com.chesire.capi.auth

import com.chesire.capi.auth.dto.AuthRequestDto
import com.chesire.capi.config.TokenRateLimiter
import com.chesire.capi.config.jwt.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class, excludeAutoConfiguration = [SecurityAutoConfiguration::class])
@TestPropertySource(properties = ["capi.auth.api-key=test-api-key-for-unit-testing"])
@DisplayName("AuthController Basic Tests")
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var jwtService: JwtService

    @MockBean
    private lateinit var rateLimiter: TokenRateLimiter

    @Test
    @DisplayName("Should reject invalid API key due to validation")
    fun shouldRejectInvalidApiKeyDueToValidation() {
        val request = AuthRequestDto(
            userId = 123456789012345678L,
            guildId = 987654321012345678L,
        )

        mockMvc.perform(
            post("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-Key", "invalid-api-key")  // Too short - triggers validation
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Invalid request parameters"))
            .andExpect(jsonPath("$.errors[0].rejectedValue").value("[REDACTED]"))
    }

    @Test
    @DisplayName("Should reject wrong API key value")
    fun shouldRejectWrongApiKeyValue() {
        val request = AuthRequestDto(
            userId = 123456789012345678L,
            guildId = 987654321012345678L,
        )

        mockMvc.perform(
            post("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-Key", "wrong-api-key-but-long-enough-for-validation")  // Valid length, wrong value
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Authentication failed"))
    }

    @Test
    @DisplayName("Should reject missing API key header")
    fun shouldRejectMissingApiKeyHeader() {
        val request = AuthRequestDto(
            userId = 123456789012345678L,
            guildId = 987654321012345678L,
        )

        mockMvc.perform(
            post("/api/v1/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isUnauthorized) // Should have matching error if missing x-api-key
            .andExpect(jsonPath("$.message").value("Authentication failed"))
    }
}
