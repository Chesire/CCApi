package com.chesire.capi.config

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class TokenRateLimiter {
    private val requestCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val lastResetTime = ConcurrentHashMap<String, Long>()

    private val maxRequests = 10
    private val windowMs = 60_000L

    fun isAllowed(clientId: String): Boolean {
        val now = System.currentTimeMillis()
        val count = requestCounts.computeIfAbsent(clientId) { AtomicInteger(0) }
        val lastReset = lastResetTime.computeIfAbsent(clientId) { now }

        if (now - lastReset > windowMs) {
            count.set(0)
            lastResetTime[clientId] = now
        }

        val newCount = count.incrementAndGet()
        val allowed = newCount <= maxRequests

        if (!allowed) {
            logger.warn("Rate limit exceeded for client, count: {}", newCount)
        }

        return allowed
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenRateLimiter::class.java)
    }
}
