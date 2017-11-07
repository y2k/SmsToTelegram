package im.y2k.messaging.client

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.Secure.ANDROID_ID
import android.provider.Settings.Secure.getString
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentLayout
import com.facebook.litho.ComponentLayout.ContainerBuilder
import im.y2k.messaging.client.MainScreen.Model
import im.y2k.messaging.client.MainScreen.Msg
import im.y2k.messaging.client.MainScreen.Msg.*
import im.y2k.messaging.domain.Domain
import y2k.litho.elmish.*

fun openTelegram(ctx: ComponentContext) =
    "https://telegram.me/BotFather"
        .let(Uri::parse)
        .let(::makeIntentView)
        .let(ctx::startActivity)

private fun makeIntentView(uri: Uri) =
    Intent(Intent.ACTION_VIEW, uri)

fun openSettings(ctx: ComponentContext) =
    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
        .let(::Intent)
        .let(ctx::startActivity)

fun getToken(ctx: ComponentContext): String =
    ctx.contentResolver
        .let(::getAndroidId)
        .let(Domain::getPinCode)

private fun getAndroidId(resolver: ContentResolver): String =
    getString(resolver, ANDROID_ID)

fun <T, R> Cmd_fromContext(f: ComponentContext.() -> R, fOk: (R) -> T): Cmd<T> =
    object : Cmd<T> {
        suspend override fun handle(ctx: ComponentContext): T? = fOk(ctx.f())
    }

fun <T> Cmd_fromContext(f: ComponentContext.() -> Unit): Cmd<T> =
    object : Cmd<T> {
        suspend override fun handle(ctx: ComponentContext): T? {
            ctx.f()
            return null
        }
    }

fun ContainerBuilder.children(vararg xs: Contextual<ComponentLayout.Builder>) =
    xs.forEach(this::child)

object MainScreen : ElmFunctions<Model, Msg> {
    data class Model(val token: String?)
    sealed class Msg {
        class TokenLoaded(val token: String) : Msg()
        object OpenSettings : Msg()
        object OpenTelegram : Msg()
    }

    override fun init() =
        Model(null) to Cmd_fromContext(::getToken, ::TokenLoaded)

    override fun update(model: Model, msg: Msg) = when (msg) {
        is TokenLoaded -> model.copy(token = msg.token) to Cmd.none<Msg>()
        OpenSettings -> model to Cmd_fromContext(::openSettings)
        OpenTelegram -> model to Cmd_fromContext(::openTelegram)
    }

    override fun view(model: Model) =
        column {
            children(
                text {
                    text("1) Доступ к нотификациям")
                    textSizeSp(18f)
                },
                column {
                    child(text { l ->
                        text("Настройки")
                        textSizeSp(18f)
                        onClick(l, OpenSettings)
                    })
                    backgroundRes(android.R.drawable.btn_default)
                },
                text {
                    text("2) Создать бота")
                    textSizeSp(18f)
                },
                column {
                    child(text { l ->
                        text("Открыть telegram")
                        textSizeSp(18f)
                        onClick(l, OpenTelegram)
                    })
                    backgroundRes(android.R.drawable.btn_default)
                },
                text {
                    text("3) Авторизировать бота")
                    textSizeSp(18f)
                },
                text {
                    text("Токен")
                    textSizeSp(16f)
                },
                editText {
                    editable(false)
                    text(model.token)
                    textSizeSp(16f)
                })
        }
}

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        program(MainScreen)
    }
}

//class MainActivity : AppCompatActivity() {
//
//    override fun onStart() {
//        super.onStart()
//        UpdateReceiver.start()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        UpdateReceiver.stop()
//    }
//}