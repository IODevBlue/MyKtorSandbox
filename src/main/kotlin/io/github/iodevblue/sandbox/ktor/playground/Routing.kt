package io.github.iodevblue.sandbox.ktor.playground

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/creds") {
            val cred = Credentials("IO DevBlue", "Jonin")
            call.respondText {
                cred.toString()
            }
        }
        staticResources("/image", "images")
        staticResources("/images", "images")

    }
}



data class Credentials(val name: String, val type: String)
