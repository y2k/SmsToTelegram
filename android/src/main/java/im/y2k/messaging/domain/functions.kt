package im.y2k.messaging.domain

import android.content.ComponentName
import android.provider.Settings.Secure
import android.service.notification.StatusBarNotification
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.GetUpdates
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.GetUpdatesResponse
import im.y2k.messaging.NotificationListener
import im.y2k.messaging.infrastructure.*

object Notifications {

    fun tryCreateNotification(action: MessageToTelegram, pref: Preference): MessageToTelegramWithUser? {
        val sendMessage = Preferences
            .getUserId(pref)
            .let(flip(::SendMessage), action.text)
        return Preferences
            .getToken(pref)
            .mapOption2(sendMessage, ::MessageToTelegramWithUser)
    }

    fun convertNotification(it: StatusBarNotification) =
        Notification(it.packageName, "" + it.notification.tickerText)

    fun filter(notification: Notification) =
        notification
            .takeIf { it.packageName == "com.google.android.talk" && it.tickerText != null }
            .mapOption { MessageToTelegram(it.tickerText!!) } // FIXME:
}

object Domain {

    fun findUserForPinCode(updates: List<Update>, pinCode: String): Long? =
        updates
            .find { it.message().text() == pinCode }
            .mapOption { it.message().from().id().toLong() }

    fun toPinCode(androidId: String): String =
        String.format("%04d", androidId.hashCode()).takeLast(4)

    fun checkPreRequests(pref: Preference, secureValue: String?, packageName: String): ValidationResult {
        val isHasUserId = Preferences.getUserId(pref) != null
        return secureValue
            .let(::validate, packageName)
            .let(::ValidationResult, isHasUserId)
    }

    private fun validate(packageName: String, notificationListeners: String?): Boolean =
        ComponentName(packageName, NotificationListener::class.java.name)
            .flattenToString()
            .let { notificationListeners?.contains(it) ?: false }
}

suspend fun trySaveOwnerUserId(token: AccessToken?) {
    token!!
        .value
        .let(Preferences::setToken)
        .let(App.instance::putStringPref)

    Bot.execute(token.value, GetUpdates())
        .map(GetUpdatesResponse::updates)
        .map2(getPinCode(), Domain::findUserForPinCode)
        .toOption()
        .mapOption(Preferences::setUserId)
        .mapOption(App.instance::putStringPref)
}

suspend fun validatePrepareForWork(): ValidationResult {
    val pref = App.instance.getPreferences()
    val secureValue = Secure.getString(
        App.instance.contentResolver, "enabled_notification_listeners")
    val packageName = App.instance.packageName

    return Domain.checkPreRequests(pref, secureValue, packageName)
}

suspend fun getPinCode(): String =
    App.instance
        .contentResolver
        .let(::getAndroidId)
        .let(Domain::toPinCode)

object Preferences {

    val name = "default"

    fun getToken(p: Preference): String? =
        p.map["token"] as String?

    fun getUserId(p: Preference): Long? =
        (p.map["user-id"] as String?)?.toLong()

    fun setToken(value: String) =
        "token" to value

    fun setUserId(value: Long) =
        "user-id" to value
}