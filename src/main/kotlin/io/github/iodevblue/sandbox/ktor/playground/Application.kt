package io.github.iodevblue.sandbox.ktor.playground

import io.github.iodevblue.sandbox.ktor.playground.telegram.telegramBotRouting
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {

    io.ktor.server.netty.EngineMain.main(args)

}

fun Application.module() {
    configureRouting()
    telegramBotRouting()
}
