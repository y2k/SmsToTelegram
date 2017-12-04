package im.y2k.messaging

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.facebook.litho.ComponentLayout.ContainerBuilder
import com.facebook.yoga.YogaEdge
import im.y2k.messaging.MainScreen.Model
import im.y2k.messaging.MainScreen.Msg
import im.y2k.messaging.MainScreen.Msg.*
import im.y2k.messaging.client.R
import im.y2k.messaging.domain.*
import im.y2k.messaging.infrastructure.notificationActor
import im.y2k.messaging.infrastructure.openSettings
import im.y2k.messaging.infrastructure.openTelegram
import y2k.litho.elmish.experimental.*
import y2k.litho.elmish.experimental.Views.column

object MainScreen : ElmFunctions<Model, Msg> {
    data class Model(val pin: PinCode?,
                     val status: ValidationResult?,
                     val token: AccessToken?)

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
        is PinCodeLoaded -> model.copy(pin = PinCode(msg.pin)) to Cmd.none<Msg>()
        OpenSettings -> model to Cmd.fromContext(::openSettings)
        OpenTelegram -> model to Cmd.fromContext(::openTelegram)
        ValidateMsg -> model.copy(status = null) to
            Cmd.batch(
                Cmd.fromSuspend { trySaveOwnerUserId(model.token) },
                Cmd.fromContext(::validatePrepareForWork, ::ValidateResultMsg))
        is ValidateResultMsg -> model.copy(status = msg.status) to Cmd.none()
        is AccessTokenMsg -> model.copy(token = AccessToken(msg.token)) to Cmd.none()
    }

    override fun view(model: Model) =
        column {
            paddingDip(YogaEdge.ALL, 4f)

            head("1) Доступ к нотификациям")
            button("Настройки", OpenSettings)

            head("2) Создать бота и ввести ему токен")
            text {
                text("Токен")
                textSizeSp(18f)
            }
            editText {
                editable(false)
                text(model.pin?.value)
                textSizeSp(18f)
            }
            button("Открыть telegram", OpenTelegram)

            head("3) Авторизовать бота")
            editText {
                hint("Access Token")
                textSizeSp(18f)

                onTextChanged(::AccessTokenMsg)
            }

            button("OK", ValidateMsg)
            text {
                text(model.status.format())
                textColor(Color.RED)
                textSizeSp(18f)
            }
        }

    private fun ContainerBuilder.head(title: String) =
        text {
            text(title)
            textSizeSp(20f)
        }

    private fun ContainerBuilder.button(title: String, msg: Msg) =
        column {
            backgroundRes(R.drawable.button_background)
            paddingDip(YogaEdge.ALL, 6f)

            onClick(msg)

            text {
                text(title)
                textColor(Color.WHITE)
                textSizeSp(18f)
            }
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