package com.chesire.capi.integration.auth

import com.chesire.capi.integration.IntegrationTestBase
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Auth API Integration Tests")
class AuthIntegrationTest : IntegrationTestBase() {

    @Test
    @DisplayName("Should successfully generate JWT token with valid API key")
    fun shouldGenerateJwtToken() {
        given()
            .contentType(ContentType.JSON)
            .header("X-Api-Key", apiKey)
            .body("""{"userId": 123456789, "guildId": 987654321}""")
        .`when`()
            .post("$baseUrl/api/v1/auth/token")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("tokenType", equalTo("Bearer"))
            .body("expiresIn", equalTo(86400))
    }

    @Test
    @DisplayName("Should return 401 when API key is missing")
    fun shouldRejectMissingApiKey() {
        given()
            .contentType(ContentType.JSON)
            .body("""{"userId": 123456789, "guildId": 987654321}""")
        .`when`()
            .post("$baseUrl/api/v1/auth/token")
        .then()
            .statusCode(401)
            .body("message", equalTo("Authentication failed"))
            .body("details", equalTo("Invalid credentials provided"))
    }

    @Test
    @DisplayName("Should return 401 when API key is invalid")
    fun shouldRejectInvalidApiKey() {
        given()
            .contentType(ContentType.JSON)
            .header("X-Api-Key", "invalid-api-key-12345678")
            .body("""{"userId": 123456789, "guildId": 987654321}""")
        .`when`()
            .post("$baseUrl/api/v1/auth/token")
        .then()
            .statusCode(401)
            .body("message", equalTo("Authentication failed"))
            .body("details", equalTo("Invalid credentials provided"))
    }

    @Test
    @DisplayName("Should return 400 when request body is malformed")
    fun shouldRejectMalformedRequest() {
        given()
            .contentType(ContentType.JSON)
            .header("X-Api-Key", apiKey)
            .body("""{"invalid": "data"}""")
        .`when`()
            .post("$baseUrl/api/v1/auth/token")
        .then()
            .statusCode(400)
    }

    @Test
    @DisplayName("Should generate valid JWT token format")
    fun shouldGenerateValidJwtFormat() {
        given()
            .contentType(ContentType.JSON)
            .header("X-Api-Key", apiKey)
            .body("""{"userId": 999888777, "guildId": 111222333}""")
        .`when`()
            .post("$baseUrl/api/v1/auth/token")
        .then()
            .statusCode(200)
            .body("token", matchesPattern("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$"))
    }
}
