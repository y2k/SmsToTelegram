import im.y2k.messaging.utils.*
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by y2k on 08/04/2017.
 **/
class MonadTests {

    @Test fun `bind and fmap`() {
        val id = ask()
            .bind { ctx -> ctx.secureID() }
            .fmap { x -> x.toUpperCase() }
            .runSync(Environment(secureID = { pure("fake secure id") }))

        assertEquals("FAKE SECURE ID", id)
    }
}