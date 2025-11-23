package com.chesire.capi.config.jwt

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class JwtAuthentication(
    val userId: String,
    val guildId: String
) : UsernamePasswordAuthenticationToken(userId, null, emptyList()) {
    override fun getPrincipal() = userId
}
