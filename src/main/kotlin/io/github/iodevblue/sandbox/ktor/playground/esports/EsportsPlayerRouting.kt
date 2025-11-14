package io.github.iodevblue.sandbox.ktor.playground.esports

import io.github.iodevblue.sandbox.ktor.playground.esports.model.MatchupResult
import io.github.iodevblue.sandbox.ktor.playground.esports.model.PlayerData
import io.github.iodevblue.sandbox.ktor.playground.esports.model.PlayerResponse
import io.github.iodevblue.sandbox.ktor.playground.esports.model.Probabilities
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.time.Instant

// TODO: Tap a user to get the matches they will have.

// Global cache variables
private var cachedPlayers: List<PlayerData> = emptyList()
private var lastFetchTime: Instant? = null
private val cacheDuration: Duration = Duration.ofMinutes(10) // refresh every 10 minutes
private var lastTotalPages: Int = 0 // store last seen totalPages

private val cachedPlayerMap: MutableMap<String, PlayerResponse> = mutableMapOf()
private val playerCacheTime: MutableMap<String, Instant> = mutableMapOf()
private val playerCacheDuration: Duration = Duration.ofMinutes(10)

fun Route.esportsPlayerRouting() {

    staticResources("/esports/static", "/static/esports")
    staticFiles("/esports/static/images", imageCacheDir)

    get("/esports/players") {
        val now = Instant.now()
        val shouldRefresh = lastFetchTime == null ||
                Duration.between(lastFetchTime, now) > cacheDuration ||
                cachedPlayers.isEmpty()

        if (shouldRefresh) {
            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
                defaultRequest {
                    headers.append("Accept", "application/json")
                    headers.append("User-Agent", "KtorClient/1.0")
                }
            }

            try {
                val allPlayers = mutableListOf<PlayerData>()
                val firstUrl = "https://football.esportsbattle.com/api/participants?page=1"
                val firstResponse: String = client.get(firstUrl).bodyAsText()
                val firstJson = Json.parseToJsonElement(firstResponse).jsonObject
                val totalPages = firstJson["totalPages"]?.jsonPrimitive?.intOrNull ?: 1
                lastTotalPages = totalPages
                val firstParticipants = firstJson["participants"]?.jsonArray ?: emptyList()
                allPlayers += parsePlayers(firstParticipants)

                for (page in 2..totalPages) {
                    val url = "https://football.esportsbattle.com/api/participants?page=$page"
                    val response: String = client.get(url).bodyAsText()
                    val jsonElement = Json.parseToJsonElement(response).jsonObject
                    val participants = jsonElement["participants"]?.jsonArray ?: continue
                    allPlayers += parsePlayers(participants)
                }

                cachedPlayers = allPlayers.sortedByDescending { it.tournamentsWon }
                lastFetchTime = now
            } finally {
                client.close()
            }
        }

        // Return JSON array of nicknames for autocomplete
        val nicknames = cachedPlayers.map { it.nickname }
        call.respondText(Json.encodeToString(nicknames), contentType = io.ktor.http.ContentType.Application.Json)
    }

    get("/esports/player/{nickname}") {
        val nickname = call.parameters["nickname"] ?: return@get call.respondText(
            "Missing nickname",
            status = io.ktor.http.HttpStatusCode.BadRequest
        )

        val refresh = call.request.queryParameters["refresh"]?.toBoolean() ?: false
        val now = Instant.now()
        val cachedPlayer = cachedPlayerMap[nickname]
        val lastFetch = playerCacheTime[nickname]

        // Reuse cached player if still valid
        val player = if (!refresh && cachedPlayer != null && lastFetch != null &&
            Duration.between(lastFetch, now) <= playerCacheDuration
        ) {
            cachedPlayer
        } else {
            val client = HttpClient(CIO) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
            try {
                val url = "https://football.esportsbattle.com/api/participants/$nickname"
                val freshPlayer: PlayerResponse = client.get(url).body()
                cachedPlayerMap[nickname] = freshPlayer
                playerCacheTime[nickname] = now
                freshPlayer
            } catch (e: Exception) {
                return@get call.respondText(
                    "Player '$nickname' not found or API error: ${e.message}",
                    status = io.ktor.http.HttpStatusCode.NotFound
                )
            } finally {
                client.close()
            }
        }

        // Cache the photo locally (runtime filesystem, not resources/)
        val clientForImage = HttpClient(CIO)
        val localPhotoPath = cachePlayerPhoto(player.photo ?: "", player.nickname, clientForImage)
        clientForImage.close()

        // If caching failed, use placeholder
        val photoPublicPath = if (localPhotoPath.isNotBlank()) {
            localPhotoPath
        } else {
            "https://via.placeholder.com/100"
        }

        // Send enriched response
        val enrichedPlayer = player.copy(photo = photoPublicPath)
        val prettyJson = Json.encodeToString(PlayerResponse.serializer(), enrichedPlayer)
        call.respondText(prettyJson, contentType = io.ktor.http.ContentType.Application.Json)
    }

    get("/esports/matchup/{player1}/{player2}") {
        val player1Nick = call.parameters["player1"] ?: return@get call.respondText(
            "Missing player1", status = io.ktor.http.HttpStatusCode.BadRequest
        )
        val player2Nick = call.parameters["player2"] ?: return@get call.respondText(
            "Missing player2", status = io.ktor.http.HttpStatusCode.BadRequest
        )

        val refresh = call.request.queryParameters["refresh"]?.toBoolean() ?: false

        suspend fun getPlayer(nick: String, forceRefresh: Boolean = false): PlayerResponse? {
            val now = Instant.now()
            val cached = cachedPlayerMap[nick]
            val lastFetch = playerCacheTime[nick]
            if (!forceRefresh && cached != null && lastFetch != null &&
                Duration.between(lastFetch, now) <= playerCacheDuration
            ) return cached

            val client = HttpClient(CIO) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                defaultRequest {
                    headers.append("Accept", "application/json")
                    headers.append("User-Agent", "KtorClient/1.0")
                }
            }

            return try {
                val player: PlayerResponse = client
                    .get("https://football.esportsbattle.com/api/participants/$nick")
                    .body()
                cachedPlayerMap[nick] = player
                playerCacheTime[nick] = now
                player
            } catch (_: Exception) {
                null
            } finally {
                client.close()
            }
        }

        val p1 = getPlayer(player1Nick, refresh)
        val p2 = getPlayer(player2Nick, refresh)

        if (p1 == null || p2 == null) {
            return@get call.respondText(
                "One or both players not found", status = io.ktor.http.HttpStatusCode.NotFound
            )
        }

        val (matchup, recommendation) = calculateMatchupProbability(p1, p2)

        val probs = matchup.probability
        val expectedGoals = matchup.expectedGoals
        val likelyOverUnder = matchup.likelyOverUnder


        val result = MatchupResult(
            player1 = p1.nickname,
            player2 = p2.nickname,
            probability = probs,
            recommendation = recommendation,
            expectedGoals = expectedGoals,
            likelyOverUnder = likelyOverUnder
        )

        call.respondText(
            Json.encodeToString(MatchupResult.serializer(), result),
            contentType = io.ktor.http.ContentType.Application.Json
        )
    }

    get("/esports/predictor") {
        val htmlText = this::class.java.classLoader
            .getResource("static/esports/index.html")  // resources/static/esports/index.html
            ?.readText()
            ?: return@get call.respondText(
                "index.html not found",
                status = io.ktor.http.HttpStatusCode.NotFound
            )
        call.respondText(htmlText, contentType = io.ktor.http.ContentType.Text.Html)
    }

}

private fun parsePlayers(participants: List<kotlinx.serialization.json.JsonElement>): List<PlayerData> {
    return participants.map { elem ->
        val obj = elem.jsonObject
        val name = obj["nickname"]?.jsonPrimitive?.content ?: "?"
        val wins = obj["totalTournamentWin"]?.jsonPrimitive?.intOrNull ?: 0
        val tournaments = obj["totalTournaments"]?.jsonPrimitive?.intOrNull ?: 0
        val matches = obj["totalMatches"]?.jsonPrimitive?.intOrNull ?: 0

        PlayerData(name, wins, tournaments, matches)
    }
}

private fun calculateMatchupProbability(p1: PlayerResponse, p2: PlayerResponse): Pair<MatchupResult, String> {
    if (p1.totalMatches == 0 || p2.totalMatches == 0) {
        val probs = Probabilities(0.5, 0.5, 0.0)
        return MatchupResult(
            player1 = p1.nickname,
            player2 = p2.nickname,
            probability = probs,
            recommendation = "Insufficient data",
            expectedGoals = 3.0,
            likelyOverUnder = "Over 2.5"
        ) to "Insufficient data"
    }

    // --- Base rates ---
    val p1WinRate = p1.totalWin.toDouble() / p1.totalMatches
    val p2WinRate = p2.totalWin.toDouble() / p2.totalMatches
    val p1DrawRate = p1.totalDraw.toDouble() / p1.totalMatches
    val p2DrawRate = p2.totalDraw.toDouble() / p2.totalMatches
    val p1LoseRate = p1.totalLose.toDouble() / p1.totalMatches
    val p2LoseRate = p2.totalLose.toDouble() / p2.totalMatches

    // --- Scoring potential heuristic ---
    // Players with many wins and few draws/losses → higher scoring tendency
    val p1Aggression = (p1WinRate * 2.0) - (p1DrawRate * 0.5)
    val p2Aggression = (p2WinRate * 2.0) - (p2DrawRate * 0.5)

    // Scale aggression by match volume (more games = more stable pattern)
    val volumeFactor1 = 1 + (p1.totalMatches / 10000.0).coerceAtMost(0.5)
    val volumeFactor2 = 1 + (p2.totalMatches / 10000.0).coerceAtMost(0.5)

    val adjAgg1 = p1Aggression * volumeFactor1
    val adjAgg2 = p2Aggression * volumeFactor2

    // --- Win probability estimation ---
    val p1Score = adjAgg1 + 0.5 * p1DrawRate
    val p2Score = adjAgg2 + 0.5 * p2DrawRate
    val total = p1Score + p2Score
    val player1Prob = if (total > 0) p1Score / total else 0.5
    val player2Prob = if (total > 0) p2Score / total else 0.5
    val drawProb = ((p1DrawRate + p2DrawRate) / 2).coerceAtMost(0.25) // esports: fewer draws

    val norm = player1Prob + player2Prob + drawProb
    val probs = Probabilities(
        player1Win = player1Prob / norm,
        player2Win = player2Prob / norm,
        draw = drawProb / norm
    )

    // --- Expected goals ---
    // Scale upwards for high win/loss frequency players
    val offensiveIndex = (p1Aggression + p2Aggression).coerceIn(0.5, 4.0)
    val consistencyBoost = ((p1.totalMatches + p2.totalMatches) / 20000.0).coerceAtMost(0.5)
    val volatility = 1.0 + (1.0 - (p1DrawRate + p2DrawRate) / 2) // fewer draws = more volatile

    var expectedGoals = (offensiveIndex * 2.0 + volatility + consistencyBoost * 3)
    expectedGoals = expectedGoals.coerceIn(2.0, 9.0)

    // --- Likely Over/Under ---
    val thresholds = listOf(0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5)
    val nearest = thresholds.minBy { kotlin.math.abs(expectedGoals - it) }
    val likelyOverUnder = if (expectedGoals >= nearest) "Over $nearest" else "Under $nearest"

    val recommendation = when {
        probs.player1Win > probs.player2Win && probs.player1Win > probs.draw ->
            "${p1.nickname} is likely to win"
        probs.player2Win > probs.player1Win && probs.player2Win > probs.draw ->
            "${p2.nickname} is likely to win"
        else -> "The match is likely to be tight"
    }

    val result = MatchupResult(
        player1 = p1.nickname,
        player2 = p2.nickname,
        probability = probs,
        recommendation = recommendation,
        expectedGoals = String.format("%.2f", expectedGoals).toDouble(),
        likelyOverUnder = likelyOverUnder
    )

    return result to recommendation
}

private suspend fun cachePlayerPhoto(photoId: String, nickname: String, client: HttpClient): String {
    // If no photo id, return empty so caller can use fallback
    if (photoId.isBlank()) return ""

    // safe filename base
    val safeBase = nickname.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    // We'll choose extension by checking Content-Type
    val remoteUrl = "https://football.esportsbattle.com/api/Image/efootball/460x460/$photoId"

    try {
        val response: HttpResponse = client.get(remoteUrl)
        val contentType = response.headers["Content-Type"]?.lowercase() ?: ""
        val ext = when {
            "jpeg" in contentType || "jpg" in contentType -> ".jpg"
            "png" in contentType -> ".png"
            "gif" in contentType -> ".gif"
            else -> {
                // fallback try derive from URL fragment, else .jpg
                val fromUrlExt = remoteUrl.substringAfterLast('.', "")
                if (fromUrlExt.length <= 4 && fromUrlExt.matches(Regex("[a-zA-Z0-9]{1,4}"))) ".$fromUrlExt" else ".jpg"
            }
        }

        val filename = "$safeBase$ext"
        val localFile = File(imageCacheDir, filename)

        if (localFile.exists() && localFile.length() > 0) {
            // Return public path - matches static mapping above
            return "/esports/static/images/${localFile.name}"
        }

        // ensure directory exists
        localFile.parentFile?.mkdirs()

        // write response body to file
        response.bodyAsChannel().toInputStream().use { input ->
            localFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        // Optionally: set file permissions or ensure flushed
        Files.setAttribute(localFile.toPath(), "dos:readonly", false) // optional on Windows

        println("Cached image -> ${localFile.absolutePath}")
        return "/esports/static/images/${localFile.name}"
    } catch (e: Exception) {
        println("Failed to download/cached photo for $nickname: ${e.message}")
        return "" // caller will use fallback
    }
}

private fun calculateExpectedGoals(p1: PlayerResponse, p2: PlayerResponse, probs: Probabilities): Double {
    // Example formula — adjust to your logic
    return (probs.player1Win * 2.0 + probs.draw * 1.5 + probs.player2Win * 2.0)
}

private fun determineLikelyOverUnder(expectedGoals: Double): String {
    return when {
        expectedGoals >= 3.0 -> "Over 2.5"
        expectedGoals >= 2.0 -> "Over 1.5"
        else -> "Under 2.5"
    }
}
