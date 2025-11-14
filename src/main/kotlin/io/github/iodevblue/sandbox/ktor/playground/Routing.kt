package io.github.iodevblue.sandbox.ktor.playground

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

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
        get("/error") {
            throw IllegalStateException("Error. Testing the error for Ktor server.")
        }

    }
}



data class Credentials(val name: String, val type: String)
