package im.y2k.messaging.infrastructure

import android.service.notification.StatusBarNotification
import im.y2k.messaging.client.App
import im.y2k.messaging.client.Bot_executeAsync
import im.y2k.messaging.client.mapOption
import im.y2k.messaging.client.mapOption2
import im.y2k.messaging.domain.Notifications
import im.y2k.messaging.domain.Preference
import im.y2k.messaging.domain.Preferences
import kotlinx.coroutines.experimental.channels.actor

val notificationActor = actor<StatusBarNotification> {
    while (true) {
        val receive = receive()
        val pref = App.instance
            .getSharedPreferences(Preferences.name, 0).all
            .let(::Preference)

        receive
            .let(Notifications::convertNotification)
            .let(Notifications::filter)
            .mapOption2(pref, Notifications::tryCreateNotification)
            .mapOption { Bot_executeAsync(it) }
    }
}