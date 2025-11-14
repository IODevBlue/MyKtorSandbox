package io.github.iodevblue.sandbox.ktor.playground.gtleague

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.io.IOException
import java.time.Duration
import java.time.Instant

private var cachedPlayers: List<GlobalRanking> = globalRankings
private var lastFetchTime: Instant? = null
private val cacheDuration: Duration = Duration.ofMinutes(10)

fun Application.gtleagueRouting() {
    routing {
        staticResources("/gtleague/static", "/static/gtleague")
        get("/gtleague/players") {
            val nicknames = cachedPlayers.map { it.player }
            call.respondText(Json.encodeToString(nicknames), contentType = io.ktor.http.ContentType.Application.Json)
        }

        get("/gtleague/player/{name}") {
            val name = call.parameters["name"] ?: return@get call.respondText(
                "Missing player name", status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val player = cachedPlayers.find { it.player.equals(name, ignoreCase = true) }
            if (player == null) {
                call.respondText("Player '$name' not found", status = io.ktor.http.HttpStatusCode.NotFound)
                return@get
            }
            call.respondText(Json.encodeToString(player), contentType = io.ktor.http.ContentType.Application.Json)
        }

        get("/gtleague/matchup/{player1}/{player2}") {
            val p1Name = call.parameters["player1"] ?: return@get call.respondText(
                "Missing player1", status = io.ktor.http.HttpStatusCode.BadRequest
            )
            val p2Name = call.parameters["player2"] ?: return@get call.respondText(
                "Missing player2", status = io.ktor.http.HttpStatusCode.BadRequest
            )

            val p1 = cachedPlayers.find { it.player.equals(p1Name, ignoreCase = true) }
            val p2 = cachedPlayers.find { it.player.equals(p2Name, ignoreCase = true) }

            if (p1 == null || p2 == null) {
                call.respondText("One or both players not found", status = io.ktor.http.HttpStatusCode.NotFound)
                return@get
            }

            val result = calculateGtleagueMatchup(p1, p2)
            call.respondText(Json.encodeToString(result), contentType = io.ktor.http.ContentType.Application.Json)
        }

        get("/gtleague/predictor") {
            val htmlText = this::class.java.classLoader
                .getResource("static/gtleague/index.html")?.readText()
                ?: return@get call.respondText("index.html not found", status = io.ktor.http.HttpStatusCode.NotFound)
            call.respondText(htmlText, contentType = io.ktor.http.ContentType.Text.Html)
        }

        get("/gtleague/ranking") {
            // TODO: Get this done
        }
    }
}

private fun calculateGtleagueMatchup(p1: GlobalRanking, p2: GlobalRanking): MatchupResult {
    // Win probability based on pointsPerMatch
    val totalPoints = p1.pointsPerMatch + p2.pointsPerMatch
    val p1Prob = if (totalPoints > 0) p1.pointsPerMatch / totalPoints else 0.5
    val p2Prob = if (totalPoints > 0) p2.pointsPerMatch / totalPoints else 0.5
    val drawProb = ((p1.drawPercent + p2.drawPercent) / 2) / 100

    val recommendation = when {
        p1Prob > p2Prob && p1Prob > drawProb -> "${p1.player} is likely to win"
        p2Prob > p1Prob && p2Prob > drawProb -> "${p2.player} is likely to win"
        else -> "The match is likely to be a draw"
    }

    // Estimate goals
    val expectedTotalGoals = ((p1.goalsForPerMatch + p2.goalsForPerMatch) / 2)
    val expectedGoalsPlayer1 = (p1.goalsForPerMatch + p2.goalsAgainstPerMatch) / 2
    val expectedGoalsPlayer2 = (p2.goalsForPerMatch + p1.goalsAgainstPerMatch) / 2
    val expectedConcedePlayer1 = expectedGoalsPlayer2
    val expectedConcedePlayer2 = expectedGoalsPlayer1


    return MatchupResult(
        player1 = p1.player,
        player2 = p2.player,
        probability = Probabilities(
            player1Win = p1Prob,
            player2Win = p2Prob,
            draw = drawProb
        ),
        recommendation = recommendation,
        expectedTotalGoals = expectedTotalGoals,
        expectedGoalsPlayer1 = expectedGoalsPlayer1,
        expectedGoalsPlayer2 = expectedGoalsPlayer2,
        expectedConcedePlayer1 = expectedConcedePlayer1,
        expectedConcedePlayer2 = expectedConcedePlayer2,
        player1Stats = p1,
        player2Stats = p2
    )
}
