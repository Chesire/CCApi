package com.chesire.capi.config

import com.chesire.capi.config.jwt.JwtAuthentication
import org.springframework.security.core.context.SecurityContextHolder

fun getAuthenticatedUser(): JwtAuthentication =
    SecurityContextHolder.getContext().authentication as JwtAuthentication
