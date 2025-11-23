package com.chesire.capi.integration

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance

/**
 * Base class for all integration tests.
 *
 * Provides:
 * - REST Assured configuration
 * - Server URL configuration from environment
 * - Common utilities for authentication and requests
 *
 * Configure via environment variables:
 * - TEST_SERVER_URL: Base URL of server (default: http://localhost:8080)
 * - TEST_API_KEY: API key for authentication endpoint (default: dev-default-api-key-extended)
 */
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IntegrationTestBase {

    protected lateinit var baseUrl: String
    protected lateinit var apiKey: String
    protected var jwtToken: String? = null

    companion object {
        private const val DEFAULT_SERVER_URL = "http://localhost:8080"
        private const val DEFAULT_API_KEY = "dev-default-api-key-extended"
    }

    @BeforeAll
    fun setupRestAssured() {
        baseUrl = System.getProperty("test.server.url", DEFAULT_SERVER_URL)
        apiKey = System.getProperty("test.api.key", DEFAULT_API_KEY)

        RestAssured.baseURI = baseUrl
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

        println("=".repeat(70))
        println("Integration Tests Configuration")
        println("=".repeat(70))
        println("Server URL: $baseUrl")
        println("API Key: ${apiKey.take(10)}...")
        println("=".repeat(70))
    }

    /**
     * Creates a base request specification with common headers and content type.
     */
    protected fun given(): RequestSpecification {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
    }

    /**
     * Creates a request specification with JWT authentication header.
     * Automatically fetches JWT token if not already present.
     */
    protected fun givenAuthenticated(): RequestSpecification {
        if (jwtToken == null) {
            jwtToken = obtainJwtToken()
        }

        return RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .header("Authorization", "Bearer $jwtToken")
    }

    /**
     * Obtains a JWT token from the auth endpoint.
     */
    protected fun obtainJwtToken(userId: String = "123456789", guildId: String = "987654321"): String {
        val response = RestAssured.given()
            .contentType(ContentType.JSON)
            .header("X-Api-Key", apiKey)
            .body("""{"userId": "$userId", "guildId": "$guildId"}""")
            .post("$baseUrl/api/v1/auth/token")
            .then()
            .statusCode(200)
            .extract()
            .path<String>("token")

        println("✅ JWT Token obtained successfully")
        return response
    }

    /**
     * Clears the cached JWT token, forcing a new one to be obtained on next authenticated request.
     */
    protected fun clearJwtToken() {
        jwtToken = null
    }
}
