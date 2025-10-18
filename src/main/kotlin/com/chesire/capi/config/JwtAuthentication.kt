package com.chesire.capi.config

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

class JwtAuthentication(
    val userId: Long,
    val guildId: Long
) : UsernamePasswordAuthenticationToken(userId, null, emptyList()) {
    override fun getPrincipal() = userId
}
