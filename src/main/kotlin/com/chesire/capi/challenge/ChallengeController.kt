package com.chesire.capi.challenge

import com.chesire.capi.challenge.dto.ChallengeDto
import com.chesire.capi.challenge.dto.PostChallengeDto
import com.chesire.capi.challenge.service.ChallengeService
import com.chesire.capi.challenge.service.DeleteChallengeResult
import com.chesire.capi.challenge.service.GetChallengeResult
import com.chesire.capi.challenge.service.GetChallengesResult
import com.chesire.capi.challenge.service.PostChallengeResult
import com.chesire.capi.config.jwt.JwtAuthentication
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/challenges")
class ChallengeController(
    private val challengeService: ChallengeService,
) {
    @GetMapping("/user/{userId}")
    fun getChallengesByUser(
        @PathVariable @Positive(message = "User ID must be positive") userId: Long,
    ): ResponseEntity<List<ChallengeDto>> {
        logger.info("Fetching challenges for userId={}", userId)
        return when (val result = challengeService.getChallenges(userId)) {
            is GetChallengesResult.Success -> {
                logger.info("Successfully fetched {} challenges for userId={}", result.challenges.size, userId)
                ResponseEntity.ok(result.challenges)
            }

            GetChallengesResult.NotFound -> {
                logger.info("No challenges found for userId={}", userId)
                ResponseEntity.noContent().build()
            }

            GetChallengesResult.UnknownError -> {
                logger.error("Unknown error fetching challenges for userId={}", userId)
                ResponseEntity.internalServerError().build()
            }
        }
    }

    @GetMapping("/{challengeId}")
    fun getChallengeById(
        @PathVariable @Positive(message = "Challenge ID must be positive") challengeId: Long,
    ): ResponseEntity<ChallengeDto> {
        logger.info("Fetching challenge for challengeId={}", challengeId)
        return when (val result = challengeService.getChallenge(challengeId)) {
            is GetChallengeResult.Success -> {
                logger.info(
                    "Successfully fetched challenge for challengeId={}, name={}",
                    challengeId,
                    result.challenge.name,
                )
                ResponseEntity.ok(result.challenge)
            }

            GetChallengeResult.NotFound -> {
                logger.info("Challenge not found for challengeId={}", challengeId)
                ResponseEntity.noContent().build()
            }

            GetChallengeResult.UnknownError -> {
                logger.error("Unknown error fetching challenge for challengeId={}", challengeId)
                ResponseEntity.internalServerError().build()
            }
        }
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createChallenge(
        @Valid @RequestBody data: PostChallengeDto,
        authentication: Authentication,
    ): ResponseEntity<ChallengeDto> {
        val auth = authentication as JwtAuthentication
        val userId = auth.userId
        val guildId = auth.guildId

        logger.info("Creating challenge for userId={} guildId={}, with name={}", userId, guildId, data.name)
        return when (val result = challengeService.addChallenge(data, userId, guildId)) {
            is PostChallengeResult.Success -> {
                logger.info(
                    "Successfully created challenge with id={}, name={}",
                    result.challenge.id,
                    result.challenge.name,
                )
                ResponseEntity.ok(result.challenge)
            }

            PostChallengeResult.InvalidData -> {
                logger.warn("Invalid data provided when trying to create challenge with name={}", data.name)
                ResponseEntity.badRequest().build()
            }

            PostChallengeResult.NotFound -> {
                logger.warn("Related entity not found when trying to create challenge with name={}", data.name)
                ResponseEntity.noContent().build()
            }

            PostChallengeResult.UnknownError -> {
                logger.error("Unknown error creating challenge with name={}", data.name)
                ResponseEntity.internalServerError().build()
            }
        }
    }

    @DeleteMapping("/{challengeId}")
    fun deleteChallenge(
        @PathVariable @Positive(message = "Challenge ID must be positive") challengeId: Long,
        authentication: Authentication,
    ): ResponseEntity<Void> {
        val auth = authentication as JwtAuthentication
        val userId = auth.userId
        val guildId = auth.guildId

        logger.info("Deleting challenge challengeId={} for userId={} guildId={}", challengeId, userId, guildId)
        return when (challengeService.deleteChallenge(challengeId, userId, guildId)) {
            DeleteChallengeResult.Success -> {
                logger.info("Successfully deleted challenge with challengeId={}", challengeId)
                ResponseEntity.noContent().build()
            }

            DeleteChallengeResult.NotFound -> {
                logger.warn("Challenge not found when trying to delete challenge with challengeId={}", challengeId)
                ResponseEntity.noContent().build()
            }

            DeleteChallengeResult.Unauthorized -> {
                logger.warn("Unauthorized attempt to delete challenge with challengeId={}", challengeId)
                ResponseEntity.status(HttpStatus.FORBIDDEN).build()
            }

            DeleteChallengeResult.UnknownError -> {
                logger.error("Unknown error deleting challenge with challengeId={}", challengeId)
                ResponseEntity.internalServerError().build()
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChallengeController::class.java)
    }
}
