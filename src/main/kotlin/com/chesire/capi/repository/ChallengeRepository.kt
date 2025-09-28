package com.chesire.capi.repository

import com.chesire.capi.entity.ChallengeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChallengeRepository : JpaRepository<ChallengeEntity, Long> {
    fun findByUserId(userId: Long): List<ChallengeEntity>
    fun save(challenge: ChallengeEntity): ChallengeEntity
}
