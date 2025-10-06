package com.chesire.capi.event.dto

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

@DisplayName("PostEventDto Validation Tests")
class PostEventDtoTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    private fun PostEventDto.validate(): Set<ConstraintViolation<PostEventDto>> = validator.validate(this)

    private fun Set<ConstraintViolation<PostEventDto>>.hasMessageContaining(message: String): Boolean =
        any { it.message.contains(message, ignoreCase = true) }

    @Nested
    @DisplayName("Valid Data Tests")
    inner class ValidDataTests {
        @Test
        @DisplayName("Should pass validation with all valid fields")
        fun shouldPassValidationWithValidData() {
            val dto = validDto()

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }

        @Test
        @DisplayName("Should pass validation with minimum valid lengths")
        fun shouldPassValidationWithMinimumLengths() {
            val dto =
                validDto().copy(
                    key = "A",
                    value = "X",
                )

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }

        @Test
        @DisplayName("Should pass validation with maximum valid lengths")
        fun shouldPassValidationWithMaximumLengths() {
            val dto =
                validDto().copy(
                    key = "A".repeat(30),
                    value = "B".repeat(200),
                )

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }

        @Test
        @DisplayName("Should pass validation with minimum valid userId")
        fun shouldPassValidationWithMinimumUserId() {
            val dto = validDto().copy(userId = 1L)

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }
    }

    @Nested
    @DisplayName("Key Validation Tests")
    inner class KeyValidationTests {
        @Test
        @DisplayName("Should reject empty key")
        fun shouldRejectEmptyKey() {
            val dto = validDto().copy(key = "")

            val violations = dto.validate()

            assertEquals(2, violations.size)
            val messages = violations.map { it.message }
            assertTrue(messages.contains("Key is required and cannot be blank"))
            assertTrue(messages.contains("Key must be between 1 and 30 characters"))
        }

        @Test
        @DisplayName("Should reject blank key with only spaces")
        fun shouldRejectBlankKeyWithSpaces() {
            val dto = validDto().copy(key = "   ")

            val violations = dto.validate()

            assertEquals(1, violations.size)
            assertEquals("Key is required and cannot be blank", violations.first().message)
        }

        @Test
        @DisplayName("Should reject key that is too long")
        fun shouldRejectKeyTooLong() {
            val dto = validDto().copy(key = "A".repeat(31))

            val violations = dto.validate()

            assertEquals(1, violations.size)
            assertEquals("Key must be between 1 and 30 characters", violations.first().message)
        }

        @Test
        @DisplayName("Should reject invalid keys")
        fun shouldRejectInvalidKeys() {
            invalidKeys.forEach { (invalidKey, description) ->
                val dto = validDto().copy(key = invalidKey)

                val violations = dto.validate()

                assertFalse(violations.isEmpty(), "Expected violations for $description")
                assertTrue(violations.hasMessageContaining("key") || violations.hasMessageContaining("blank"))
            }
        }
    }

    @Nested
    @DisplayName("Value Validation Tests")
    inner class ValueValidationTests {
        @Test
        @DisplayName("Should reject empty value")
        fun shouldRejectEmptyValue() {
            val dto = validDto().copy(value = "")

            val violations = dto.validate()

            assertEquals(2, violations.size)
            val messages = violations.map { it.message }
            assertTrue(messages.contains("Value is required and cannot be blank"))
            assertTrue(messages.contains("Value must be between 1 and 200 characters"))
        }

        @Test
        @DisplayName("Should reject blank value with only spaces")
        fun shouldRejectBlankValueWithSpaces() {
            val dto = validDto().copy(value = "     ")

            val violations = dto.validate()

            assertEquals(1, violations.size)
            assertEquals("Value is required and cannot be blank", violations.first().message)
        }

        @Test
        @DisplayName("Should reject value that is too long")
        fun shouldRejectValueTooLong() {
            val dto = validDto().copy(value = "A".repeat(201))

            val violations = dto.validate()

            assertEquals(1, violations.size)
            assertEquals("Value must be between 1 and 200 characters", violations.first().message)
        }

        @Test
        @DisplayName("Should accept value with mixed content including spaces")
        fun shouldAcceptValueWithMixedContent() {
            val dto =
                validDto().copy(
                    value = "This is a valid value with spaces, punctuation! And numbers 123.",
                )

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }
    }

    @Nested
    @DisplayName("UserId Validation Tests")
    inner class UserIdValidationTests {
        @Test
        @DisplayName("Should reject zero userId")
        fun shouldRejectZeroUserId() {
            val dto = validDto().copy(userId = 0L)

            val violations = dto.validate()

            assertEquals(1, violations.size)
            // @Positive constraint default message is "must be greater than 0"
            assertTrue(violations.any { it.message.contains("greater than 0") || it.message.contains("positive") })
        }

        @Test
        @DisplayName("Should reject negative userId")
        fun shouldRejectNegativeUserId() {
            val dto = validDto().copy(userId = -1L)

            val violations = dto.validate()

            assertEquals(1, violations.size)
            // @Positive constraint default message is "must be greater than 0"
            assertTrue(violations.any { it.message.contains("greater than 0") || it.message.contains("positive") })
        }

        @Test
        @DisplayName("Should reject invalid userId values")
        fun shouldRejectInvalidUserIdValues() {
            invalidUserIds.forEach { (invalidUserId, description) ->
                val dto = validDto().copy(userId = invalidUserId)

                val violations = dto.validate()

                assertFalse(violations.isEmpty(), "Expected violations for $description")
                assertTrue(
                    violations.any { it.message.contains("greater than 0") || it.message.contains("positive") },
                    "Expected positive validation message for $description. Got: ${violations.map { it.message }}",
                )
            }
        }

        @Test
        @DisplayName("Should accept large positive userId")
        fun shouldAcceptLargePositiveUserId() {
            val dto = validDto().copy(userId = Long.MAX_VALUE)

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }
    }

    @Nested
    @DisplayName("Multiple Field Validation Tests")
    inner class MultipleFieldValidationTests {
        @Test
        @DisplayName("Should report all validation errors when multiple fields are invalid")
        fun shouldReportAllValidationErrors() {
            val dto =
                PostEventDto(
                    key = "",
                    value = "",
                    userId = 0L,
                )

            val violations = dto.validate()

            assertEquals(5, violations.size)

            val violatedFields = violations.map { it.propertyPath.toString() }.toSet()
            assertTrue(violatedFields.contains("key"))
            assertTrue(violatedFields.contains("value"))
            assertTrue(violatedFields.contains("userId"))
        }

        @Test
        @DisplayName("Should handle maximum violations across all fields")
        fun shouldHandleMaximumViolationsAcrossAllFields() {
            val dto =
                PostEventDto(
                    key = "X".repeat(50),
                    value = "Y".repeat(300),
                    userId = -100L,
                )

            val violations = dto.validate()

            assertEquals(3, violations.size)
            assertTrue(violations.hasMessageContaining("key"))
            assertTrue(violations.hasMessageContaining("value"))
            assertTrue(violations.any { it.message.contains("greater than 0") || it.message.contains("positive") })
        }
    }

    @Nested
    @DisplayName("Special Characters and Content Tests")
    inner class SpecialCharactersTests {
        @Test
        @DisplayName("Should accept keys with special characters")
        fun shouldAcceptKeysWithSpecialCharacters() {
            val specialKeys =
                listOf(
                    "challenge_failed",
                    "event-type-123",
                    "user_action!",
                    "test&go",
                    "action#1",
                )

            specialKeys.forEach { specialKey ->
                val dto = validDto().copy(key = specialKey)

                val violations = dto.validate()

                assertTrue(violations.isEmpty(), "Expected no violations for special key: '$specialKey'")
            }
        }

        @Test
        @DisplayName("Should accept values with various content types")
        fun shouldAcceptValuesWithVariousContent() {
            val complexValues =
                listOf(
                    "Value with numbers: 123, 456!",
                    "Value with symbols: @#\$%^&*()",
                    "Value with quotes: 'single' and \"double\"",
                    "Value with accents: café, naïve, résumé",
                    "JSON-like: {\"type\":\"challenge\",\"id\":123}",
                )

            complexValues.forEach { value ->
                val dto = validDto().copy(value = value)

                val violations = dto.validate()

                assertTrue(violations.isEmpty(), "Expected no violations for value: '$value'")
            }
        }
    }

    @Nested
    @DisplayName("Exact Boundary Tests")
    inner class ExactBoundaryTests {
        @Test
        @DisplayName("Should test exact key length boundaries")
        fun shouldTestExactKeyLengthBoundaries() {
            val boundaryTests =
                mapOf(
                    0 to false,
                    1 to true,
                    30 to true,
                    31 to false,
                )

            boundaryTests.forEach { (length, shouldBeValid) ->
                val dto = validDto().copy(key = "A".repeat(length))

                val violations = dto.validate()

                if (shouldBeValid) {
                    assertTrue(violations.isEmpty(), "Expected no violations for key length: $length")
                } else {
                    assertFalse(violations.isEmpty(), "Expected violations for key length: $length")
                    if (length == 0) {
                        assertTrue(violations.hasMessageContaining("blank") || violations.hasMessageContaining("between 1 and 30"))
                    } else {
                        assertTrue(violations.hasMessageContaining("between 1 and 30 characters"))
                    }
                }
            }
        }

        @Test
        @DisplayName("Should test exact value length boundaries")
        fun shouldTestExactValueLengthBoundaries() {
            val boundaryTests =
                mapOf(
                    0 to false,
                    1 to true,
                    200 to true,
                    201 to false,
                )

            boundaryTests.forEach { (length, shouldBeValid) ->
                val dto = validDto().copy(value = "B".repeat(length))

                val violations = dto.validate()

                if (shouldBeValid) {
                    assertTrue(violations.isEmpty(), "Expected no violations for value length: $length")
                } else {
                    assertFalse(violations.isEmpty(), "Expected violations for value length: $length")
                    if (length == 0) {
                        assertTrue(violations.hasMessageContaining("blank") || violations.hasMessageContaining("between 1 and 200"))
                    } else {
                        assertTrue(violations.hasMessageContaining("between 1 and 200 characters"))
                    }
                }
            }
        }

        @Test
        @DisplayName("Should test exact userId boundaries")
        fun shouldTestExactUserIdBoundaries() {
            val boundaryTests =
                mapOf(
                    -1L to false,
                    0L to false,
                    1L to true,
                    Long.MAX_VALUE to true,
                )

            boundaryTests.forEach { (userId, shouldBeValid) ->
                val dto = validDto().copy(userId = userId)

                val violations = dto.validate()

                if (shouldBeValid) {
                    assertTrue(violations.isEmpty(), "Expected no violations for userId: $userId")
                } else {
                    assertFalse(violations.isEmpty(), "Expected violations for userId: $userId")
                    assertTrue(violations.any { it.message.contains("greater than 0") || it.message.contains("positive") })
                }
            }
        }
    }

    @Nested
    @DisplayName("Data Class Functionality Tests")
    inner class DataClassFunctionalityTests {
        @Test
        @DisplayName("Should support copy with field changes")
        fun shouldSupportCopyWithFieldChanges() {
            val original = validDto()

            val modified = original.copy(key = "new_key")

            assertEquals("new_key", modified.key)
            assertEquals(original.value, modified.value)
            assertEquals(original.userId, modified.userId)
        }

        @Test
        @DisplayName("Should implement equals correctly")
        fun shouldImplementEqualsCorrectly() {
            val dto1 = validDto()
            val dto2 = validDto()
            val dto3 = validDto().copy(key = "different_key")

            assertEquals(dto1, dto2)
            assertTrue(dto1 != dto3)
        }

        @Test
        @DisplayName("Should implement toString correctly")
        fun shouldImplementToStringCorrectly() {
            val dto = validDto()

            val toString = dto.toString()

            assertTrue(toString.contains("PostEventDto"))
            assertTrue(toString.contains("challenge_completed"))
            assertTrue(toString.contains("gym_challenge"))
            assertTrue(toString.contains("123"))
        }
    }

    companion object {
        fun validDto() =
            PostEventDto(
                key = "challenge_completed",
                value = "gym_challenge",
                userId = 123L,
            )

        val invalidKeys =
            listOf(
                "" to "empty string",
                "A".repeat(31) to "too long",
                "   " to "whitespace only",
                "\t\n" to "tabs and newlines",
            )

        val invalidUserIds =
            listOf(
                -100L to "large negative",
                -1L to "small negative",
                0L to "zero value",
            )
    }
}
