package im.y2k.messaging.infrastructure

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramBotAdapter
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.request.GetUpdates
import com.pengrad.telegrambot.request.SendMessage
import im.y2k.messaging.domain.Domain.getPinCode
import im.y2k.messaging.domain.Domain.handleMessage
import im.y2k.messaging.domain.Message
import im.y2k.messaging.utils.*

object UpdateReceiver {

    private var count = 0
    private var bot: TelegramBot? = null

    fun reset() {
        if (count <= 0) return

        bot!!.removeGetUpdatesListener()
        bot = TelegramBotAdapter.build(getToken())
        setBotUpdates()
    }

    fun start() {
        if (count == 0) {
            bot = TelegramBotAdapter.build(getToken())
            setBotUpdates()
        }
        count++
    }

    private fun setBotUpdates() {
        val bot_ = bot!!
        bot_.setUpdatesListener { updates ->
            updates
                .map {
                    it.message().from().id() to
                        handleMessage(it.message().text(), getPinCode())
                }
                .forEach { (id, msg) ->
                    bot_.execute(SendMessage(id, msg))
                }
            UpdatesListener.CONFIRMED_UPDATES_ALL
        }
    }

    fun stop() {
        count--
        if (count == 0)
            bot!!.removeGetUpdatesListener()
    }

    private fun getPinCode(): String = getPinCode(getAndroidSecureId())
    private fun getAndroidSecureId(): String = TODO()
    private fun getToken(): String = TODO()
}

class Bot {

    companion object {

        fun getNewMessages(token: String, delayMs: Int): IO<List<Message>> =
            ask().bind { env ->
                IO.create<Pair<Environment, List<Message>>> { callback ->
                    val bot = openBot(token)
                    bot.setUpdatesListener({ updates ->

                        val messages = updates.map {
                            Message(
                                "" + it.message().from().id(),
                                it.message().text())
                        }
                        callback.onSuccess(env to messages)

                        env.runOnMain { bot.removeGetUpdatesListener() }
                        UpdatesListener.CONFIRMED_UPDATES_ALL
                    }, GetUpdates().timeout(delayMs / 1000))
                }
            }

        @Deprecated("")
        fun waitToConnect(token: String): IO<Unit> =
            ask().bind { env ->
                IO.create<Pair<Environment, Unit>> { callback ->
                    openBot(token).apply {
                        setUpdatesListener { updates ->
                            updates.forEach {
                                env.setPref(
                                    it.message().from().username(),
                                    "" + it.message().chat().id())
                            }

                            env.runOnMain {
                                callback.onSuccess(env to Unit)
                                removeGetUpdatesListener()
                            }
                            UpdatesListener.CONFIRMED_UPDATES_ALL
                        }
                    }
                }
            }

        fun sendMessage(token: String, id: Int, message: String): IO<Unit> = pure {
            openBot(token).execute(SendMessage(id, message))
            Unit
        }
    }
}

private fun openBot(token: String) = TelegramBotAdapter.build(token)