package im.y2k.messaging.utils

import rx.Subscription
import java.util.*
import java.util.concurrent.CountDownLatch

fun <R> ignore0(): R = TODO()
fun <T, R> ignore1(a: T): R = TODO()
fun <T1, T2, R> ignore2(a: T1, b: T2): R = TODO()

fun <T> IO<T>.runSync(env: Environment): T {
    var result: T? = null
    val lock = CountDownLatch(1)
    run(env) {
        result = it
        lock.countDown()
    }
    lock.await()
    return result!!
}

fun Subscription.findRootSubscription(): Subscription {
//    if (type.java.isInstance(this)) return this
//    if (this is EnvSingleSubscriber<*>) return this

    val clazz = javaClass
    fun Subscription.find(name: String): Subscription? {
        val f = clazz.declaredFields.find { it.name == name } ?: return null
        f.isAccessible = true
        return (f.get(this) as Subscription?)?.findRootSubscription()
    }
    return find("actual") ?: find("val\$child") ?: find("val\$onSuccess") ?: this
}

fun randomInts(count: Int, seed: Long = 42): List<Int> {
    val random = Random(seed)
    return (1..count).map { random.nextInt() }
}