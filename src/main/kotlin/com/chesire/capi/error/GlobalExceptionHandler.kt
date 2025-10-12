package com.chesire.capi.error

import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn(
            "Validation failed for request: {} validation errors found",
            ex.bindingResult.fieldErrors.size,
        )

        val validationErrors =
            ex.bindingResult.fieldErrors.map { fieldError ->
                ValidationError(
                    field = fieldError.field,
                    rejectedValue = fieldError.rejectedValue,
                    message = fieldError.defaultMessage ?: "Invalid value",
                )
            }
        val errorResponse =
            ErrorResponse(
                message = "Validation failed",
                details = "One or more fields have invalid values",
                errors = validationErrors,
            )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        logger.warn("Malformed JSON request: {}", ex.message)

        val errorResponse =
            ErrorResponse(
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

        val errorResponse =
            ErrorResponse(
                message = "Invalid parameter type",
                details = "The parameter '${ex.name}' should be of type '${ex.requiredType?.simpleName}'",
            )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        logger.warn(
            "Constraint violations found: {} violations",
            ex.constraintViolations.size,
        )

        val validationErrors =
            ex.constraintViolations.map { violation ->
                ValidationError(
                    field = violation.propertyPath.toString(),
                    rejectedValue = violation.invalidValue,
                    message = violation.message,
                )
            }

        val errorResponse =
            ErrorResponse(
                message = "Invalid request parameters",
                details = "One or more path parameters or request parameters are invalid",
                errors = validationErrors,
            )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error(
            "Unexpected exception occurred: {} - {}",
            ex.javaClass.simpleName,
            ex.message,
            ex,
        )

        val errorResponse =
            ErrorResponse(
                message = "An unexpected error occurred",
                details = "Please try again later or contact support if the problem persists",
            )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}
