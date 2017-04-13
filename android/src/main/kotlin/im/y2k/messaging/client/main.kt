package im.y2k.messaging.client

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.LinearLayout.HORIZONTAL
import im.y2k.messaging.domain.*
import im.y2k.messaging.infrastructure.ActiveBot
import im.y2k.messaging.utils.run
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scrollView {
            verticalLayout {
                header(1, "Создать бота")
                componentCreateBot()

                header(2, "Введите токен")
                componentBotToken()

                header(3, "Доступ к нотификациям")
                openSettingsButton()

                header(4, "Пинкод для доступа к боту")
                textView {
                    gravity = Gravity.CENTER
                    textSize = 48f
                    textColor = getColor(R.color.colorPrimary)
                    typeface = Typeface.DEFAULT_BOLD

                    getToken().run(env) { text = it }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ActiveBot.start()
    }

    override fun onStop() {
        super.onStop()
        ActiveBot.stop()
    }
}

private fun _LinearLayout.header(index: Int, title: String) {
    linearLayout {
        padding = dip(8)
        dividerPadding = dip(8)
        gravity = Gravity.CENTER_VERTICAL

        textView("$index") {
            gravity = Gravity.CENTER
            textColor = Color.WHITE
            backgroundResource = R.drawable.round
        }
        textView(title) {
            leftPadding = dip(8)
            textSize = 18f
            textColor = Color.BLACK
        }
    }
}

private fun _LinearLayout.componentBotToken() {
    linearLayout {
        orientation = HORIZONTAL
        editText {
            lparams { weight = 1f }
            hint = "Токен"
            loadCurrentBotToken().run(env) {
                setText(it)
            }
            textChangedListener {
                afterTextChanged {
                    saveBotToken("" + it).run(env)
                }
            }
        }
        button("OK") {
            textSize = 24f
            lparams { weight = 0f }
            onClick { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Domain.getHelpPage()))) }
        }
    }
}

private fun _LinearLayout.componentCreateBot() {
    linearLayout {
        orientation = HORIZONTAL
        button("Открыть телеграм") {
            lparams { weight = 1f }
            textSize = 24f
            onClickIO { openCreateBot() }
        }
        button("?") {
            lparams { weight = 0f }
            textSize = 24f
            onClick { (context as Activity).recreate() }
        }
    }
}

private fun _LinearLayout.openSettingsButton() {
    button("Настройки") {
        textSize = 24f
        onClickIO { openNotificationSettings() }
    }
}