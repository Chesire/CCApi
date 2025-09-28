package com.chesire.capi.controller

import com.chesire.capi.dto.ChallengeDto
import com.chesire.capi.service.ChallengeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/challenge")
class ChallengeController(private val challengeService: ChallengeService) {

    @GetMapping("/{userId}")
    fun getChallenges(@PathVariable userId: Long): ResponseEntity<List<ChallengeDto>> {
        // TODO: Need to get all user challenges by their id, return as a JSON array.
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/{challengeId}")
    fun getChallenge(@PathVariable challengeId: Long): ResponseEntity<ChallengeDto> {
        // TODO: Need to get a single challenge by its ID, return as a JSON object.
        return ResponseEntity.notFound().build()
    }

    // Questions to answer:
    // Should we get the user token by header or query param?
    @PostMapping
    fun addChallenge(@PathVariable data: Any): String {
        // TODO: Need to add a challenge for a user, return the created challenge as a JSON object.
        return "Challenge add $data requested"
    }

    // Questions to answer:
    // Should we get the user token by header or query param?
    @DeleteMapping("/{id}")
    fun deleteChallenge(@PathVariable id: Long): ResponseEntity<Void> {
        // TODO: Need to delete a challenge for a user.
        return ResponseEntity.noContent().build()
    }
}
