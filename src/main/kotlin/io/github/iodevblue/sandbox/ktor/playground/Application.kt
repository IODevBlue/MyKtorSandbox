package io.github.iodevblue.sandbox.ktor.playground

import io.github.iodevblue.sandbox.ktor.playground.eadriaticleague.ealRouting
import io.github.iodevblue.sandbox.ktor.playground.esports.esportsRouting
import io.github.iodevblue.sandbox.ktor.playground.gtleague.gtleagueRouting
import io.github.iodevblue.sandbox.ktor.playground.telegram.activateTelegramBot
import io.github.iodevblue.sandbox.ktor.playground.telegram.telegramBotRouting
import io.ktor.server.application.*


fun main(args: Array<String>) {

    io.ktor.server.netty.EngineMain.main(args)

}

fun Application.module() {
    configureRouting()
    if(activateTelegramBot) {
        telegramBotRouting()
    }
    esportsRouting()
    ealRouting()
    gtleagueRouting()
}
