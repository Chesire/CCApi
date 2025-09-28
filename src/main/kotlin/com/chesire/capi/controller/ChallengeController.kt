package com.chesire.capi.controller

import com.chesire.capi.dto.PostChallengeDto
import com.chesire.capi.dto.RetrieveChallengeDto
import com.chesire.capi.service.ChallengeService
import com.chesire.capi.service.DeleteChallengeResult
import com.chesire.capi.service.GetChallengeResult
import com.chesire.capi.service.GetChallengesResult
import com.chesire.capi.service.PostChallengeResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/challenge")
class ChallengeController(private val challengeService: ChallengeService) {

    @GetMapping("/{userId}")
    fun getChallenges(@PathVariable userId: Long): ResponseEntity<List<RetrieveChallengeDto>> {
        return when (val result = challengeService.getChallenges(userId)) {
            is GetChallengesResult.Success -> ResponseEntity.ok(result.challenges)
            GetChallengesResult.UserNotFound -> ResponseEntity.notFound().build()
            GetChallengesResult.UnknownError -> ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{challengeId}")
    fun getChallenge(@PathVariable challengeId: Long): ResponseEntity<RetrieveChallengeDto> {
        return when (val result = challengeService.getChallenge(challengeId)) {
            is GetChallengeResult.Success -> ResponseEntity.ok(result.challenge)
            GetChallengeResult.ChallengeNotFound -> ResponseEntity.notFound().build()
            GetChallengeResult.UserNotFound -> ResponseEntity.notFound().build()
            GetChallengeResult.UnknownError -> ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping
    fun addChallenge(@RequestBody data: PostChallengeDto): ResponseEntity<RetrieveChallengeDto> {
        // TODO: Need to pass the users token to validate they can add this challenge
        return when (val result = challengeService.addChallenge(data)) {
            is PostChallengeResult.Success -> ResponseEntity.ok(result.challenge)
            PostChallengeResult.InvalidData -> ResponseEntity.badRequest().build()
            PostChallengeResult.UserNotFound -> ResponseEntity.notFound().build()
            PostChallengeResult.UnknownError -> ResponseEntity.internalServerError().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteChallenge(@PathVariable id: Long): ResponseEntity<Void> {
        // TODO: Need to pass the users token to validate they can delete this challenge
        val result = challengeService.deleteChallenge(id)
        return when (result) {
            DeleteChallengeResult.Success -> ResponseEntity.noContent().build()
            DeleteChallengeResult.ChallengeNotFound -> ResponseEntity.notFound().build()
            DeleteChallengeResult.UserNotFound -> ResponseEntity.notFound().build()
            DeleteChallengeResult.UnknownError -> ResponseEntity.internalServerError().build()
        }
    }
}
