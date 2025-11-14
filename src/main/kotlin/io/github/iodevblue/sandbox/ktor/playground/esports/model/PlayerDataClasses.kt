package io.github.iodevblue.sandbox.ktor.playground.esports.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
    val nickname: String,
    val tournamentsWon: Int,
    val totalTournaments: Int,
    val totalMatches: Int
)

@Serializable
data class PlayerResponse(
    val id: Int,
    val nickname: String,
    val disqualified: Int,
    val photo: String? = null,
    val birthdate: Int,
    val address: Address,
    val totalTournamentWin: Int,
    val totalTournaments: Int,
    val totalMatches: Int,
    val totalWin: Int,
    val totalLose: Int,
    val totalDraw: Int
)

@Serializable
data class Address(
    val city: String,
    val country: Country
)

@Serializable
data class Country(
    val token: String,
    val token_international: String
)

@Serializable
data class Probabilities(
    val player1Win: Double,
    val player2Win: Double,
    val draw: Double
)

@Serializable
data class MatchupResult(
    val player1: String,
    val player2: String,
    val probability: Probabilities,
    val recommendation: String,
    val expectedGoals: Double,
    val likelyOverUnder: String // e.g. "Over 2.5"
)

