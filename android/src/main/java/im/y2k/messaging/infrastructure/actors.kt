package im.y2k.messaging.infrastructure

import android.service.notification.StatusBarNotification
import im.y2k.messaging.domain.Notifications
import kotlinx.coroutines.experimental.channels.actor

val notificationActor = actor<StatusBarNotification>(capacity = 20) {
    while (true) {
        val receive = receive()
        val pref = App.instance.getPreferences()

        receive
            .let(Notifications::convertNotification)
            .let(Notifications::filter)
            .mapOption2(pref, Notifications::tryCreateNotification)
            .mapOption { Bot.execute(it) }
    }
}