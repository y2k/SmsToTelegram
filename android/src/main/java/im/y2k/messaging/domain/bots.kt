package im.y2k.messaging.domain

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramBotAdapter
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.request.GetUpdates
import com.pengrad.telegrambot.request.SendMessage
import im.y2k.messaging.client.Result
import im.y2k.messaging.client.executeAsync
import im.y2k.messaging.client.map
import im.y2k.messaging.domain.Domain.getPinCode
import im.y2k.messaging.domain.Domain.handleMessage
import kotlinx.coroutines.experimental.channels.actor

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

suspend fun loadNewMessages(): Result<List<Update>, Exception> =
    TelegramBot("TODO") // FIXME
        .executeAsync(GetUpdates())
        .map { it.updates() }

val botActor = actor<TelegramMsg> {
    val startMsg = receive()
    val bot = TelegramBot(startMsg.token)
    bot.executeAsync(SendMessage(startMsg.id, startMsg.text))

    while (true) {
        val msg = receive()
        bot.executeAsync(SendMessage(msg.id, msg.text))
    }
}

class Bot {

    companion object {

        suspend fun getNewMessages(token: String, delayMs: Int): List<Message> {
            TODO()

//            val env = getEnvironment()
//            return suspendCoroutine { callback ->
//                val bot = openBot(token)
//                bot.setUpdatesListener({ updates ->
//                    val messages = updates.map {
//                        Message(
//                            "" + it.message().from().id(),
//                            it.message().text())
//                    }
//                    callback.resume(messages)
//
//                    env.runOnMain { bot.removeGetUpdatesListener() }
//                    UpdatesListener.CONFIRMED_UPDATES_ALL
//                }, GetUpdates().timeout(delayMs / 1000))
//            }
        }

        class Environment {
            fun setPref(key: String, value: String): Unit = TODO()
        }

        @Deprecated("")
        suspend fun waitToConnect(token: String) {
            TODO()

//            val env = getEnvironment()
//            suspendCoroutine<Unit> { callback ->
//                openBot(token).apply {
//                    setUpdatesListener { updates ->
//                        updates.forEach {
//                            env.setPref(
//                                it.message().from().username(),
//                                "" + it.message().chat().id())
//                        }
//
//                        env.runOnMain {
//                            callback.resume(Unit)
//                            removeGetUpdatesListener()
//                        }
//                        UpdatesListener.CONFIRMED_UPDATES_ALL
//                    }
//                }
//            }
        }

        private fun getEnvironment() = Environment()
    }
}

private fun openBot(token: String) = TelegramBotAdapter.build(token)