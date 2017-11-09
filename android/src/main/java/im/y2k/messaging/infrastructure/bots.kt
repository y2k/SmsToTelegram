package im.y2k.messaging.infrastructure

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.GetUpdates
import im.y2k.messaging.client.Result
import im.y2k.messaging.client.executeAsync
import im.y2k.messaging.client.map

suspend fun loadNewMessages(token: String): Result<List<Update>, Exception> =
    TelegramBot(token)
        .executeAsync(GetUpdates())
        .map { it.updates() }