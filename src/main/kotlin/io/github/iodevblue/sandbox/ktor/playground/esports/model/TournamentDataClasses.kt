package io.github.iodevblue.sandbox.ktor.playground.esports.model

import kotlinx.serialization.Serializable

@Serializable
data class LeagueInfo(
    val id: Int,
    val token: String,
    val token_international: String
)

@Serializable
data class LocationInfo(
    val id: Int,
    val code: String,
    val color: String,
    val token: String,
    val token_international: String
)

@Serializable
data class Tournament(
    val id: Int,
    val token: String,
    val token_international: String,
    val marker: String,
    val status_id: Int,
    val start_date: String,
    val league: LeagueInfo,
    val location: LocationInfo
)

@Serializable
data class TournamentResponse(
    val totalPages: Int,
    val tournaments: List<Tournament>
)

@Serializable
data class TournamentResultsResponse(
    val id: Int,
    val status_id: Int,
    val token: String,
    val token_international: String,
    val results: List<TournamentResultEntry>
)

@Serializable
data class TournamentResultEntry(
    val details: TournamentDetails,
    val participant: TournamentParticipant
)

@Serializable
data class TournamentDetails(
    val D: Int,
    val L: Int,
    val W: Int,
    val GA: Int,
    val GD: Int,
    val GF: Int,
    val GP: Int,
    val PTS: Int,
    val position: Int
)

@Serializable
data class TournamentParticipant(
    val id: Int,
    val nickname: String,
    val team: TournamentTeam,
    val photo: String? = null
)

@Serializable
data class TournamentTeam(
    val token: String,
    val token_international: String,
    val logo: String
)

@Serializable
data class TournamentMatch(
    val id: Int,
    val participant1: Participant?,
    val participant2: Participant?,
    val startTime: String? = null,
    val status: String? = null,
    val winner: Int? = null,
    val tournamentId: Int? = null,
    val bestOf: Int? = null
)

@Serializable
data class Participant(
    val id: Int,
    val nickname: String?,
    val score: Int?, // ✅ must be nullable
    val photo: String?
)

@Serializable
data class TournamentInfo(
    val id: Int,
    val token: String,
    val token_international: String,
    val status_id: Int
)

@Serializable
data class ConsoleInfo(
    val id: Int,
    val token: String,
    val token_international: String
)

@Serializable
data class ParticipantInfo(
    val id: Int,
    val nickname: String,
    val score: Int,
    val photo: String,
    val team: TeamInfo,
    val prevPeriodsScores: List<String>
)

@Serializable
data class TeamInfo(
    val id: Int,
    val token: String,
    val token_international: String,
    val logo: String
)
