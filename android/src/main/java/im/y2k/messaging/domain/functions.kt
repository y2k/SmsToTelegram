package im.y2k.messaging.domain

import android.content.ComponentName
import android.content.SharedPreferences
import android.provider.Settings.Secure
import com.facebook.litho.ComponentContext
import com.pengrad.telegrambot.model.Update
import im.y2k.messaging.client.NotificationListener
import im.y2k.messaging.client.getAndroidId
import im.y2k.messaging.client.map
import im.y2k.messaging.domain.Domain.isValid
import kotlinx.coroutines.experimental.channels.actor
import im.y2k.messaging.domain.Bot as bot

fun validateSettings(context: ComponentContext): ValidationResult {
    val cn = ComponentName(context, NotificationListener::class.java)
    val enabled = Secure
        .getString(context.contentResolver, "enabled_notification_listeners")
        ?.contains(cn.flattenToString())
        ?: false

    context
        .getSharedPreferences(Preferences.name, 0)
        .let(Preferences::getToken)

    return ValidationResult(enabled, false) // FIXME:
}

suspend fun validateSettings(pincode: String): ValidationResult {

    val xs =
        loadNewMessages()
            .map { Domain.findPincode(it, pincode) }

    TODO()
}

fun getToken(ctx: ComponentContext): String =
    ctx.contentResolver
        .let(::getAndroidId)
        .let(Domain::getPinCode)

object Preferences {

    val name = "default"

    fun getToken(p: SharedPreferences): String? =
        p.getString("token", null)
}

object Domain {

    fun findPincode(xs: List<Update>, pincode: String): Boolean =
        xs.any { it.message().text() == pincode }

    fun getPinCode(androidId: String): String =
        String.format("%04d", androidId.hashCode()).takeLast(4)

    fun isValid(sbn: Notification): Boolean =
        sbn.packageName == "com.google.android.talk" && sbn.tickerText != null

    fun handleMessage(message: String, pincode: String) = when (message) {
        pincode -> "Бот будет слать вам сообщения с телефона"
        else -> "Введите пинкод для связи бота с вашим аккаунтом"
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