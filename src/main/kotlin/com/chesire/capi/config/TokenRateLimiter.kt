package com.chesire.capi.config

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class TokenRateLimiter {
    private val requestCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val lastResetTime = ConcurrentHashMap<String, Long>()

    private var guildLimitPerHour: Int = 100
    private var userLimitPerHour: Int = 20
    private var apiKeyLimitPerHour: Int = 2000
    private val windowMs = 3600000L // 1 hour

    fun isAllowed(apiKey: String, userId: Long, guildId: Long): Boolean {
        return isAllowedByApiKey(apiKey) && isAllowedByGuild(guildId) && isAllowedByUser(userId)
    }

    private fun isAllowedByApiKey(apiKey: String): Boolean {
        return checkLimit("api_${apiKey.hashCode()}", apiKeyLimitPerHour, "API key")
    }

    private fun isAllowedByGuild(guildId: Long): Boolean {
        return checkLimit("guild_$guildId", guildLimitPerHour, "guild")
    }

    private fun isAllowedByUser(userId: Long): Boolean {
        return checkLimit("user_$userId", userLimitPerHour, "user")
    }

    private fun checkLimit(key: String, limit: Int, type: String): Boolean {
        val now = System.currentTimeMillis()
        val count = requestCounts.computeIfAbsent(key) { AtomicInteger(0) }
        val lastReset = lastResetTime.computeIfAbsent(key) { now }

        if (now - lastReset > windowMs) {
            count.set(0)
            lastResetTime[key] = now
        }

        val newCount = count.incrementAndGet()
        val allowed = newCount <= limit

        if (!allowed) {
            logger.warn("Rate limit exceeded for {} - count: {}, limit: {}", type, newCount, limit)
        }

        return allowed
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    fun cleanupExpiredEntries() {
        val now = System.currentTimeMillis()
        val cutoff = now - (windowMs * 2) // Keep 2-hour buffer
        var cleaned = 0

        lastResetTime.entries.removeIf { (key, lastReset) ->
            if (now - lastReset > cutoff) {
                requestCounts.remove(key)
                cleaned++
                true
            } else {
                false
            }
        }

        if (cleaned > 0) {
            logger.info("Cleaned up {} expired rate limit entries", cleaned)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TokenRateLimiter::class.java)
    }
}
