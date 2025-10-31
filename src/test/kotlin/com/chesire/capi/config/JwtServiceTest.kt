package com.chesire.capi.config

import com.chesire.capi.config.jwt.JwtService
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

@DisplayName("JwtService Tests")
class JwtServiceTest {
    private lateinit var jwtService: JwtService

    @BeforeEach
    fun setUp() {
        jwtService = JwtService()
        ReflectionTestUtils.setField(jwtService, "secretKey", "testSecretKeyForJwtGenerationThatIsLongEnough123456789")
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L)
        ReflectionTestUtils.setField(jwtService, "jwtExpirationSeconds", 3600L)
    }

    @Nested
    @DisplayName("Token Generation Tests")
    inner class TokenGenerationTests {
        @Test
        @DisplayName("Should generate valid JWT token with all claims")
        fun shouldGenerateValidTokenWithAllClaims() {
            val userId = 123456789012345678L
            val guildId = 987654321012345678L

            val token = jwtService.generateToken(userId, guildId)

            assertNotNull(token)
            assertTrue(token.isNotBlank())
            assertTrue(jwtService.isTokenValid(token))
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        fun shouldGenerateDifferentTokensForDifferentUsers() {
            val token1 = jwtService.generateToken(123L, 456L)
            val token2 = jwtService.generateToken(789L, 456L)

            assertTrue(token1 != token2)
        }
    }

    @Nested
    @DisplayName("Claim Extraction Tests")
    inner class ClaimExtractionTests {
        private lateinit var validToken: String

        @BeforeEach
        fun setUp() {
            validToken = jwtService.generateToken(123456789012345678L, 987654321012345678L)
        }

        @Test
        @DisplayName("Should extract user ID correctly")
        fun shouldExtractUserIdCorrectly() {
            val extractedUserId = jwtService.extractUserId(validToken)

            assertEquals(123456789012345678L, extractedUserId)
        }

        @Test
        @DisplayName("Should extract guild ID correctly")
        fun shouldExtractGuildIdCorrectly() {
            val extractedGuildId = jwtService.extractGuildId(validToken)

            assertEquals(987654321012345678L, extractedGuildId)
        }

        @Test
        @DisplayName("Should extract scope correctly")
        fun shouldExtractScopeCorrectly() {
            val extractedScope = jwtService.extractScope(validToken)

            assertEquals(JwtService.SCOPE_VALUE, extractedScope)
        }

        @Test
        @DisplayName("Should extract JWT ID")
        fun shouldExtractJwtId() {
            val extractedJwtId = jwtService.extractJwtId(validToken)

            assertNotNull(extractedJwtId)
            assertTrue(extractedJwtId!!.isNotBlank())
        }
    }

    @Nested
    @DisplayName("Scope Validation Tests")
    inner class ScopeValidationTests {
        private lateinit var validToken: String

        @BeforeEach
        fun setUp() {
            validToken = jwtService.generateToken(123456789012345678L, 987654321012345678L)
        }

        @Test
        @DisplayName("Should validate correct scope")
        fun shouldValidateCorrectScope() {
            val hasScope = jwtService.hasScope(validToken, JwtService.SCOPE_VALUE)

            assertTrue(hasScope)
        }

        @Test
        @DisplayName("Should reject incorrect scope")
        fun shouldRejectIncorrectScope() {
            val hasScope = jwtService.hasScope(validToken, "invalid:scope")

            assertFalse(hasScope)
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    inner class ErrorHandlingTests {
        @Test
        @DisplayName("Should return null for invalid token")
        fun shouldReturnNullForInvalidToken() {
            val invalidToken = "invalid.jwt.token"

            assertNull(jwtService.extractUserId(invalidToken))
            assertNull(jwtService.extractGuildId(invalidToken))
            assertNull(jwtService.extractScope(invalidToken))
            assertNull(jwtService.extractJwtId(invalidToken))
        }

        @Test
        @DisplayName("Should return false for invalid token validation")
        fun shouldReturnFalseForInvalidTokenValidation() {
            val invalidToken = "invalid.jwt.token"

            assertFalse(jwtService.isTokenValid(invalidToken))
            assertFalse(jwtService.hasScope(invalidToken, JwtService.SCOPE_VALUE))
        }

        @Test
        @DisplayName("Should handle empty token gracefully")
        fun shouldHandleEmptyTokenGracefully() {
            assertNull(jwtService.extractUserId(""))
            assertFalse(jwtService.isTokenValid(""))
        }
    }
}
