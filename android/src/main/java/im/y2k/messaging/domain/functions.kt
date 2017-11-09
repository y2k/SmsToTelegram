package im.y2k.messaging.domain

import android.content.ComponentName
import android.content.SharedPreferences
import android.provider.Settings.Secure
import android.service.notification.StatusBarNotification
import com.facebook.litho.ComponentContext
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.SendMessage
import im.y2k.messaging.client.*
import im.y2k.messaging.infrastructure.loadNewMessages

object Notifications {

    fun tryCreateNotification(action: MessageToTelegram, pref: Preference): MessageToTelegramWithUser? {
        val sendMessage =
            Preferences
                .getUserId(pref)
                .let2(action.text, ::SendMessage)
        return Preferences
            .getToken(pref)
            .mapOption2(sendMessage, ::MessageToTelegramWithUser)
    }

    fun convertNotification(it: StatusBarNotification) =
        Notification(it.packageName, "" + it.notification.tickerText)

    fun filter(notification: Notification) =
        notification
            .takeIf {
                it.packageName == "com.google.android.talk" && it.tickerText != null
            }
            .mapOption { MessageToTelegram(it.tickerText!!) } // FIXME:
}

object Domain {

    fun valid(notificationListeners: String?, packageName: String): Boolean =
        ComponentName(packageName, NotificationListener::class.java.name)
            .flattenToString()
            .let { notificationListeners?.contains(it) }
            ?: false

    fun findPinCode(xs: List<Update>, pinCode: String): Boolean =
        xs.any { it.message().text() == pinCode }

    fun getPinCode(androidId: String): String =
        String.format("%04d", androidId.hashCode()).takeLast(4)
}

fun validateSettings(context: ComponentContext): ValidationResult {
    val enabled = Secure
        .getString(context.contentResolver, "enabled_notification_listeners")
        .let { Domain.valid(it, context.packageName) }

//    val token =
//        context
//            .getSharedPreferences(Preferences.name, 0)
//            .let(Preferences::getToken)

    val user = context
        .getSharedPreferences(Preferences.name, 0)
        .let(Preferences::getUserId)

    return ValidationResult(enabled, user != null)
}

suspend fun validateSettings(pincode: String, token: String): ValidationResult {

    val xs =
        loadNewMessages(token)
            .map { Domain.findPinCode(it, pincode) }

    TODO()
}

fun getToken(ctx: ComponentContext): String =
    ctx.contentResolver
        .let(::getAndroidId)
        .let(Domain::getPinCode)

object Preferences {

    val name = "default"

    fun getToken(p: Preference): String? =
        p.map["token"] as String?

    fun getUserId(p: Preference): String? =
        p.map["user-id"] as String?

    fun getUserId(p: SharedPreferences): String? =
        p.getString("user-id", null)
}