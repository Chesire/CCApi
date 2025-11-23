package com.chesire.capi.integration.event

import com.chesire.capi.integration.IntegrationTestBase
import io.restassured.RestAssured.given
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Event API Integration Tests")
class EventIntegrationTest : IntegrationTestBase() {

    @Nested
    @DisplayName("POST /api/v1/events")
    inner class CreateEvent {

        @Test
        @DisplayName("Should create event with valid data")
        fun shouldCreateEvent() {
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "key": "challenge_completed",
                        "userId": 123456789
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/events")
            .then()
                .statusCode(200)
                .body("key", equalTo("challenge_completed"))
                .body("userId", equalTo(123456789))
                .body("guildId", notNullValue())
                .body("year", notNullValue())
                .body("count", notNullValue())
        }

        @Test
        @DisplayName("Should reject event with blank key")
        fun shouldRejectBlankKey() {
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "key": "",
                        "userId": 123456789
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/events")
            .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("errors.size()", greaterThan(0))
        }

        @Test
        @DisplayName("Should reject event with negative user ID")
        fun shouldRejectNegativeUserId() {
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "key": "valid_key",
                        "userId": -1
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/events")
            .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
        }

        @Test
        @DisplayName("Should reject event with key too long")
        fun shouldRejectKeyTooLong() {
            val longKey = "a".repeat(31)
            
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "key": "$longKey",
                        "userId": 123456789
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/events")
            .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("errors[0].field", equalTo("key"))
        }

        @Test
        @DisplayName("Should reject event without authentication")
        fun shouldRejectUnauthenticated() {
            given()
                .spec(given())
                .body("""
                    {
                        "key": "test_key",
                        "userId": 123456789
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/events")
            .then()
                .statusCode(403)
        }
    }
}
