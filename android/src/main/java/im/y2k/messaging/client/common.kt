package im.y2k.messaging.client

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.facebook.litho.ComponentContext
import com.facebook.soloader.SoLoader
import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.response.BaseResponse
import java.io.IOException
import java.io.Serializable
import kotlin.coroutines.experimental.suspendCoroutine

sealed class Result<out T, out E>
class Ok<out T>(val value: T) : Result<T, Nothing>()
class Error<out E>(val error: E) : Result<Nothing, E>()

fun <T, E, R> Result<T, E>.map(f: (T) -> R): Result<R, E> = when (this) {
    is Ok -> Ok(f(value))
    is Error -> this
}

suspend fun <T : BaseRequest<T, R>, R : BaseResponse> TelegramBot.executeAsync(request: T): Result<R, Exception> =
    suspendCoroutine {
        execute(request, object : Callback<T, R> {
            override fun onResponse(request: T, response: R) {
                it.resume(Ok(response))
            }

            override fun onFailure(request: T, e: IOException) {
                it.resume(Error(e))
            }
        })
    }

fun getAndroidId(resolver: ContentResolver): String =
    Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID)

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

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> Intent.getExtra(key: String): T = getSerializableExtra(key) as T

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Instance = this
        SoLoader.init(this, false)
    }

    companion object {
        lateinit var Instance: Context
    }
}