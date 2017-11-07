package im.y2k.messaging.domain

import im.y2k.messaging.domain.Domain.isValid
import im.y2k.messaging.infrastructure.TelegramMsg
import im.y2k.messaging.infrastructure.botActor
import kotlinx.coroutines.experimental.channels.actor
import im.y2k.messaging.infrastructure.Bot as bot

object Domain {

    fun getPinCode(androidId: String): String =
        String.format("%04d", androidId.hashCode()).takeLast(4)

    fun isValid(sbn: Notification): Boolean =
        sbn.packageName == "com.google.android.talk" && sbn.tickerText != null

    fun handleMessage(message: String, pincode: String): String {
        return when (message) {
            pincode -> "Бот будет слать вам сообщения с телефона"
            else -> "Введите пинкод для связи бота с вашим аккаунтом"
        }
    }
}

val notificationActor = actor<NotificationWithToken> {
    while (true) {
        val x = receive()
        handleNotification(x.notification, x.id, x.token)
    }
}

suspend fun handleNotification(sbn: Notification, id: String, token: String) {
    if (isValid(sbn))
        botActor.offer(TelegramMsg(id.toInt(), "" + sbn.tickerText, token))
}