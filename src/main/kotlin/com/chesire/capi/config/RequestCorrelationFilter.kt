package com.chesire.capi.config

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RequestCorrelationFilter : Filter {
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse
        val correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER) ?: UUID.randomUUID().toString()

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId)
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId)

        val startTime = System.currentTimeMillis()

        logger.info(
            "HTTP Request started: method={}, uri={}, correlationId={}",
            httpRequest.method,
            httpRequest.requestURI,
            correlationId,
        )

        try {
            chain.doFilter(request, response)
        } finally {
            val duration = System.currentTimeMillis() - startTime

            logger.info(
                "HTTP Request completed: method={}, uri={}, status={}, durationMs={}, correlationId={}",
                httpRequest.method,
                httpRequest.requestURI,
                httpResponse.status,
                duration,
                correlationId,
            )
            MDC.clear()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RequestCorrelationFilter::class.java)
        private const val CORRELATION_ID_HEADER = "X-Correlation-ID"
        private const val CORRELATION_ID_MDC_KEY = "correlationId"
    }
}
