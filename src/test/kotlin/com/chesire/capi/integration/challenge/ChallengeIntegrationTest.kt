package com.chesire.capi.integration.challenge

import com.chesire.capi.integration.IntegrationTestBase
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Challenge API Integration Tests")
class ChallengeIntegrationTest : IntegrationTestBase() {

    @Nested
    @DisplayName("POST /api/v1/challenges")
    inner class CreateChallenge {

        @Test
        @DisplayName("Should create challenge with valid data")
        fun shouldCreateChallenge() {
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "name": "Daily Exercise",
                        "description": "Complete daily workout routine",
                        "timeFrame": "DAILY",
                        "allowPauses": true,
                        "cheats": 2
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/challenges")
            .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", equalTo("Daily Exercise"))
                .body("description", equalTo("Complete daily workout routine"))
                .body("timeFrame", equalTo("DAILY"))
                .body("allowPauses", equalTo(true))
                .body("cheats", equalTo(2))
        }

        @Test
        @DisplayName("Should reject challenge with name too short")
        fun shouldRejectShortName() {
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "name": "AB",
                        "description": "Valid description",
                        "timeFrame": "WEEKLY",
                        "allowPauses": false,
                        "cheats": 1
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/challenges")
            .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("errors.size()", equalTo(1))
                .body("errors[0].field", equalTo("name"))
        }

        @Test
        @DisplayName("Should reject challenge with too many cheats")
        fun shouldRejectTooManyCheats() {
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "name": "Valid Name",
                        "description": "Valid description",
                        "timeFrame": "MONTHLY",
                        "allowPauses": true,
                        "cheats": 10
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/challenges")
            .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("errors[0].field", equalTo("cheats"))
        }

        @Test
        @DisplayName("Should reject challenge without authentication")
        fun shouldRejectUnauthenticated() {
            given()
                .spec(given())
                .body("""
                    {
                        "name": "Test Challenge",
                        "description": "Test description",
                        "timeFrame": "DAILY",
                        "allowPauses": true,
                        "cheats": 0
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/challenges")
            .then()
                .statusCode(403)
        }

        @Test
        @DisplayName("Should create challenge with all TimeFrame values")
        fun shouldCreateWithAllTimeFrames() {
            listOf("DAILY", "WEEKLY", "MONTHLY").forEach { timeFrame ->
                given()
                    .spec(givenAuthenticated())
                    .body("""
                        {
                            "name": "$timeFrame Task",
                            "description": "Test $timeFrame challenge",
                            "timeFrame": "$timeFrame",
                            "allowPauses": true,
                            "cheats": 0
                        }
                    """.trimIndent())
                .`when`()
                    .post("$baseUrl/api/v1/challenges")
                .then()
                    .statusCode(200)
                    .body("timeFrame", equalTo(timeFrame))
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/challenges/{id}")
    inner class GetChallengeById {

        @Test
        @DisplayName("Should retrieve challenge by ID")
        fun shouldGetChallengeById() {
            val challengeId = createTestChallenge("Get By ID Test")

            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/challenges/$challengeId")
            .then()
                .statusCode(200)
                .body("id", equalTo(challengeId.toInt()))
                .body("name", equalTo("Get By ID Test"))
        }

        @Test
        @DisplayName("Should return 204 for non-existent challenge")
        fun shouldReturn204ForNonExistent() {
            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/challenges/999999")
            .then()
                .statusCode(204)
        }

        @Test
        @DisplayName("Should reject negative challenge ID")
        fun shouldRejectNegativeId() {
            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/challenges/-1")
            .then()
                .statusCode(400)
                .body("message", equalTo("Invalid request parameters"))
        }

        @Test
        @DisplayName("Should reject request without authentication")
        fun shouldRejectUnauthenticated() {
            given()
                .spec(given())
            .`when`()
                .get("$baseUrl/api/v1/challenges/1")
            .then()
                .statusCode(403)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/challenges/user/{userId}")
    inner class GetChallengesByUser {

        @Test
        @DisplayName("Should retrieve all challenges for user")
        fun shouldGetChallengesByUser() {
            createTestChallenge("User Challenge 1")
            createTestChallenge("User Challenge 2")

            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/challenges/user/123456789")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
        }

        @Test
        @DisplayName("Should return 204 when no challenges found for user")
        fun shouldReturn204WhenNoChallenges() {
            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/challenges/user/999999999")
            .then()
                .statusCode(204)
        }

        @Test
        @DisplayName("Should reject negative user ID")
        fun shouldRejectNegativeUserId() {
            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/challenges/user/-1")
            .then()
                .statusCode(400)
                .body("message", equalTo("Invalid request parameters"))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/challenges/{id}")
    inner class DeleteChallenge {

        @Test
        @DisplayName("Should delete challenge successfully")
        fun shouldDeleteChallenge() {
            val challengeId = createTestChallenge("To Be Deleted")

            given()
                .spec(givenAuthenticated())
            .`when`()
                .delete("$baseUrl/api/v1/challenges/$challengeId")
            .then()
                .statusCode(204)

            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/challenges/$challengeId")
            .then()
                .statusCode(204)
        }

        @Test
        @DisplayName("Should return 204 when deleting non-existent challenge")
        fun shouldReturn204ForNonExistent() {
            given()
                .spec(givenAuthenticated())
            .`when`()
                .delete("$baseUrl/api/v1/challenges/999999")
            .then()
                .statusCode(204)
        }

        @Test
        @DisplayName("Should reject delete without authentication")
        fun shouldRejectUnauthenticated() {
            given()
                .spec(given())
            .`when`()
                .delete("$baseUrl/api/v1/challenges/1")
            .then()
                .statusCode(403)
        }
    }

    private fun createTestChallenge(name: String): String {
        return given()
            .spec(givenAuthenticated())
            .body("""
                {
                    "name": "$name",
                    "description": "Integration test challenge",
                    "timeFrame": "DAILY",
                    "allowPauses": true,
                    "cheats": 1
                }
            """.trimIndent())
            .post("$baseUrl/api/v1/challenges")
            .then()
            .statusCode(200)
            .extract()
            .path<Int>("id")
            .toString()
    }
}
