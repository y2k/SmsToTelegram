package im.y2k.messaging.client

import android.app.IntentService
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import im.y2k.messaging.domain.Notification
import im.y2k.messaging.domain.handleNotification
import im.y2k.messaging.utils.runSync

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Intent(this, WorkService::class.java)
            .putExtra(KEY, convertNotification(sbn))
            .let { startService(it) }
    }

    private fun convertNotification(sbn: StatusBarNotification) =
        Notification(sbn.packageName, "" + sbn.notification.tickerText)

    class WorkService : IntentService("work-service") {

        override fun onHandleIntent(intent: Intent) =
            intent
                .getExtra<Notification>(KEY)
                .let(::handleNotification)
                .runSync(env())
    }

    companion object {
        private val KEY = "sbn"
    }
}