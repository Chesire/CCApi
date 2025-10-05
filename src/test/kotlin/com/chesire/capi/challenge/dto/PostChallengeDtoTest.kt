package com.chesire.capi.challenge.dto

import com.chesire.capi.models.TimeFrame
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

@DisplayName("PostChallengeDto Validation Tests")
class PostChallengeDtoTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    private fun PostChallengeDto.validate(): Set<ConstraintViolation<PostChallengeDto>> =
        validator.validate(this)

    private fun Set<ConstraintViolation<PostChallengeDto>>.hasMessageContaining(message: String): Boolean =
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
            val dto = validDto().copy(
                name = "ABC",
                description = "X"
            )

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }

        @Test
        @DisplayName("Should pass validation with maximum valid lengths")
        fun shouldPassValidationWithMaximumLengths() {
            val dto = validDto().copy(
                name = "A".repeat(20),
                description = "B".repeat(200)
            )

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }

        @Test
        @DisplayName("Should pass validation with zero cheats")
        fun shouldPassValidationWithZeroCheats() {
            val dto = validDto().copy(cheats = 0)

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }

        @Test
        @DisplayName("Should pass validation with maximum cheats")
        fun shouldPassValidationWithMaximumCheats() {
            val dto = validDto().copy(cheats = 4)

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }
    }

    @Nested
    @DisplayName("Name Validation Tests")
    inner class NameValidationTests {

        @Test
        @DisplayName("Should reject empty name")
        fun shouldRejectEmptyName() {
            val dto = validDto().copy(name = "")

            val violations = dto.validate()

            assertEquals(2, violations.size)
            val messages = violations.map { it.message }
            assertTrue(messages.contains("Challenge name is required and cannot be blank"))
            assertTrue(messages.contains("Challenge name must be between 3 and 20 characters"))
        }

        @Test
        @DisplayName("Should reject blank name with only spaces")
        fun shouldRejectBlankNameWithSpaces() {
            val dto = validDto().copy(name = "   ")

            val violations = dto.validate()

            assertEquals(1, violations.size)
            assertEquals("Challenge name is required and cannot be blank", violations.first().message)
        }

        @Test
        @DisplayName("Should reject invalid names")
        fun shouldRejectInvalidNames() {
            invalidNames.forEach { (invalidName, description) ->
                val dto = validDto().copy(name = invalidName)

                val violations = dto.validate()

                assertFalse(violations.isEmpty(), "Expected violations for $description")
                assertTrue(violations.hasMessageContaining("name") || violations.hasMessageContaining("blank"))
            }
        }
    }

    @Nested
    @DisplayName("Description Validation Tests")
    inner class DescriptionValidationTests {

        @Test
        @DisplayName("Should reject empty description")
        fun shouldRejectEmptyDescription() {
            val dto = validDto().copy(description = "")

            val violations = dto.validate()

            assertEquals(2, violations.size)
            val messages = violations.map { it.message }
            assertTrue(messages.contains("Challenge description is required and cannot be blank"))
            assertTrue(messages.contains("Challenge description must be between 1 and 200 characters"))
        }

        @Test
        @DisplayName("Should reject blank description with only spaces")
        fun shouldRejectBlankDescriptionWithSpaces() {
            val dto = validDto().copy(description = "     ")

            val violations = dto.validate()

            assertEquals(1, violations.size)
            assertEquals("Challenge description is required and cannot be blank", violations.first().message)
        }

        @Test
        @DisplayName("Should reject description that is too long")
        fun shouldRejectDescriptionTooLong() {
            val dto = validDto().copy(description = "A".repeat(201))

            val violations = dto.validate()

            assertEquals(1, violations.size)
            assertEquals("Challenge description must be between 1 and 200 characters", violations.first().message)
        }

        @Test
        @DisplayName("Should accept description with mixed content including spaces")
        fun shouldAcceptDescriptionWithMixedContent() {
            val dto = validDto().copy(
                description = "This is a valid description with spaces, punctuation! And numbers 123."
            )

            val violations = dto.validate()

            assertTrue(violations.isEmpty())
        }
    }

    @Nested
    @DisplayName("Cheats Validation Tests")
    inner class CheatsValidationTests {

        @Test
        @DisplayName("Should reject negative cheats")
        fun shouldRejectNegativeCheats() {
            val dto = validDto().copy(cheats = -1)

            val violations = dto.validate()

            assertEquals(1, violations.size)
            assertEquals("Number of cheats cannot be negative", violations.first().message)
        }

        @Test
        @DisplayName("Should reject cheats exceeding maximum")
        fun shouldRejectCheatsExceedingMaximum() {
            val dto = validDto().copy(cheats = 5)

            val violations = dto.validate()

            assertEquals(1, violations.size)
            assertEquals("Number of cheats cannot exceed 4", violations.first().message)
        }

        @Test
        @DisplayName("Should reject invalid cheat values")
        fun shouldRejectInvalidCheatValues() {
            invalidCheats.forEach { (invalidValue, description) ->
                val dto = validDto().copy(cheats = invalidValue)

                val violations = dto.validate()

                assertFalse(violations.isEmpty(), "Expected violations for $description")
                assertTrue(
                    violations.hasMessageContaining("negative") || violations.hasMessageContaining("exceed"),
                    "Expected cheat-related validation message for $description"
                )
            }
        }
    }

    @Nested
    @DisplayName("TimeFrame Validation Tests")
    inner class TimeFrameValidationTests {

        @Test
        @DisplayName("Should accept all valid TimeFrame values")
        fun shouldAcceptAllValidTimeFrameValues() {
            TimeFrame.entries.forEach { timeFrame ->
                val dto = validDto().copy(timeFrame = timeFrame)

                val violations = dto.validate()

                assertTrue(violations.isEmpty(), "Expected no violations for TimeFrame: $timeFrame")
            }
        }
    }

    @Nested
    @DisplayName("Multiple Field Validation Tests")
    inner class MultipleFieldValidationTests {

        @Test
        @DisplayName("Should report all validation errors when multiple fields are invalid")
        fun shouldReportAllValidationErrors() {
            val dto = PostChallengeDto(
                name = "",
                description = "",
                timeFrame = TimeFrame.DAILY,
                allowPauses = true,
                cheats = -5
            )

            val violations = dto.validate()

            assertEquals(5, violations.size)

            val violatedFields = violations.map { it.propertyPath.toString() }.toSet()
            assertTrue(violatedFields.contains("name"))
            assertTrue(violatedFields.contains("description"))
            assertTrue(violatedFields.contains("cheats"))
        }

        @Test
        @DisplayName("Should handle maximum violations across all fields")
        fun shouldHandleMaximumViolationsAcrossAllFields() {
            val dto = PostChallengeDto(
                name = "X".repeat(50),
                description = "Y".repeat(300),
                timeFrame = TimeFrame.WEEKLY,
                allowPauses = false,
                cheats = 15
            )

            val violations = dto.validate()

            assertEquals(3, violations.size)
            assertTrue(violations.hasMessageContaining("name"))
            assertTrue(violations.hasMessageContaining("description"))
            assertTrue(violations.hasMessageContaining("cheats"))
        }
    }

    @Nested
    @DisplayName("Special Characters and Content Tests")
    inner class SpecialCharactersTests {

        @Test
        @DisplayName("Should accept names with special characters")
        fun shouldAcceptNamesWithSpecialCharacters() {
            val specialNames = listOf(
                "Test-123",
                "Challenge_2",
                "My Test!",
                "Test & Go",
                "Challenge#1"
            )

            specialNames.forEach { specialName ->
                val dto = validDto().copy(name = specialName)

                val violations = dto.validate()

                assertTrue(violations.isEmpty(), "Expected no violations for special name: '$specialName'")
            }
        }

        @Test
        @DisplayName("Should accept descriptions with various content types")
        fun shouldAcceptDescriptionsWithVariousContent() {
            val complexDescriptions = listOf(
                "Challenge with numbers: 123, 456!",
                "Challenge with symbols: @#\$%^&*()",
                "Challenge with quotes: 'single' and \"double\"",
                "Challenge with accents: café, naïve, résumé"
            )

            complexDescriptions.forEach { description ->
                val dto = validDto().copy(description = description)

                val violations = dto.validate()

                assertTrue(violations.isEmpty(), "Expected no violations for description: '$description'")
            }
        }
    }

    @Nested
    @DisplayName("Exact Boundary Tests")
    inner class ExactBoundaryTests {

        @Test
        @DisplayName("Should test exact name length boundaries")
        fun shouldTestExactNameLengthBoundaries() {
            val boundaryTests = mapOf(
                2 to false,
                3 to true,
                20 to true,
                21 to false
            )

            boundaryTests.forEach { (length, shouldBeValid) ->
                val dto = validDto().copy(name = "A".repeat(length))

                val violations = dto.validate()

                if (shouldBeValid) {
                    assertTrue(violations.isEmpty(), "Expected no violations for name length: $length")
                } else {
                    assertFalse(violations.isEmpty(), "Expected violations for name length: $length")
                    assertTrue(violations.hasMessageContaining("between 3 and 20 characters"))
                }
            }
        }

        @Test
        @DisplayName("Should test exact cheat value boundaries")
        fun shouldTestExactCheatValueBoundaries() {
            val boundaryTests = mapOf(
                -1 to false,
                0 to true,
                4 to true,
                5 to false
            )

            boundaryTests.forEach { (cheats, shouldBeValid) ->
                val dto = validDto().copy(cheats = cheats)

                val violations = dto.validate()

                if (shouldBeValid) {
                    assertTrue(violations.isEmpty(), "Expected no violations for cheats: $cheats")
                } else {
                    assertFalse(violations.isEmpty(), "Expected violations for cheats: $cheats")
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

            val modified = original.copy(name = "New Name")

            assertEquals("New Name", modified.name)
            assertEquals(original.description, modified.description)
            assertEquals(original.timeFrame, modified.timeFrame)
        }

        @Test
        @DisplayName("Should implement equals correctly")
        fun shouldImplementEqualsCorrectly() {
            val dto1 = validDto()
            val dto2 = validDto()
            val dto3 = validDto().copy(name = "Different")

            assertEquals(dto1, dto2)
            assertTrue(dto1 != dto3)
        }

        @Test
        @DisplayName("Should implement toString correctly")
        fun shouldImplementToStringCorrectly() {
            val dto = validDto()

            val toString = dto.toString()

            assertTrue(toString.contains("PostChallengeDto"))
            assertTrue(toString.contains("Valid Challenge"))
            assertTrue(toString.contains("DAILY"))
        }
    }

    companion object {
        fun validDto() = PostChallengeDto(
            name = "Valid Challenge",
            description = "This is a valid description",
            timeFrame = TimeFrame.DAILY,
            allowPauses = true,
            cheats = 2
        )

        val invalidNames = listOf(
            "" to "empty string",
            "AB" to "too short",
            "A".repeat(21) to "too long",
            "   " to "whitespace only",
            "\t\n" to "tabs and newlines"
        )

        val invalidCheats = listOf(
            -10 to "large negative",
            -1 to "small negative",
            5 to "exceeds maximum",
            100 to "way too large"
        )
    }
}
