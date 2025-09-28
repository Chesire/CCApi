package com.chesire.capi.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// Questions to answer:
// Should we get the user token by header or query param?
@RestController
@RequestMapping("/api/challenge")
class ChallengeController {

    @GetMapping
    fun getChallenges(): String {
        // TODO: Need to get all challenges for a single user, return as a JSON array.
        return "Challenges requested"
    }

    @GetMapping("/{id}")
    fun getChallenge(@PathVariable id: Long): ResponseEntity<String> {
        // TODO: Need to get a single challenge by its ID, return as a JSON object.
        return ResponseEntity.notFound().build()
    }

    @PostMapping
    fun addChallenge(@PathVariable data: Any): String {
        // TODO: Need to add a challenge for a user, return the created challenge as a JSON object.
        return "Challenge add $data requested"
    }

    @DeleteMapping("/{id}")
    fun deleteChallenge(@PathVariable id: Long): ResponseEntity<Void> {
        // TODO: Need to delete a challenge for a user.
        return ResponseEntity.noContent().build()
    }
}
