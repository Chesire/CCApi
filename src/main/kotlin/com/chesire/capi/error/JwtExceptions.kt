package com.chesire.capi.error

/**
 * Base class for all JWT-related exceptions
 */
abstract class JwtException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * Thrown when JWT system configuration is invalid (e.g., bad secret key)
 * Results in HTTP 500 - Internal Server Error
 */
class JwtConfigurationException(
    message: String,
    cause: Throwable? = null,
) : JwtException(message, cause)

/**
 * Thrown when JWT token generation fails due to invalid parameters
 * Results in HTTP 400 - Bad Request
 */
class JwtGenerationException(
    message: String,
    cause: Throwable? = null,
) : JwtException(message, cause)

/**
 * Thrown when rate limit is exceeded for token generation
 * Results in HTTP 429 - Too Many Requests
 */
class TokenRateLimitException(
    message: String,
) : JwtException(message)
