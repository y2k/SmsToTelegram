package im.y2k.messaging.domain

import android.content.ComponentName
import android.content.Context
import android.provider.Settings.Secure
import android.service.notification.StatusBarNotification
import com.facebook.litho.ComponentContext
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.GetUpdates
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.GetUpdatesResponse
import im.y2k.messaging.NotificationListener
import im.y2k.messaging.infrastructure.*

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

    fun findUserForPinCode(updates: List<Update>, pinCode: String) =
        updates
            .find { it.message().text() == pinCode }
            .mapOption { it.message().from().id().toString() }

    fun toPinCode(androidId: String): String =
        String.format("%04d", androidId.hashCode()).takeLast(4)

    fun checkPreRequests(pref: Preference, secureValue: String?, packageName: String): ValidationResult {
        val isHasUserId = Preferences.getUserId(pref) != null
        return secureValue
            .let2(packageName, Domain::valid)
            .let2(isHasUserId, ::ValidationResult)
    }

    private fun valid(notificationListeners: String?, packageName: String): Boolean =
        ComponentName(packageName, NotificationListener::class.java.name)
            .flattenToString()
            .let { notificationListeners?.contains(it) }
            ?: false
}

suspend fun trySaveOwnerUserId(token: AccessToken?) {
    token.mapOption { trySaveOwnerUserId_(it) }
}

private suspend fun trySaveOwnerUserId_(token: AccessToken) {
    token
        .value
        .let(Preferences::setToken)
        .let(App.instance::putStringPref)

    val pinCode = App.instance.let(::getPinCode)
    Bot.execute(token.value, GetUpdates())
        .map(GetUpdatesResponse::updates)
        .map2(pinCode, Domain::findUserForPinCode)
        .toOption()
        .mapOption(Preferences::setUserId)
        .mapOption(App.instance::putStringPref)
}

fun validatePrepareForWork(_ignore: ComponentContext?): ValidationResult {
    val app = App.instance
    val secureValue = Secure.getString(
        app.contentResolver, "enabled_notification_listeners")
    val pref = app.getPreferences()
    val packageName = app.packageName

    return Domain.checkPreRequests(pref, secureValue, packageName)
}

fun getPinCode(ctx: Context): String =
    ctx.contentResolver
        .let(::getAndroidId)
        .let(Domain::toPinCode)

object Preferences {

    val name = "default"

    fun getToken(p: Preference): String? =
        p.map["token"] as String?

    fun getUserId(p: Preference): String? =
        p.map["user-id"] as String?

    fun setToken(value: String) =
        "token" to value

    fun setUserId(value: String) =
        "user-id" to value
}