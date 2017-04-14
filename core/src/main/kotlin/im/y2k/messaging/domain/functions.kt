@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package im.y2k.messaging.domain

import im.y2k.messaging.domain.Domain.getCreateBotTarget
import im.y2k.messaging.domain.Domain.getOpenSettingsTarget
import im.y2k.messaging.domain.Domain.isValid
import im.y2k.messaging.utils.*
import im.y2k.messaging.infrastructure.Bot as bot

object Domain {

    fun getHelpPage() = "https://github.com/y2k/SmsToTelegram/blob/master/docs/CREATEBOT.md"

    fun getPinCode(androidId: String): String =
        String.format("%04d", androidId.hashCode()).takeLast(4)

    fun isValid(sbn: Notification): Boolean =
        sbn.packageName == "com.google.android.talk" && sbn.tickerText != null

    fun getCreateBotTarget() = TargetUrl("https://telegram.me/BotFather")
    fun getOpenSettingsTarget() = TargetAction("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")

    fun handleMessage(message: Message, pincode: String): String {
        return when (message.message) {
            pincode -> "Бот будет слать вам сообщения с телефона"
            else -> "Введите пинкод для связи бота с вашим аккаунтом"
        }
    }
}

fun getToken(): IO<String> =
    ask { secureID().fmap { Domain.getPinCode(it) } }

fun loadCurrentBotToken(): IO<String> =
    ask { getPref("token").fmap { it ?: "" } }

fun saveBotToken(token: String): IO<Unit> =
    ask { setPref("token", token) }

fun openCreateBot(): IO<Unit> =
    ask { open(getCreateBotTarget()) }

fun openNotificationSettings(): IO<Unit> =
    ask { open(getOpenSettingsTarget()) }

fun handleNotification(sbn: Notification): IO<Unit> =
    when {
        isValid(sbn) -> sendMessage("" + sbn.tickerText)
        else -> pure(Unit)
    }

private fun sendMessage(message: String): IO<Unit> = ask {
    getPref("user")
        .zip(getPref("token"))
        .bind { (id, token) -> bot.sendMessage(id!!, token!!.toInt(), message) }
}