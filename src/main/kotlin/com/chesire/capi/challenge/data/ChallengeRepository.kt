package com.chesire.capi.challenge.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChallengeRepository : JpaRepository<ChallengeEntity, Long> {
    fun findByUserIdAndGuildId(userId: Long, guildId: Long): List<ChallengeEntity>
}
