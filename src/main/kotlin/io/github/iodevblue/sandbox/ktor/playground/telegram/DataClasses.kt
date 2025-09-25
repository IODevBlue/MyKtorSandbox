package io.github.iodevblue.sandbox.ktor.playground.telegram

import kotlinx.serialization.Serializable

// Telegram data classes
@Serializable
data class TelegramUpdate(val message: TelegramMessage? = null)

@Serializable
data class TelegramMessage(
    val message_id: Int? = null,
    val from: TelegramUser? = null,
    val chat: TelegramChat,
    val text: String? = null
)

@Serializable
data class TelegramUser(
    val id: Long? = null,
    val is_bot: Boolean? = null,
    val first_name: String? = null,
    val last_name: String? = null,
    val username: String? = null
)
@Serializable
data class TelegramChat(
    val id: Long? = null,
    val type: String? = null,
    val title: String? = null,
    val username: String? = null,
    val first_name: String? = null,
    val last_name: String? = null
)

@Serializable
data class TelegramGetUpdatesResponse(
    val ok: Boolean? = null,
    val result: List<TelegramUpdate>
)

@Serializable
data class Result(
    val update_id: Long? = null,
    val message: Message? = null,
    val callback_query: CallbackQuery? = null
)

@Serializable
data class CallbackQuery(
    val id: String? = null,
    val from: Chat? = null,
    val message: Message? = null,
    val data: String? = null // This is what your callback_data contains
)

@Serializable
data class TelegramResponse(
    val ok: Boolean? = null,
    val result: List<Result> = emptyList()
)

@Serializable
data class Message(
    val chat: TelegramChat? = null,
    val date: Int? = null,
    val entities: List<Entity> = emptyList(), // default empty if missing
    val from: From? = null,                   // nullable if missing
    val message_id: Int? = null,
    val text: String? = null                  // nullable if not a text message
)

@Serializable
data class Entity(
    val length: Int? = null,
    val offset: Int? = null,
    val type: String? = null
)

@Serializable
data class From(
    val first_name: String? = null,
    val id: Long? = null,
    val is_bot: Boolean? = null,
    val language_code: String? = null,
    val username: String? = null
)

@Serializable

data class Chat(
    val first_name: String? = null,
    val id: Long? = null,
    val type: String? = null,
    val username: String? = null
)