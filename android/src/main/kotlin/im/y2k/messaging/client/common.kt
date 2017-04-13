package im.y2k.messaging.client

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings.Secure
import android.view.View
import im.y2k.messaging.domain.TargetAction
import im.y2k.messaging.domain.TargetUrl
import im.y2k.messaging.utils.Environment
import im.y2k.messaging.utils.IO
import im.y2k.messaging.utils.pure
import im.y2k.messaging.utils.run
import org.jetbrains.anko.onClick
import java.io.Serializable

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> Intent.getExtra(key: String): T = getSerializableExtra(key) as T

fun View.onClickIO(f: (Context) -> IO<Unit>) = onClick { f(context).run(env) }

private val HANDLER by lazy { Handler(Looper.getMainLooper()) }

val View.env: Environment get() = context.env()

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Instance = this
    }

    companion object {
        lateinit var Instance: Context
    }
}

fun Context.env() = Environment(
    open = { target ->
        pure {
            when (target) {
                is TargetUrl -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target.url)))
                is TargetAction -> startActivity(Intent(target.action))
            }
            Unit
        }
    },
    runOnMain = { HANDLER.post(it) },
    getPref = { key ->
        pure {
            prefs.getString(key, null)
        }
    },
    setPref = { key, value ->
        pure {
            prefs.edit().putString(key, value).apply()
            Unit
        }
    },
    secureID = {
        pure {
            Secure.getString(contentResolver, Secure.ANDROID_ID)
        }
    }
)

private val Context.prefs: SharedPreferences
    get() = getSharedPreferences("default", 0)