package im.y2k.messaging.utils

import rx.Single
import rx.Single.create
import rx.SingleSubscriber

typealias IO<T> = Single<Pair<Environment, T>>

fun <T> pure(func: () -> T): IO<T> =
    ask().bind { env ->
        IO.create<Pair<Environment, T>> { subscriber ->
            subscriber.onSuccess(env to func())
        }
    }

fun <T> pure(x: T): IO<T> = ask().map { (env, _) -> env to x }

fun <T> ask(f: Environment.() -> IO<T>): IO<T> = ask().bind(f)

fun ask(): IO<Environment> =
    create<Pair<Environment, Environment>> { t ->
        val x = t.findRootSubscription() as EnvSingleSubscriber<*>
        t.onSuccess(x.env to x.env)
    }

fun <T, R> IO<T>.fmap(f: (T) -> R): IO<R> = map { (env, x) -> env to f(x) }
fun <T, R> IO<T>.bind(f: (T) -> IO<R>): IO<R> = flatMap { (_, x) -> f(x) }

fun <T, R> IO<T>.zip(next: IO<R>): IO<Pair<T, R>> =
    bind { t -> next.fmap { r -> t to r } }

fun <T> IO<T>.run(env: Environment) {
    subscribe(EnvSingleSubscriber(env, {}))
}

fun <T> IO<T>.run(env: Environment, callback: (T) -> Unit) {
    subscribe(EnvSingleSubscriber(env, callback))
}

open class EnvSingleSubscriber<T>(
    val env: Environment,
    private val callback: (T) -> Unit) : SingleSubscriber<Pair<Environment, T>>() {

    override fun onSuccess(t: Pair<Environment, T>) = callback(t.second)
    override fun onError(error: Throwable) = throw error
}