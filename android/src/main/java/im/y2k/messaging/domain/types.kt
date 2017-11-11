package im.y2k.messaging.domain

import com.pengrad.telegrambot.request.SendMessage
import java.io.Serializable

class Notification(
    val packageName: String,
    val tickerText: String?
) : Serializable

class ValidationResult(val settings: Boolean, val telegram: Boolean)

class Preference(val map: Map<String, *>)

class MessageToTelegramWithUser(val token: String, val msg: SendMessage)
class MessageToTelegram(val text: String)

class AccessToken(val value: String)
class PinCode(val value: String)