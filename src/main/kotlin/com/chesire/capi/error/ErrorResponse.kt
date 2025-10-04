package com.chesire.capi.error

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

class ErrorResponse(
    val message: String,
    val details: String? = null,
    val errors: List<FieldError>? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class FieldError(
    val field: String,
    val rejectedValue: Any?,
    val message: String
)
