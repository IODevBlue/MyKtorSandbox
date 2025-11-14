package io.github.iodevblue.sandbox.ktor.playground.esports

import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.routing
import java.io.File

val imageCacheDir = File("cache/esports/images").apply { mkdirs() }

fun Application.esportsRouting() {
    routing {

        staticResources("/esports/static", "/static/esports")
        staticFiles("/esports/static/images", imageCacheDir)
        esportsPlayerRouting()
        esportsTournamentsRoute()
    }
}
