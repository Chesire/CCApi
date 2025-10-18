package com.chesire.capi.challenge.service

import com.chesire.capi.challenge.data.ChallengeEntity
import com.chesire.capi.challenge.data.ChallengeRepository
import com.chesire.capi.challenge.dto.ChallengeDto
import com.chesire.capi.challenge.dto.PostChallengeDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChallengeService(
    private val repository: ChallengeRepository,
) {
    fun getChallenges(userId: Long): GetChallengesResult {
        logger.debug("Starting getChallenges for userId={}", userId)
        val startTime = System.currentTimeMillis()

        return try {
            val allForUser = repository.findByUserId(userId)
            val queryTime = System.currentTimeMillis() - startTime
            logger.debug(
                "Database query completed in {}ms for userId={}, found {} challenges",
                queryTime,
                userId,
                allForUser.size,
            )

            if (allForUser.isEmpty()) {
                logger.info("No challenges exist for userId={}", userId)
                GetChallengesResult.NotFound
            } else {
                val challenges = allForUser.map { it.toChallengeDto() }
                val totalTime = System.currentTimeMillis() - startTime
                logger.info(
                    "Successfully retrieved and mapped {} challenges for userId={} in {}ms",
                    challenges.size,
                    userId,
                    totalTime,
                )

                GetChallengesResult.Success(challenges)
            }
        } catch (ex: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            logger.error(
                "Error retrieving challenges for userId={} after {}ms: {} - {}",
                userId,
                totalTime,
                ex.javaClass.simpleName,
                ex.message,
                ex,
            )

            GetChallengesResult.UnknownError
        }
    }

    fun getChallenge(challengeId: Long): GetChallengeResult {
        logger.debug("Starting getChallenge for challengeId={}", challengeId)
        val startTime = System.currentTimeMillis()

        return try {
            val challenge = repository.findById(challengeId).orElse(null)
            val queryTime = System.currentTimeMillis() - startTime

            if (challenge == null) {
                logger.info("Challenge not found in database: challengeId={} (query took {}ms)", challengeId, queryTime)
                GetChallengeResult.NotFound
            } else {
                logger.debug(
                    "Found challenge in database: challengeId={}, name='{}', userId={} (query took {}ms)",
                    challengeId,
                    challenge.name,
                    challenge.userId,
                    queryTime,
                )
                val dto = challenge.toChallengeDto()
                val totalTime = System.currentTimeMillis() - startTime
                logger.info(
                    "Successfully retrieved and mapped challenge: challengeId={} in {}ms",
                    challengeId,
                    totalTime,
                )
                GetChallengeResult.Success(dto)
            }
        } catch (ex: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            logger.error(
                "Error retrieving challenge challengeId={} after {}ms: {} - {}",
                challengeId,
                totalTime,
                ex.javaClass.simpleName,
                ex.message,
                ex,
            )
            GetChallengeResult.UnknownError
        }
    }

    fun addChallenge(
        data: PostChallengeDto,
        userId: Long,
        guildId: Long
    ): PostChallengeResult {
        logger.info(
            "Starting challenge creation: name='{}', timeFrame={}, userId={}, guildId={}",
            data.name,
            data.timeFrame,
            userId,
            guildId
        )
        val startTime = System.currentTimeMillis()

        return try {
            val entity = data.toEntity(userId, guildId)
            logger.debug(
                "Created challenge entity: name='{}', description='{}', allowPauses={}, cheats={}",
                entity.name,
                entity.description,
                entity.allowPauses,
                entity.cheats,
            )

            val saveStartTime = System.currentTimeMillis()
            val result = repository.save(entity)
            val saveTime = System.currentTimeMillis() - saveStartTime
            logger.debug("Database save completed in {}ms: challengeId={}", saveTime, result.id)

            val dto = result.toChallengeDto()
            val totalTime = System.currentTimeMillis() - startTime
            logger.info(
                "Successfully created challenge: challengeId={}, name='{}' in {}ms",
                result.id,
                result.name,
                totalTime,
            )

            PostChallengeResult.Success(dto)
        } catch (ex: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            logger.error(
                "Error creating challenge '{}' for userId={} guildId={} after {}ms: {} - {}",
                data.name,
                userId,
                guildId,
                totalTime,
                ex.javaClass.simpleName,
                ex.message,
                ex,
            )
            PostChallengeResult.UnknownError
        }
    }

    fun deleteChallenge(challengeId: Long, userId: Long, guildId: Long): DeleteChallengeResult {
        logger.info("Starting challenge deletion: challengeId={} userId={} guildId={}", challengeId, userId, guildId)
        val startTime = System.currentTimeMillis()

        return try {
            val currentChallenge = retrieveChallenge(challengeId)
            if (currentChallenge == null) {
                val totalTime = System.currentTimeMillis() - startTime
                logger.info(
                    "Challenge not found for deletion: challengeId={} (total time {}ms)",
                    challengeId,
                    totalTime,
                )
                DeleteChallengeResult.NotFound
            } else if (currentChallenge.userId != userId || currentChallenge.guildId != guildId) {
                val totalTime = System.currentTimeMillis() - startTime
                logger.warn(
                    "Unauthorized deletion attempt: challengeId={} belongs to userId={} guildId={}, attempted by userId={} guildId={} (total time {}ms)",
                    challengeId,
                    currentChallenge.userId,
                    currentChallenge.guildId,
                    userId,
                    guildId,
                    totalTime
                )
                DeleteChallengeResult.Unauthorized
            } else {
                val deleteStartTime = System.currentTimeMillis()
                repository.deleteById(challengeId)
                val deleteTime = System.currentTimeMillis() - deleteStartTime
                logger.debug("Database delete completed in {}ms: challengeId={}", deleteTime, challengeId)

                val totalTime = System.currentTimeMillis() - startTime
                logger.info("Successfully deleted challenge: challengeId={} in {}ms", challengeId, totalTime)
                DeleteChallengeResult.Success
            }
        } catch (ex: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            logger.error(
                "Error deleting challenge challengeId={} after {}ms: {} - {}",
                challengeId,
                totalTime,
                ex.javaClass.simpleName,
                ex.message,
                ex,
            )
            DeleteChallengeResult.UnknownError
        }
    }

    private fun retrieveChallenge(challengeId: Long): ChallengeEntity? {
        val existsStartTime = System.currentTimeMillis()
        val entity = repository.findById(challengeId).orElse(null)
        val existsTime = System.currentTimeMillis() - existsStartTime

        logger.debug(
            "Existence check completed in {}ms: challengeId={}, exists={}",
            existsTime,
            challengeId,
            entity != null,
        )

        return entity
    }

    private fun PostChallengeDto.toEntity(userId: Long, guildId: Long) =
        ChallengeEntity(
            userId = userId,
            guildId = guildId,
            name = name,
            description = description,
            timeFrame = timeFrame,
            allowPauses = allowPauses,
            cheats = cheats,
        )

    private fun ChallengeEntity.toChallengeDto() =
        ChallengeDto(
            id = id,
            name = name,
            description = description,
            timeFrame = timeFrame,
            allowPauses = allowPauses,
            cheats = cheats,
        )

    companion object {
        private val logger = LoggerFactory.getLogger(ChallengeService::class.java)
    }
}

sealed interface GetChallengesResult {
    data class Success(
        val challenges: List<ChallengeDto>,
    ) : GetChallengesResult

    object NotFound : GetChallengesResult

    object UnknownError : GetChallengesResult
}

sealed interface GetChallengeResult {
    data class Success(
        val challenge: ChallengeDto,
    ) : GetChallengeResult

    object NotFound : GetChallengeResult

    object UnknownError : GetChallengeResult
}

sealed interface PostChallengeResult {
    data class Success(
        val challenge: ChallengeDto,
    ) : PostChallengeResult

    object InvalidData : PostChallengeResult

    object NotFound : PostChallengeResult

    object UnknownError : PostChallengeResult
}

sealed interface DeleteChallengeResult {
    object Success : DeleteChallengeResult

    object NotFound : DeleteChallengeResult

    object Unauthorized : DeleteChallengeResult

    object UnknownError : DeleteChallengeResult
}
