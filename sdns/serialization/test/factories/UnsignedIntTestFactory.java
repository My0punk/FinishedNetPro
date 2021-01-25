//Contains the UnsignedIntTestFactory class (see comments below)
//Created: 11/6/20
package sdns.serialization.test.factories;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything with a preference
 *      32-bit, unsigned integer.
 */
public abstract class UnsignedIntTestFactory {
    /**
     * Test valid unsigned ints
     * @param x unsigned int to test
     */
    @ParameterizedTest(name = "Valid Unsigned Ints = {0}")
    @ValueSource(longs = {0, 1, 65535, 65536, 4294967295L})
    void testValidUnsignedShort(long x){
        try {
            assertEquals(x, setGetUnsignedInt(x));
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Test valid unsigned ints
     * @param x unsigned int to test
     */
    @ParameterizedTest(name = "Invalid Unsigned Ints = {0}")
    @ValueSource(longs = {-1, -2147483648, -32768, -4294967296L, 4294967296L})
    void testInvalidUnsignedInt(long x){
        if(getTestInvalid())
            assertThrows(ValidationException.class, () -> setGetUnsignedInt(x));
    }

    /**
     * Factory method for calling the appropriate function you want to test for unsigned ints validity
     * @param x unsigned int to test
     * @return the result of a getPreference on the respective object
     * @throws ValidationException if invalid object
     */
    protected abstract long setGetUnsignedInt(long x) throws ValidationException;

    /**
     * Gives the subclass the option of whether or not to run invalid tests (if decoding an unsigned int, you can't
     *   have invalid numbers there because they're unsigned and only fit into 4 bytes so...)
     * @return whether or not to run the invalid tests
     */
    protected boolean getTestInvalid() { return true; }
}
