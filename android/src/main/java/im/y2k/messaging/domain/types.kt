package im.y2k.messaging.domain

import java.io.Serializable

class Notification(
    val packageName: String,
    val tickerText: String?
) : Serializable

class NotificationWithToken(
    val notification: Notification,
    val id: String,
    val token: String)

sealed class Target
class TargetUrl(val url: String) : Target()
class TargetAction(val action: String) : Target()

class Message(val userId: String, val message: String)