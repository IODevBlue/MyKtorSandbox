package io.github.iodevblue.sandbox.ktor.playground.esports

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText

class EsportsPlayerApiTest : StringSpec({

    val client = HttpClient(CIO)

    "fetch player Hristian05 and print raw response" {
        val playerID = "Hristian05"
        val url = "https://football.esportsbattle.com/api/participants/$playerID"

        val response: String = client.get(url).bodyAsText()

        println("Fetched Response:\n$response")

        // At least assert that the nickname appears in the HTML
        response shouldContain playerID
    }
})
