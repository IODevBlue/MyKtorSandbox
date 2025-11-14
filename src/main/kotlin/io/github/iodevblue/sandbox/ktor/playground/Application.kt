package io.github.iodevblue.sandbox.ktor.playground

import io.github.iodevblue.sandbox.ktor.playground.eadriaticleague.ealRouting
import io.github.iodevblue.sandbox.ktor.playground.esports.esportsRouting
import io.github.iodevblue.sandbox.ktor.playground.gtleague.gtleagueRouting
import io.github.iodevblue.sandbox.ktor.playground.telegram.activateTelegramBot
import io.github.iodevblue.sandbox.ktor.playground.telegram.telegramBotRouting
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "Global exception handler: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    configureRouting()
    if(activateTelegramBot) {
        telegramBotRouting()
    }
    esportsRouting()
    ealRouting()
    gtleagueRouting()
}
