package com.chesire.capi.service

import com.chesire.capi.dto.PostChallengeDto
import com.chesire.capi.dto.RetrieveChallengeDto
import com.chesire.capi.models.TimeFrame
import com.chesire.capi.repository.ChallengeRepository
import org.springframework.stereotype.Service

@Service
class ChallengeService(
    private val repository: ChallengeRepository
) {

    fun getChallenges(userId: Long): GetChallengesResult {
        // TODO: Perform the get
        return GetChallengesResult.Success(
            listOf(
                RetrieveChallengeDto(
                    id = 1,
                    name = "Test Challenge 1",
                    description = "This is a test challenge",
                    timeFrame = TimeFrame.DAILY,
                    allowPauses = true,
                    cheats = 3,
                ),
                RetrieveChallengeDto(
                    id = 2,
                    name = "Test Challenge 2",
                    description = "This is another test challenge",
                    timeFrame = TimeFrame.WEEKLY,
                    allowPauses = false,
                    cheats = 1,
                ),
            )
        )
    }

    fun getChallenge(challengeId: Long): GetChallengeResult {
        // TODO: Perform the get
        return GetChallengeResult.Success(
            RetrieveChallengeDto(
                id = challengeId,
                name = "Test Challenge",
                description = "This is a test challenge",
                timeFrame = TimeFrame.DAILY,
                allowPauses = true,
                cheats = 3,
            )
        )
    }

    fun addChallenge(data: PostChallengeDto): PostChallengeResult {
        // TODO: Perform add
        return PostChallengeResult.Success(
            data.toRetrieveChallengeDto(1)
        )
    }

    fun deleteChallenge(challengeId: Long): DeleteChallengeResult {
        // TODO: Perform delete
        return DeleteChallengeResult.Success
    }

    private fun PostChallengeDto.toRetrieveChallengeDto(id: Long) = RetrieveChallengeDto(
        id = id,
        name = name,
        description = description,
        timeFrame = timeFrame,
        allowPauses = allowPauses,
        cheats = cheats,
    )
}

sealed interface GetChallengesResult {
    data class Success(val challenges: List<RetrieveChallengeDto>) : GetChallengesResult
    object UserNotFound : GetChallengesResult
    object UnknownError : GetChallengesResult
}

sealed interface GetChallengeResult {
    data class Success(val challenge: RetrieveChallengeDto) : GetChallengeResult
    object ChallengeNotFound : GetChallengeResult
    object UserNotFound : GetChallengeResult
    object UnknownError : GetChallengeResult
}

sealed interface PostChallengeResult {
    data class Success(val challenge: RetrieveChallengeDto) : PostChallengeResult
    object InvalidData : PostChallengeResult
    object UserNotFound : PostChallengeResult
    object UnknownError : PostChallengeResult
}

sealed interface DeleteChallengeResult {
    object Success : DeleteChallengeResult
    object ChallengeNotFound : DeleteChallengeResult
    object UserNotFound : DeleteChallengeResult
    object UnknownError : DeleteChallengeResult
}
