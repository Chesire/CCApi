package com.chesire.capi.config.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7) // Remove "Bearer " prefix
        val userId = jwtService.extractUserId(jwt)
        val guildId = jwtService.extractGuildId(jwt)

        if (
            userId != null &&
            guildId != null &&
            SecurityContextHolder.getContext().authentication == null
        ) {
            MDC.put("userId", userId.toString())
            MDC.put("guildId", guildId.toString())

            if (jwtService.isTokenValid(jwt)) {
                val authToken = JwtAuthentication(
                    userId = userId,
                    guildId = guildId
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }
}
