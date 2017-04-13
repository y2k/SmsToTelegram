import im.y2k.messaging.domain.Domain.getPinCode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by y2k on 12/04/2017.
 **/
class DomainTests {

    @Test fun `test get pincode`() = repeat(1_000) {
        val pinCode = getPinCode("$it")
        assertEquals(pinCode, 4, pinCode.length)
    }
}