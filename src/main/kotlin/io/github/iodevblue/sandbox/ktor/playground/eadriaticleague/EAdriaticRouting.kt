package io.github.iodevblue.sandbox.ktor.playground.eadriaticleague


import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.time.Duration
import java.time.Instant

private var cachedPlayers: List<EALPlayer> = emptyList()
private var lastFetchTime: Instant? = null
private val cacheDuration: Duration = Duration.ofMinutes(10) // refresh every 10 minutes

fun Application.ealRouting() {
    routing {
        get("/eal/grand-prix-2025") {
            val now = Instant.now()
            val shouldRefresh = lastFetchTime == null || Duration.between(lastFetchTime, now) > cacheDuration

            if (shouldRefresh) {
                val url = "https://eadriaticleague.com/grand-prix-final-eadriaticleague-2025/"

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
                    val html = client.get(url).bodyAsText()
                    val doc = Jsoup.parse(html)

                    // Select all inner-section player rows
                    val playerSections = doc.select("section.elementor-inner-section")

                    cachedPlayers = playerSections.mapNotNull { section ->
                        val columns = section.select("div.elementor-col-33 p")
                        if (columns.size < 3) return@mapNotNull null

                        val nameCategoryText = columns[1].text() // e.g., "Panther (Cybersharks esport)"
                        val pointsText = columns[2].text().replace(",", "")

                        val name = nameCategoryText.substringBefore("(").trim()
                        val category = nameCategoryText.substringAfter("(").substringBefore(")").trim()
                        val points = pointsText.toDoubleOrNull() ?: 0.0

                        EALPlayer(name, category, points)
                    }

                    lastFetchTime = now
                } finally {
                    client.close()
                }
            }

            call.respondText(
                Json.encodeToString(cachedPlayers),
                contentType = io.ktor.http.ContentType.Application.Json
            )
        }

        get("/eal/predictor") {
            val htmlText = this::class.java.classLoader
                .getResource("static/eadriaticleague/index.html")  // resources/static/esports/index.html
                ?.readText()
                ?: return@get call.respondText(
                    "index.html not found",
                    status = io.ktor.http.HttpStatusCode.NotFound
                )
            call.respondText(htmlText, contentType = io.ktor.http.ContentType.Text.Html)
        }
    }
}
