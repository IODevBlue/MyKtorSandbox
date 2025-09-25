package io.github.iodevblue.sandbox.ktor.playground.telegram

// Add this at the top of the file
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun Application.telegramBotRouting() {
    val botToken = environment.config.propertyOrNull("ktor.environment.TELEGRAM_TEST_BOT_TOKEN")?.getString()
        ?: error("TELEGRAM_TEST_BOT_TOKEN not set")
    val telegramApiUrl = "https://api.telegram.org/bot$botToken"
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    // This will run in background when the server starts
    val listenerJob = launch {
        var lastUpdateId = 0L
        while (isActive) {
            try {
                val response: TelegramResponse = try {
                    client.get("$telegramApiUrl/getUpdates") {
                        parameter("offset", lastUpdateId + 1)
                        parameter("timeout", 30)
                    }.body()
                } catch (e: Exception) {
                    println("Warning: failed to parse update: ${e.message}")
                    delay(1000)
                    continue
                }


                response.result.forEach { result ->
                    result.message?.let { message ->
                        val chatId = message.chat?.id
                        val text = message.text ?: ""

                        println("New message from ${message.chat?.username ?: message.chat?.first_name}: $text")

                        // --- Respond to the message ---
                        if (text.isNotEmpty()) {
                            if (text.startsWith("/")) {
                                chatId?.let { cd ->
                                    handleCommand(client, telegramApiUrl, cd, text)
                                }
                            } else if (text.isNotEmpty()) {
                                // normal echo
                                client.post("$telegramApiUrl/sendMessage") {
                                    parameter("chat_id", chatId)
                                    parameter("text", "You said: $text")
                                }
                            }
                        } else {
                            client.post("$telegramApiUrl/sendMessage") {
                                parameter("chat_id", chatId)
                                parameter("text", "Empty text! Send me a message!")
                            }
                        }
                    }

                    result.callback_query?.let { query ->
                        val callbackData = query.data ?: ""
                        val chatId = query.from?.id

                        println("Button clicked: $callbackData from ${query.from?.username ?: query.from?.first_name}")

                        // respond to the button
                        client.post("$telegramApiUrl/sendMessage") {
                            parameter("chat_id", chatId)
                            parameter("text", "You clicked: $callbackData")
                        }

                        // Optional: answerCallbackQuery to remove the loading spinner
                        client.post("$telegramApiUrl/answerCallbackQuery") {
                            parameter("callback_query_id", query.id)
                            parameter("text", "Received!")
                        }
                    }

                    result.update_id?.let { if (it > lastUpdateId) lastUpdateId = result.update_id }
                }

            } catch (e: Exception) {
                println("Error fetching updates: ${e.message}")
                delay(5000)
            }
        }
    }

    // Optional: stop listener when server shuts down
    environment.monitor.subscribe(ApplicationStopped) {
        listenerJob.cancel()
    }

    routing {
        get("/telegram/messages") {
            try {
                // Fetch updates from Telegram
                val telegramResponse: TelegramResponse = client.get("$telegramApiUrl/getUpdates") {
                    parameter("limit", 10)
                }.body()  // ⚡ directly parses JSON to your class

                val messages = telegramResponse.result.mapNotNull { it.message?.text }
                val rest = buildString {
                    messages.forEach {
                        append(it)
                        append("\n")
                    }
                }
                call.respond(rest)
            } catch (e: Exception) {
                call.respondText("Error fetching messages: ${e.message}")
            }
        }
    }

}

suspend fun handleCommand(client: HttpClient, telegramApiUrl: String, chatId: Long, text: String) {
    when (text.trim()) {
        "/options" -> {
            val buttons = """
                {
                  "inline_keyboard": [
                    [{"text":"Option 1","callback_data":"opt1"}],
                    [{"text":"Option 2","callback_data":"opt2"}],
                    [{"text":"Option 3","callback_data":"opt3"}]
                  ]
                }
            """.trimIndent()

            client.post("$telegramApiUrl/sendMessage") {
                parameter("chat_id", chatId)
                parameter("text", "Choose an option:")
                parameter("reply_markup", buttons)
            }
        }

        "/help" -> {
            client.post("$telegramApiUrl/sendMessage") {
                parameter("chat_id", chatId)
                parameter("text", "Available commands:\n/options - Show options\n/help - Show this message")
            }
        }

        else -> {
            // fallback response
            client.post("$telegramApiUrl/sendMessage") {
                parameter("chat_id", chatId)
                parameter("text", "Unknown command: $text")
            }
        }
    }
}



