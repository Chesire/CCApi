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
                        "value": "daily_workout_done",
                        "userId": 123456789
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/events")
            .then()
                .statusCode(200)
                .body("key", equalTo("challenge_completed"))
                .body("value", equalTo("daily_workout_done"))
                .body("userId", equalTo(123456789))
                .body("timestamp", notNullValue())
        }

        @Test
        @DisplayName("Should reject event with blank key")
        fun shouldRejectBlankKey() {
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "key": "",
                        "value": "some_value",
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
        @DisplayName("Should reject event with blank value")
        fun shouldRejectBlankValue() {
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "key": "valid_key",
                        "value": "",
                        "userId": 123456789
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/events")
            .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
        }

        @Test
        @DisplayName("Should reject event with negative user ID")
        fun shouldRejectNegativeUserId() {
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "key": "valid_key",
                        "value": "valid_value",
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
                        "value": "valid_value",
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
        @DisplayName("Should reject event with value too long")
        fun shouldRejectValueTooLong() {
            val longValue = "a".repeat(201)
            
            given()
                .spec(givenAuthenticated())
                .body("""
                    {
                        "key": "valid_key",
                        "value": "$longValue",
                        "userId": 123456789
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/events")
            .then()
                .statusCode(400)
                .body("message", equalTo("Validation failed"))
                .body("errors[0].field", equalTo("value"))
        }

        @Test
        @DisplayName("Should reject event without authentication")
        fun shouldRejectUnauthenticated() {
            given()
                .spec(given())
                .body("""
                    {
                        "key": "test_key",
                        "value": "test_value",
                        "userId": 123456789
                    }
                """.trimIndent())
            .`when`()
                .post("$baseUrl/api/v1/events")
            .then()
                .statusCode(403)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/events/{key}")
    inner class GetEventsByKey {

        @Test
        @DisplayName("Should retrieve events by key")
        fun shouldGetEventsByKey() {
            val eventKey = "test_event_${System.currentTimeMillis()}"
            
            createTestEvent(eventKey, "value1")
            createTestEvent(eventKey, "value2")
            createTestEvent(eventKey, "value3")

            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/events/$eventKey")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].key", equalTo(eventKey))
        }

        @Test
        @DisplayName("Should return events with different values for same key")
        fun shouldReturnMultipleValuesForSameKey() {
            val eventKey = "multi_value_${System.currentTimeMillis()}"
            
            createTestEvent(eventKey, "first_value")
            createTestEvent(eventKey, "second_value")

            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/events/$eventKey")
            .then()
                .statusCode(200)
                .body("size()", equalTo(2))
        }

        @Test
        @DisplayName("Should return empty list for non-existent key")
        fun shouldReturnEmptyForNonExistentKey() {
            val nonExistentKey = "non_existent_${System.currentTimeMillis()}"

            given()
                .spec(givenAuthenticated())
            .`when`()
                .get("$baseUrl/api/v1/events/$nonExistentKey")
            .then()
                .statusCode(200)
                .body("size()", equalTo(0))
        }

        @Test
        @DisplayName("Should reject request without authentication")
        fun shouldRejectUnauthenticated() {
            given()
                .spec(given())
            .`when`()
                .get("$baseUrl/api/v1/events/some_key")
            .then()
                .statusCode(403)
        }

        @Test
        @DisplayName("Should only return events for authenticated guild")
        fun shouldFilterByGuild() {
            val eventKey = "guild_filtered_${System.currentTimeMillis()}"
            
            createTestEvent(eventKey, "guild_1_value")

            val differentGuildToken = obtainJwtToken(userId = 111111111, guildId = 999999999)

            given()
                .spec(given())
                .header("Authorization", "Bearer $differentGuildToken")
            .`when`()
                .get("$baseUrl/api/v1/events/$eventKey")
            .then()
                .statusCode(200)
                .body("size()", equalTo(0))
        }
    }

    private fun createTestEvent(key: String, value: String) {
        given()
            .spec(givenAuthenticated())
            .body("""
                {
                    "key": "$key",
                    "value": "$value",
                    "userId": 123456789
                }
            """.trimIndent())
            .post("$baseUrl/api/v1/events")
            .then()
            .statusCode(200)
    }
}
