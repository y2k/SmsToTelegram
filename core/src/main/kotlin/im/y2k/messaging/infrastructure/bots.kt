package im.y2k.messaging.infrastructure

import com.pengrad.telegrambot.TelegramBotAdapter
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.request.GetUpdates
import com.pengrad.telegrambot.request.SendMessage
import im.y2k.messaging.domain.Message
import im.y2k.messaging.utils.*

object ActiveBot {

    fun start() {
        // TODO
    }

    fun stop() {
        // TODO
    }
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