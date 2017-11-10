package im.y2k.messaging.client

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import im.y2k.messaging.client.MainScreen.Model
import im.y2k.messaging.client.MainScreen.Msg
import im.y2k.messaging.client.MainScreen.Msg.*
import im.y2k.messaging.domain.ValidationResult
import im.y2k.messaging.domain.getPinCode
import im.y2k.messaging.domain.validateSettings
import im.y2k.messaging.infrastructure.notificationActor
import y2k.litho.elmish.*

object MainScreen : ElmFunctions<Model, Msg> {
    data class Model(val pin: String?,
                     val status: ValidationResult?,
                     val accessToken: String?)

    sealed class Msg {
        class PinCodeLoaded(val pin: String) : Msg()
        object OpenSettings : Msg()
        object OpenTelegram : Msg()
        object ValidateMsg : Msg()
        class ValidateResultMsg(val status: ValidationResult) : Msg()
        class AccessTokenMsg(val token: String) : Msg()
    }

    override fun init() =
        Model(null, null, null) to Cmd.fromContext(::getPinCode, ::PinCodeLoaded)

    override fun update(model: Model, msg: Msg) = when (msg) {
        is PinCodeLoaded -> model.copy(pin = msg.pin) to Cmd.none<Msg>()
        OpenSettings -> model to Cmd.fromContext(::openSettings)
        OpenTelegram -> model to Cmd.fromContext(::openTelegram)
        ValidateMsg -> model to Cmd.fromContext(::validateSettings, ::ValidateResultMsg)
        is ValidateResultMsg -> model.copy(status = msg.status) to Cmd.none()
        is AccessTokenMsg -> model.copy(accessToken = msg.token) to Cmd.none()
    }

    override fun view(model: Model) =
        column {
            children(
                head("1) Доступ к нотификациям"),
                button("Настройки", OpenSettings),

                head("2) Создать бота и ввести ему токен"),
                text {
                    text("Токен")
                    textSizeSp(16f)
                },
                editText {
                    editable(false)
                    text(model.pin)
                    textSizeSp(16f)
                },
                button("Открыть telegram", OpenTelegram),

                head("3) Протестировать настройки"),
                editText {
                    hint("Access Token")
                    textSizeSp(16f)
                    onTextChanged(::AccessTokenMsg)
                },
                button("OK", ValidateMsg),
                text {
                    text(model.status.format())
                    textColor(Color.RED)
                    textSizeSp(16f)
                })
        }

    private fun head(title: String) =
        text {
            text(title)
            textSizeSp(18f)
        }

    private fun button(title: String, msg: Msg) =
        column {
            child(text { l ->
                text(title)
                textSizeSp(18f)
                onClick(l, msg)
            })
            backgroundRes(android.R.drawable.btn_default)
        }

    private fun ValidationResult?.format() =
        this?.let { "Настройки: ${it.settings}\nТелеграм: ${it.telegram}" } ?: ""
}

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program(MainScreen)
    }
}

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        notificationActor.offer(sbn)
    }
}