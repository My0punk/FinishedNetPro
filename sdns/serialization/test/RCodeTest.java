//Contains the RCodeTest class (see comments below)
//Created: 10/18/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.RCode;
import sdns.serialization.ValidationException;
import sdns.serialization.test.factories.RCodeSetGetAbstractTestFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
public class RCodeTest {
    /**
     * Test getRCode
     */
    @Nested
    class RCodeGetRCodeSetGet extends RCodeSetGetAbstractTestFactory {
        /**
         * Factory method for calling the appropriate function you want to test for rcode validity
         *
         * The valid tests are redundant for this, but the abstract functionality is still useful for invalid tests.
         * @param rcode rcode to call with
         * @return the result of a getRCode on the respective object
         * @throws ValidationException if invalid rcode
         */
        @Override
        protected RCode callSetGetRCode(int rcode) throws ValidationException {
            return RCode.getRCode(rcode);
        }
    }

    //Test getRCodeValue is set correctly
    @Test @DisplayName("Test getRCodeValue")
    void testGetRCodeValue(){
        assertAll("Each rcode value is correct",
                () -> assertEquals(0, RCode.NOERROR.getRCodeValue()),
                () -> assertEquals(1, RCode.FORMATERROR.getRCodeValue()),
                () -> assertEquals(2, RCode.SERVERFAILURE.getRCodeValue()),
                () -> assertEquals(3, RCode.NAMEERROR.getRCodeValue()),
                () -> assertEquals(4, RCode.NOTIMPLEMENTED.getRCodeValue()),
                () -> assertEquals(5, RCode.REFUSED.getRCodeValue()));
    }

    //Test getRCodeMessage contains some semblance of a message relating to the name (according to the specifications)
    @Test @DisplayName("Test getRCodeMessage returns an appropriate message")
    void testGetRCodeMessage(){
        assertAll("Each rcode value is correct",
            () -> assertEquals(RCode.NOERROR.getRCodeMessage(), "No error condition"),
            () -> assertEquals(RCode.FORMATERROR.getRCodeMessage(), "The name server was unable to interpret the query"),
            () -> assertEquals("The name server was unable to process this query " +
                    "due to a problem with the name server", RCode.SERVERFAILURE.getRCodeMessage()),
            () -> assertEquals("The domain name referenced in the query does not exist", RCode.NAMEERROR.getRCodeMessage()),
            () -> assertEquals(RCode.NOTIMPLEMENTED.getRCodeMessage(), "The name server does not support the " +
                    "requested kind of query"),
            () -> assertEquals(RCode.REFUSED.getRCodeMessage(), "The name server refuses to perform the " +
                    "specified operation"));
    }
}
