package io.github.iodevblue.sandbox.ktor.playground.esports

import io.github.iodevblue.sandbox.ktor.playground.esports.model.Tournament
import io.github.iodevblue.sandbox.ktor.playground.esports.model.TournamentMatch
import io.github.iodevblue.sandbox.ktor.playground.esports.model.TournamentResponse
import io.github.iodevblue.sandbox.ktor.playground.esports.model.TournamentResultsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private var cachedTournaments: List<Tournament> = emptyList()
private var lastFetchTime: Instant? = null
private val cacheDuration: Duration = Duration.ofMinutes(10)

private val cachedResults = mutableMapOf<Int, TournamentResultsResponse>()
private val cacheTimestamps = mutableMapOf<Int, Instant>()
private val resultsCacheDuration: Duration = Duration.ofMinutes(15)

private val cachedMatches = mutableMapOf<Int, List<TournamentMatch>>()
private val cacheTimestampsMatch = mutableMapOf<Int, Instant>()
private val matchCacheDuration: Duration = Duration.ofMinutes(15)


fun Route.esportsTournamentsRoute() {
    get("/esports/tournaments/today") {
        val now = Instant.now()

        // ✅ Cache check
        if (cachedTournaments.isNotEmpty() && lastFetchTime != null &&
            Duration.between(lastFetchTime, now) < cacheDuration
        ) {
            return@get call.respondText(
                Json.encodeToString(
                    TournamentResponse(totalPages = 1, tournaments = cachedTournaments)
                ),
                contentType = io.ktor.http.ContentType.Application.Json
            )
        }

        val zone = ZoneId.of("UTC")
        val today = LocalDate.now(zone)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

        val dateFrom = "${today.format(dateFormatter)} 07:00"
        val dateTo = "${today.format(dateFormatter)} 22:59"

        val encodedDateFrom = dateFrom.replace(" ", "%20").replace("/", "%2F").replace(":", "%3A")
        val encodedDateTo = dateTo.replace(" ", "%20").replace("/", "%2F").replace(":", "%3A")

        val baseUrl = "https://football.esportsbattle.com/api/tournaments"

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
            // ✅ First call: page 1 to get total pages
            val firstUrl = "$baseUrl?page=1&dateFrom=$encodedDateFrom&dateTo=$encodedDateTo"
            val firstResponse: TournamentResponse = client.get(firstUrl).body()

            val allTournaments = mutableListOf<Tournament>()
            allTournaments.addAll(firstResponse.tournaments)

            // ✅ Loop through remaining pages
            if (firstResponse.totalPages > 1) {
                for (page in 2..firstResponse.totalPages) {
                    val pagedUrl = "$baseUrl?page=$page&dateFrom=$encodedDateFrom&dateTo=$encodedDateTo"
                    val response: TournamentResponse = client.get(pagedUrl).body()
                    allTournaments.addAll(response.tournaments)
                }
            }

            cachedTournaments = allTournaments
            lastFetchTime = now

            call.respondText(
                Json.encodeToString(
                    TournamentResponse(
                        totalPages = firstResponse.totalPages,
                        tournaments = allTournaments
                    )
                ),
                contentType = io.ktor.http.ContentType.Application.Json
            )

        } catch (e: Exception) {
            call.respondText(
                "Failed to fetch tournaments: ${e.message}",
                status = HttpStatusCode.InternalServerError
            )
        } finally {
            client.close()
        }
    }

    get("/esports/tournament/{id}/results") {
        val idParam = call.parameters["id"] ?: return@get call.respondText(
            "Missing tournament ID",
            status = HttpStatusCode.BadRequest
        )

        val id = idParam.toIntOrNull()
        if (id == null) {
            return@get call.respondText("Invalid tournament ID", status = HttpStatusCode.BadRequest)
        }

        val now = Instant.now()
        val cached = cachedResults[id]
        val lastFetch = cacheTimestamps[id]

        // ✅ Serve from cache if valid
        if (cached != null && lastFetch != null &&
            Duration.between(lastFetch, now) < resultsCacheDuration
        ) {
            return@get call.respondText(
                Json.encodeToString(cached),
                contentType = ContentType.Application.Json
            )
        }

        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            defaultRequest {
                headers.append("Accept", "application/json")
                headers.append("User-Agent", "KtorClient/1.0")
            }
        }

        val url = "https://football.esportsbattle.com/api/tournaments/$id/results"

        try {
            val response: TournamentResultsResponse = client.get(url).body()

            // ✅ Cache result
            cachedResults[id] = response
            cacheTimestamps[id] = now

            call.respondText(
                Json.encodeToString(response),
                contentType = ContentType.Application.Json
            )
        } catch (e: Exception) {
            call.respondText(
                "Failed to fetch tournament results: ${e.message}",
                status = HttpStatusCode.InternalServerError
            )
        } finally {
            client.close()
        }
    }

    get("/esports/tournament/{id}/matches") {
        val idParam = call.parameters["id"]
            ?: return@get call.respondText("Tournament ID missing", status = HttpStatusCode.BadRequest)

        val id = idParam.toIntOrNull()
            ?: return@get call.respondText("Invalid tournament ID", status = HttpStatusCode.BadRequest)

        val now = Instant.now()
        val cached = cachedMatches[id]
        val lastFetch = cacheTimestampsMatch[id]

        // ✅ Use cache if still valid
        if (cached != null && lastFetch != null &&
            Duration.between(lastFetch, now) < matchCacheDuration
        ) {
            return@get call.respondText(
                Json.encodeToString(ListSerializer(TournamentMatch.serializer()), cached),
                contentType = ContentType.Application.Json
            )
        }

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
            val response: List<TournamentMatch> =
                client.get("https://football.esportsbattle.com/api/tournaments/$id/matches").body()

            // ✅ Cache new results
            cachedMatches[id] = response
            cacheTimestampsMatch[id] = now

            call.respondText(
                Json.encodeToString(ListSerializer(TournamentMatch.serializer()), response),
                contentType = ContentType.Application.Json
            )
        } catch (e: Exception) {
            call.respondText(
                Json.encodeToString(mapOf("error" to e.localizedMessage)),
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.InternalServerError
            )
        } finally {
            client.close()
        }
    }
}


