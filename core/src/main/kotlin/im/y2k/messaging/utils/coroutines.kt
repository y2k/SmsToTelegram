package im.y2k.messaging.utils

import rx.Single
import kotlin.coroutines.experimental.*

/**
 * Created by y2k on 08/04/2017.
 **/

fun <T> async(block: suspend AsyncBuilder.() -> T): IO<T> =
    ask().flatMap { envPair ->
        Single.create<Pair<Environment, T>> { subscriber ->
            block.startCoroutine(AsyncBuilder(envPair.first), object : Continuation<T> {

                override fun resume(value: T) = subscriber.onSuccess(envPair.first to value)
                override fun resumeWithException(exception: Throwable) = subscriber.onError(exception)
                override val context: CoroutineContext get() = EmptyCoroutineContext
            })
        }
    }

class AsyncBuilder(val env: Environment) {

    suspend fun <T> IO<T>.await(): T =
        suspendCoroutine { continuation ->
            subscribe(object : EnvSingleSubscriber<T>(env, {}) {

                override fun onSuccess(t: Pair<Environment, T>) = continuation.resume(t.second)
            })
        }

}