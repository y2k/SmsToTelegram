import im.y2k.messaging.utils.randomInts

/**
 * Created by y2k on 13/04/2017.
 **/

fun repeat(count: Int, action: (Int) -> Unit) =
    randomInts(count).forEach(action)