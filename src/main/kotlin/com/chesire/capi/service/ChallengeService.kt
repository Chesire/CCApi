package com.chesire.capi.service

import com.chesire.capi.dto.ChallengeDto
import com.chesire.capi.dto.PostChallengeDto
import com.chesire.capi.entity.ChallengeEntity
import com.chesire.capi.repository.ChallengeRepository
import org.springframework.stereotype.Service

@Service
class ChallengeService(private val repository: ChallengeRepository) {

    fun getChallenges(userId: Long): GetChallengesResult {
        val allForUser = repository.findByUserId(userId)
        return try {
            if (allForUser.isEmpty()) {
                GetChallengesResult.NotFound
            } else {
                GetChallengesResult.Success(
                    allForUser.map {
                        ChallengeDto(
                            id = it.id,
                            name = it.name,
                            description = it.description,
                            timeFrame = it.timeFrame,
                            allowPauses = it.allowPauses,
                            cheats = it.cheats,
                        )
                    }
                )
            }
        } catch (ex: Exception) {
            GetChallengesResult.UnknownError
        }
    }

    fun getChallenge(challengeId: Long): GetChallengeResult {
        return try {
            val challenge = repository.findById(challengeId).orElse(null)
            if (challenge == null) {
                GetChallengeResult.NotFound
            } else {
                GetChallengeResult.Success(challenge.toRetrieveChallengeDto())
            }
        } catch (ex: Exception) {
            GetChallengeResult.UnknownError
        }
    }

    fun addChallenge(data: PostChallengeDto, userId: Long): PostChallengeResult {
        // Do some validation on the data?
        return try {
            val result = repository.save(data.toEntity(userId))
            PostChallengeResult.Success(result.toRetrieveChallengeDto())
        } catch (ex: Exception) {
            PostChallengeResult.UnknownError
        }
    }

    fun deleteChallenge(challengeId: Long): DeleteChallengeResult {
        return if (repository.existsById(challengeId)) {
            try {
                repository.deleteById(challengeId)
                DeleteChallengeResult.Success
            } catch (ex: Exception) {
                DeleteChallengeResult.UnknownError
            }
        } else {
            DeleteChallengeResult.NotFound
        }
    }

    private fun PostChallengeDto.toEntity(userId: Long) = ChallengeEntity(
        userId = userId,
        name = name,
        description = description,
        timeFrame = timeFrame,
        allowPauses = allowPauses,
        cheats = cheats,
    )

    private fun ChallengeEntity.toRetrieveChallengeDto() = ChallengeDto(
        id = id,
        name = name,
        description = description,
        timeFrame = timeFrame,
        allowPauses = allowPauses,
        cheats = cheats,
    )
}

sealed interface GetChallengesResult {
    data class Success(val challenges: List<ChallengeDto>) : GetChallengesResult
    object NotFound : GetChallengesResult
    object UnknownError : GetChallengesResult
}

sealed interface GetChallengeResult {
    data class Success(val challenge: ChallengeDto) : GetChallengeResult
    object NotFound : GetChallengeResult
    object UnknownError : GetChallengeResult
}

sealed interface PostChallengeResult {
    data class Success(val challenge: ChallengeDto) : PostChallengeResult
    object InvalidData : PostChallengeResult
    object NotFound : PostChallengeResult
    object UnknownError : PostChallengeResult
}

sealed interface DeleteChallengeResult {
    object Success : DeleteChallengeResult
    object NotFound : DeleteChallengeResult
    object UnknownError : DeleteChallengeResult
}

//
