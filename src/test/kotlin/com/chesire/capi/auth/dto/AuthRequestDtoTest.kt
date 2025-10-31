package com.chesire.capi.auth.dto

import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("AuthRequestDto Validation Tests")
class AuthRequestDtoTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    private fun AuthRequestDto.validate(): Set<ConstraintViolation<AuthRequestDto>> = validator.validate(this)

    private fun validDto() = AuthRequestDto(
        userId = 123456789012345678L,
        guildId = 987654321012345678L,
    )

    @Nested
    @DisplayName("Valid Data Tests")
    inner class ValidDataTests {
        @Test
        @DisplayName("Should pass validation with valid Discord IDs")
        fun shouldPassValidationWithValidDiscordIds() {
            val dto = validDto()

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }

        @Test
        @DisplayName("Should pass validation with minimum valid Discord IDs")
        fun shouldPassValidationWithMinimumValidDiscordIds() {
            val dto = AuthRequestDto(
                userId = 4194304L,  // Minimum Discord snowflake
                guildId = 4194304L,
            )

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }

        @Test
        @DisplayName("Should pass validation with maximum valid Discord IDs")
        fun shouldPassValidationWithMaximumValidDiscordIds() {
            val dto = AuthRequestDto(
                userId = Long.MAX_VALUE,
                guildId = Long.MAX_VALUE,
            )

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }
    }

    @Nested
    @DisplayName("Invalid User ID Tests")
    inner class InvalidUserIdTests {
        @Test
        @DisplayName("Should reject zero user ID")
        fun shouldRejectZeroUserId() {
            val dto = validDto().copy(userId = 0)

            val violations = dto.validate()

            assertFalse(violations.isEmpty())
            assertTrue(violations.any { it.propertyPath.toString() == "userId" })
            assertTrue(violations.any { it.message.contains("Invalid Discord user ID") })
        }

        @Test
        @DisplayName("Should reject negative user ID")
        fun shouldRejectNegativeUserId() {
            val dto = validDto().copy(userId = -123L)

            val violations = dto.validate()

            assertFalse(violations.isEmpty())
            assertTrue(violations.any { it.propertyPath.toString() == "userId" })
        }

        @Test
        @DisplayName("Should reject user ID below Discord minimum")
        fun shouldRejectUserIdBelowDiscordMinimum() {
            val dto = validDto().copy(userId = 123L)  // Below 4194304

            val violations = dto.validate()

            assertFalse(violations.isEmpty())
            assertTrue(violations.any { it.propertyPath.toString() == "userId" })
            assertTrue(violations.any { it.message.contains("Invalid Discord user ID") })
        }
    }

    @Nested
    @DisplayName("Invalid Guild ID Tests")
    inner class InvalidGuildIdTests {
        @Test
        @DisplayName("Should reject zero guild ID")
        fun shouldRejectZeroGuildId() {
            val dto = validDto().copy(guildId = 0)

            val violations = dto.validate()

            assertFalse(violations.isEmpty())
            assertTrue(violations.any { it.propertyPath.toString() == "guildId" })
            assertTrue(violations.any { it.message.contains("Invalid Discord guild ID") })
        }

        @Test
        @DisplayName("Should reject negative guild ID")
        fun shouldRejectNegativeGuildId() {
            val dto = validDto().copy(guildId = -456L)

            val violations = dto.validate()

            assertFalse(violations.isEmpty())
            assertTrue(violations.any { it.propertyPath.toString() == "guildId" })
        }

        @Test
        @DisplayName("Should reject guild ID below Discord minimum")
        fun shouldRejectGuildIdBelowDiscordMinimum() {
            val dto = validDto().copy(guildId = 999L)  // Below 4194304

            val violations = dto.validate()

            assertFalse(violations.isEmpty())
            assertTrue(violations.any { it.propertyPath.toString() == "guildId" })
            assertTrue(violations.any { it.message.contains("Invalid Discord guild ID") })
        }
    }

    @Nested
    @DisplayName("Multiple Field Validation Tests")
    inner class MultipleFieldValidationTests {
        @Test
        @DisplayName("Should report multiple violations for both invalid IDs")
        fun shouldReportMultipleViolationsForBothInvalidIds() {
            val dto = AuthRequestDto(
                userId = -1L,
                guildId = -2L,
            )

            val violations = dto.validate()

            assertEquals(2, violations.size)
            assertTrue(violations.any { it.propertyPath.toString() == "userId" })
            assertTrue(violations.any { it.propertyPath.toString() == "guildId" })
        }
    }
}