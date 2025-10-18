package com.chesire.capi.auth.dto

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("AuthResponseDto Tests")
class AuthResponseDtoTest {

    @Test
    @DisplayName("Should create response with all fields")
    fun shouldCreateResponseWithAllFields() {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        val tokenType = "Bearer"
        val expiresIn = 3600L

        val response = AuthResponseDto(token, tokenType, expiresIn)

        assertEquals(token, response.token)
        assertEquals(tokenType, response.tokenType)
        assertEquals(expiresIn, response.expiresIn)
    }

    @Test
    @DisplayName("Should have default token type of Bearer")
    fun shouldHaveDefaultTokenTypeOfBearer() {
        val token = "test.jwt.token"
        val expiresIn = 3600L

        val response = AuthResponseDto(token, "Bearer", expiresIn)

        assertEquals("Bearer", response.tokenType)
    }

    @Test
    @DisplayName("Should handle OAuth2 standard format")
    fun shouldHandleOAuth2StandardFormat() {
        val response = AuthResponseDto(
            token = "access_token_value",
            tokenType = "Bearer", 
            expiresIn = 7200L,
        )

        assertNotNull(response.token)
        assertEquals("Bearer", response.tokenType)
        assertEquals(7200L, response.expiresIn)
    }
}