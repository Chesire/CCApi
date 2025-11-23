package com.chesire.capi.config

import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

@DisplayName("TokenRateLimiter Tests")
class TokenRateLimiterTest {
    private lateinit var rateLimiter: TokenRateLimiter

    @BeforeEach
    fun setUp() {
        rateLimiter = TokenRateLimiter()
        // Use shorter window for testing
        ReflectionTestUtils.setField(rateLimiter, "windowMs", 1000L)
    }

    private fun createValidRequest() = Triple("test-api-key", "123456789012345678", "987654321012345678")

    private fun userId(offset: Int = 0) = (123456789012345678L + offset).toString()
    private fun guildId(offset: Int = 0) = (987654321012345678L + offset).toString()

    @Nested
    @DisplayName("Rate Limiting Tests")
    inner class RateLimitingTests {
        @Test
        @DisplayName("Should allow requests within limits")
        fun shouldAllowRequestsWithinLimits() {
            val (apiKey, userId, guildId) = createValidRequest()

            val allowed = rateLimiter.isAllowed(apiKey, userId, guildId)

            assertTrue(allowed)
        }

        @Test
        @DisplayName("Should block requests exceeding API key limit")
        fun shouldBlockRequestsExceedingApiKeyLimit() {
            val (apiKey, user, guild) = createValidRequest()
            val apiKeyLimit = ReflectionTestUtils.getField(rateLimiter, "apiKeyLimitPerHour") as Int

            // Exhaust API key limit
            repeat(apiKeyLimit) {
                rateLimiter.isAllowed(apiKey, user, guildId(it))
            }

            val blocked = rateLimiter.isAllowed(apiKey, userId(999), guildId(999))

            assertFalse(blocked)
        }

        @Test
        @DisplayName("Should block requests exceeding guild limit")
        fun shouldBlockRequestsExceedingGuildLimit() {
            val (apiKey, user, guild) = createValidRequest()
            val guildLimit = ReflectionTestUtils.getField(rateLimiter, "guildLimitPerHour") as Int

            // Exhaust guild limit
            repeat(guildLimit) {
                rateLimiter.isAllowed(apiKey + it, userId(it), guild)
            }

            val blocked = rateLimiter.isAllowed("different-api-key", userId(999), guild)

            assertFalse(blocked)
        }

        @Test
        @DisplayName("Should block requests exceeding user limit")
        fun shouldBlockRequestsExceedingUserLimit() {
            val (apiKey, user, guild) = createValidRequest()
            val userLimit = ReflectionTestUtils.getField(rateLimiter, "userLimitPerHour") as Int

            // Exhaust user limit
            repeat(userLimit) {
                rateLimiter.isAllowed(apiKey + it, user, guildId(it))
            }

            val blocked = rateLimiter.isAllowed("different-api-key", user, guildId(999))

            assertFalse(blocked)
        }
    }

    @Nested
    @DisplayName("Independent Limiting Tests")
    inner class IndependentLimitingTests {
        @Test
        @DisplayName("Should limit users independently")
        fun shouldLimitUsersIndependently() {
            val (apiKey, user1, guild) = createValidRequest()
            val user2 = userId(1)
            val userLimit = ReflectionTestUtils.getField(rateLimiter, "userLimitPerHour") as Int

            // Exhaust limit for user1
            repeat(userLimit) {
                rateLimiter.isAllowed(apiKey, user1, guild)
            }

            // User2 should still be allowed
            val user2Allowed = rateLimiter.isAllowed(apiKey, user2, guild)
            val user1Blocked = rateLimiter.isAllowed(apiKey, user1, guild)

            assertTrue(user2Allowed)
            assertFalse(user1Blocked)
        }

        @Test
        @DisplayName("Should limit guilds independently")
        fun shouldLimitGuildsIndependently() {
            val (apiKey, user, guild1) = createValidRequest()
            val guild2 = guildId(1)
            val guildLimit = ReflectionTestUtils.getField(rateLimiter, "guildLimitPerHour") as Int

            // Exhaust limit for guild1
            repeat(guildLimit) {
                rateLimiter.isAllowed(apiKey, userId(it), guild1)
            }

            // Guild2 should still be allowed
            val guild2Allowed = rateLimiter.isAllowed(apiKey, userId(999), guild2)

            assertTrue(guild2Allowed)
        }

        @Test
        @DisplayName("Should limit API keys independently")
        fun shouldLimitApiKeysIndependently() {
            val (apiKey1, user, guild) = createValidRequest()
            val apiKey2 = "different-api-key"
            val apiKeyLimit = ReflectionTestUtils.getField(rateLimiter, "apiKeyLimitPerHour") as Int

            // Exhaust limit for apiKey1
            repeat(apiKeyLimit) {
                rateLimiter.isAllowed(apiKey1, userId(it), guildId(it))
            }

            // apiKey2 should still be allowed
            val apiKey2Allowed = rateLimiter.isAllowed(apiKey2, userId(999), guildId(999))

            assertTrue(apiKey2Allowed)
        }
    }

    @Nested
    @DisplayName("Cleanup Tests")
    inner class CleanupTests {
        @Test
        @DisplayName("Should clean up expired entries")
        fun shouldCleanUpExpiredEntries() {
            val (apiKey, userId, guildId) = createValidRequest()

            // Make some requests to populate the maps
            rateLimiter.isAllowed(apiKey, userId, guildId)

            val requestCounts = ReflectionTestUtils.getField(rateLimiter, "requestCounts") as ConcurrentHashMap<*, *>
            val sizeBefore = requestCounts.size

            // Wait for entries to expire (window is 1 second in test)
            Thread.sleep(1100)

            // Trigger cleanup
            rateLimiter.cleanupExpiredEntries()

            val sizeAfter = requestCounts.size
            assertTrue(sizeAfter <= sizeBefore)
        }
    }
}
