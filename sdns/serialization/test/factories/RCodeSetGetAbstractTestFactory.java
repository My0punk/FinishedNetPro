//Contains the RCodeSetGetAbstractTest class (see comments below)
//Created: 10/18/20
package sdns.serialization.test.factories;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.RCode;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything with an rcode
 */
public abstract class RCodeSetGetAbstractTestFactory {
    //invalid tests
    @ParameterizedTest(name = "Test invalid rcode = {0}")
    @ValueSource(ints = {-1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 2147483647, -2147483648, -64})
    void testGetRCode(int code){
        assertThrows(ValidationException.class, () -> RCode.getRCode(code));
    }

    //Valid tests
    @ParameterizedTest(name = "Valid response code = {0}")
    @ValueSource(ints = {0, 1 ,2, 3, 4, 5})
    void validResponseCode(int code){
        try {
            assertEquals(RCode.getRCode(code), this.callSetGetRCode(code));
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Factory method for calling the appropriate function you want to test for rcode validity
     * @param rcode rcode to call with
     * @return the result of a getRCode on the respective object
     * @throws ValidationException if invalid rcode
     */
    protected abstract RCode callSetGetRCode(int rcode) throws ValidationException;
}