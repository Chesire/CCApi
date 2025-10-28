package com.chesire.capi.event

import com.chesire.capi.config.jwt.JwtAuthentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockJwtAuthenticationSecurityContextFactory::class)
annotation class WithMockJwtAuthentication(
    val userId: Long = 123L,
    val guildId: Long = 1000L
)

class WithMockJwtAuthenticationSecurityContextFactory :
    WithSecurityContextFactory<WithMockJwtAuthentication> {
    override fun createSecurityContext(annotation: WithMockJwtAuthentication): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val authentication = JwtAuthentication(
            userId = annotation.userId,
            guildId = annotation.guildId
        )
        context.authentication = authentication
        return context
    }
}
