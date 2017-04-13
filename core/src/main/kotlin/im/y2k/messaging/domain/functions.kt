@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package im.y2k.messaging.domain

import im.y2k.messaging.utils.*
import im.y2k.messaging.infrastructure.Bot as bot

object Domain {

    //    fun getHelpPage() = "https://github.com/y2k/SmsToTelegram/wiki/Создание-бота-через-Telegram"
    fun getHelpPage() = "https://github.com/y2k/SmsToTelegram/blob/master/docs/CREATEBOT.md"

    fun getPinCode(androidId: String): String =
        String.format("%04d", androidId.hashCode()).takeLast(4)
}

/**
344455991:AAFy8a3Cr4jPhKBGyA75AZrle2W1nww67nA
 */
fun waitForConnect(): IO<Unit> = async {
    //    val token = env.getPref("token").await()!!
//    bot.waitToConnect(token).await()

    val messages = env.getPref("token")
        .bind { bot.getNewMessages(it!!, 3000) }
        .await()

//    val message = bot.getNewMessages(token, 3000).await()

    Unit
}

fun getToken(): IO<String> =
    ask { secureID().fmap { Domain.getPinCode(it) } }

fun loadCurrentBotToken(): IO<String> =
    ask { getPref("token").fmap { it ?: "" } }

fun saveBotToken(token: String): IO<Unit> =
    ask { setPref("token", token) }

/**
 * (com.google.android.talk * Ксения Петрова: Привет) -> ()
 * (com.google.android.talk * Ксения Петрова: отправлен стикер) -> ()
 */
fun handleNotification(sbn: Notification): IO<Unit> =
    when {
        isValid(sbn) -> sendMessage("" + sbn.tickerText)
        else -> pure(Unit)
    }

private fun isValid(sbn: Notification): Boolean =
    sbn.packageName == "com.google.android.talk" && sbn.tickerText != null

private fun sendMessage(message: String): IO<Unit> =
    ask {
        getPref("user").bind { id ->
            getPref("token").bind { token ->
                bot.sendMessage(id!!, token!!.toInt(), message)
            }
        }
    }

fun openCreateBot(): IO<Unit> =
    ask { open(TargetUrl("https://telegram.me/BotFather")) }

fun openNotificationSettings(): IO<Unit> =
    ask { open(TargetAction("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }