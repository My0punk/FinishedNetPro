//Contains the TTLTestFactory class (see comments below)
//Created: 10/18/20
package sdns.serialization.test.factories;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything with an IPv6
 */
public abstract class TTLTestFactory {
    /**
     * Test valid TTLs
     * @param ttl TTL to test
     */
    @ParameterizedTest(name = "Valid TTL = {0}")
    @ValueSource(ints = {0, 1, 2147483647})
    void testValidTTL(int ttl){
        try {
            assertEquals(ttl, setGetTTL(ttl));
        } catch (ValidationException e) {
            fail();
        }
    }
    /**
     * Test valid TTLs
     * @param ttl TTL to test
     */
    @ParameterizedTest(name = "Invalid TTL = {0}")
    @ValueSource(ints = {-1, -2147483648})
    void testInvalidTTL(int ttl){
        assertThrows(ValidationException.class, () -> setGetTTL(ttl));
    }

    /**
     * Factory method for calling the appropriate function you want to test for TTL validity
     * @param ttl ttl to test
     * @return the result of a getTTL on the respective object
     * @throws ValidationException if invalid object
     */
    protected abstract int setGetTTL(int ttl) throws ValidationException;
}
