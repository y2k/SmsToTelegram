package im.y2k.messaging.infrastructure

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.facebook.soloader.SoLoader
import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.BaseRequest
import com.pengrad.telegrambot.request.GetUpdates
import com.pengrad.telegrambot.response.BaseResponse
import im.y2k.messaging.domain.MessageToTelegramWithUser
import im.y2k.messaging.domain.Preference
import im.y2k.messaging.domain.Preferences
import kotlinx.types.Result
import kotlinx.types.Result.Error
import kotlinx.types.Result.Ok
import java.io.IOException
import kotlin.coroutines.experimental.suspendCoroutine

object Log {

    fun <T> log(e: Exception, x: T): T {
        e.printStackTrace()
        return x
    }
}

inline fun <T, E, R> Result<T, E>.map(f: (T) -> R): Result<R, E> = when (this) {
    is Ok -> Ok(f(value))
    is Error -> this
}

fun <T> Result<T, *>.toOption(): T? = when (this) {
    is Ok -> this.value
    is Error -> null
}

inline fun <T1, T2, E, R> Result<T1, E>.map2(x: T2, f: (T1, T2) -> R): Result<R, E> = when (this) {
    is Ok -> Ok(f(value, x))
    is Error -> this
}

inline fun <T : Any, R : Any> T?.mapOption(f: (T) -> R): R? =
    if (this != null) f(this) else null

inline fun <T1 : Any, T2 : Any, R : Any> T1?.mapOption2(x: T2?, f: (T1, T2) -> R?): R? =
    if (this != null && x != null) f(this, x) else null

inline fun <P1, P2, R> flip(crossinline f: (P1, P2) -> R): ((P2, P1) -> R) =
    { p2, p1 -> f(p1, p2) }

inline fun <T1, T2, R> T2.let(f: (T1, T2) -> R, x: T1): R = f(x, this)

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

fun Context.getPreferences(): Preference =
    getSharedPreferences(Preferences.name, 0).all
        .let(::Preference)

fun Application.putStringPref(xy: Pair<String, Any>) {
    getSharedPreferences(Preferences.name, 0)
        .edit()
        .putString(xy.first, xy.second.toString())
        .apply()
}

fun getAndroidId(resolver: ContentResolver): String =
    Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID)

object Navigation {

    suspend fun openTelegram(ctx: Context) =
        "https://telegram.me/BotFather"
            .let(Uri::parse)
            .let(::makeIntentView)
            .let(ctx::startActivity)

    private fun makeIntentView(uri: Uri) =
        Intent(Intent.ACTION_VIEW, uri)

    suspend fun openSettings(ctx: Context) =
        "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
            .let(::Intent)
            .let(ctx::startActivity)
}

object Bot {

    suspend fun execute(token: String, request: GetUpdates) =
        TelegramBot(token).executeAsync(request)

    suspend fun execute(x: MessageToTelegramWithUser) =
        TelegramBot(x.token).executeAsync(x.msg)
}

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        _instance = this
        SoLoader.init(this, false)
    }

    companion object {
        private lateinit var _instance: Application
        val instance: Application get() = _instance
    }
}