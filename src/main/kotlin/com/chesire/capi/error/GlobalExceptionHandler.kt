package com.chesire.capi.error

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn(
            "Validation failed for request: {} validation errors found",
            ex.bindingResult.fieldErrors.size,
        )

        val validationErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            ValidationError(
                field = fieldError.field,
                rejectedValue = sanitizeRejectedValue(fieldError.field, fieldError.rejectedValue),
                message = fieldError.defaultMessage ?: "Invalid value",
            )
        }
        val errorResponse = ErrorResponse(
            message = "Validation failed",
            details = "One or more fields have invalid values",
            errors = validationErrors,
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.warn("Malformed JSON request: {}", ex.message)

        val errorResponse = ErrorResponse(
            message = "Malformed JSON request",
            details = "Please check your request body format and ensure all required fields are provided",
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponse> {
        logger.warn(
            "Parameter type mismatch: parameter '{}' expected type '{}' but received '{}'",
            ex.name,
            ex.requiredType?.simpleName,
            ex.value,
        )

        val errorResponse = ErrorResponse(
            message = "Invalid parameter type",
            details = "The parameter '${ex.name}' should be of type '${ex.requiredType?.simpleName}'",
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingRequestHeaderException(ex: MissingRequestHeaderException): ResponseEntity<ErrorResponse> {
        return when {
            ex.headerName.equals("X-API-Key", ignoreCase = true) -> {
                logger.warn("Authentication failed - missing API key")
                ResponseEntity(
                    ErrorResponse(
                        message = "Authentication failed",
                        details = "Invalid credentials provided"
                    ),
                    HttpStatus.UNAUTHORIZED
                )
            }
            else -> {
                logger.warn("Missing required header: '{}'", ex.headerName)
                ResponseEntity(
                    ErrorResponse(
                        message = "Missing required header",
                        details = "The request header '${ex.headerName}' is required"
                    ),
                    HttpStatus.BAD_REQUEST
                )
            }
        }
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        logger.warn(
            "Constraint violations found: {} violations",
            ex.constraintViolations.size,
        )

        val validationErrors = ex.constraintViolations.map { violation ->
            ValidationError(
                field = violation.propertyPath.toString(),
                rejectedValue = sanitizeRejectedValue(violation.propertyPath.toString(), violation.invalidValue),
                message = violation.message,
            )
        }

        val errorResponse = ErrorResponse(
            message = "Invalid request parameters",
            details = "One or more path parameters or request parameters are invalid",
            errors = validationErrors,
        )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(JwtConfigurationException::class)
    fun handleJwtConfigurationException(ex: JwtConfigurationException): ResponseEntity<ErrorResponse> {
        logger.error("JWT configuration error: {}", ex.message, ex)

        val errorResponse = ErrorResponse(
            message = "Authentication service temporarily unavailable",
            details = "Please try again later or contact support if the problem persists",
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(JwtGenerationException::class)
    fun handleJwtGenerationException(ex: JwtGenerationException): ResponseEntity<ErrorResponse> {
        logger.warn("JWT generation failed: {}", ex.message, ex)

        val errorResponse = ErrorResponse(
            message = "Token generation failed",
            details = "Invalid request parameters for authentication",
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(TokenRateLimitException::class)
    fun handleTokenRateLimitException(ex: TokenRateLimitException): ResponseEntity<ErrorResponse> {
        logger.warn("Token rate limit exceeded: {}", ex.message)

        val errorResponse = ErrorResponse(
            message = "Too many requests",
            details = "Rate limit exceeded. Please try again later.",
        )
        return ResponseEntity(errorResponse, HttpStatus.TOO_MANY_REQUESTS)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        // Handle 401 Unauthorized specially to avoid info disclosure
        return when (ex.statusCode) {
            HttpStatus.UNAUTHORIZED -> {
                logger.warn("Authentication failed: {}", ex.reason)
                ResponseEntity(
                    ErrorResponse(
                        message = "Authentication failed",
                        details = "Invalid credentials provided"
                    ),
                    HttpStatus.UNAUTHORIZED
                )
            }

            else -> {
                logger.warn("HTTP error: {} - {}", ex.statusCode, ex.reason)
                ResponseEntity(
                    ErrorResponse(
                        message = ex.reason ?: "Request failed",
                        details = "Please check your request and try again"
                    ),
                    ex.statusCode
                )
            }
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error(
            "Unexpected exception occurred: {} - {}",
            ex.javaClass.simpleName,
            ex.message,
            ex,
        )

        val errorResponse = ErrorResponse(
            message = "An unexpected error occurred",
            details = "Please try again later or contact support if the problem persists",
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun sanitizeRejectedValue(field: String, value: Any?): Any? {
        return when {
            field.contains("password", ignoreCase = true) -> "[REDACTED]"
            field.contains("secret", ignoreCase = true) -> "[REDACTED]"
            field.contains("key", ignoreCase = true) -> "[REDACTED]"
            field.contains("token", ignoreCase = true) -> "[REDACTED]"
            value is String && value.length > 50 -> "${value.take(10)}...[TRUNCATED]"
            else -> value
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}
