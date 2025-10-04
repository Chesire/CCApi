package com.chesire.capi.challenge

import com.chesire.capi.challenge.dto.ChallengeDto
import com.chesire.capi.challenge.dto.PostChallengeDto
import com.chesire.capi.challenge.service.ChallengeService
import com.chesire.capi.challenge.service.DeleteChallengeResult
import com.chesire.capi.challenge.service.GetChallengeResult
import com.chesire.capi.challenge.service.GetChallengesResult
import com.chesire.capi.challenge.service.PostChallengeResult
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.ResponseEntity
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
class ChallengeController(private val challengeService: ChallengeService) {

    @GetMapping("/user/{userId}")
    fun getChallengesByUser(
        @PathVariable @Positive(message = "User ID must be positive") userId: Long
    ): ResponseEntity<List<ChallengeDto>> {
        return when (val result = challengeService.getChallenges(userId)) {
            is GetChallengesResult.Success -> ResponseEntity.ok(result.challenges)
            GetChallengesResult.NotFound -> ResponseEntity.noContent().build()
            GetChallengesResult.UnknownError -> ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{challengeId}")
    fun getChallengeById(
        @PathVariable @Positive(message = "Challenge ID must be positive") challengeId: Long
    ): ResponseEntity<ChallengeDto> {
        return when (val result = challengeService.getChallenge(challengeId)) {
            is GetChallengeResult.Success -> ResponseEntity.ok(result.challenge)
            GetChallengeResult.NotFound -> ResponseEntity.noContent().build()
            GetChallengeResult.UnknownError -> ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping
    fun createChallenge(@Valid @RequestBody data: PostChallengeDto): ResponseEntity<ChallengeDto> {
        // TODO: Need to pass the users id to validate they can add this challenge
        return when (val result = challengeService.addChallenge(data, 0L)) {
            is PostChallengeResult.Success -> ResponseEntity.ok(result.challenge)
            PostChallengeResult.InvalidData -> ResponseEntity.badRequest().build()
            PostChallengeResult.NotFound -> ResponseEntity.noContent().build()
            PostChallengeResult.UnknownError -> ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{challengeId}")
    fun deleteChallenge(
        @PathVariable @Positive(message = "Challenge ID must be positive") challengeId: Long
    ): ResponseEntity<Void> {
        // TODO: Need to pass the users token to validate they can delete this challenge
        val result = challengeService.deleteChallenge(challengeId)
        return when (result) {
            DeleteChallengeResult.Success -> ResponseEntity.noContent().build()
            DeleteChallengeResult.NotFound -> ResponseEntity.noContent().build()
            DeleteChallengeResult.UnknownError -> ResponseEntity.internalServerError().build()
        }
    }
}
