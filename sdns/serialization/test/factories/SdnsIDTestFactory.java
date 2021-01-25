//Contains the SdnsIDTestFactory class (see comments below)
//Created: 10/18/20
package sdns.serialization.test.factories;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything with an SDNS ID (16 bit unsigned int)
 */
public abstract class SdnsIDTestFactory {
    /**
     * Valid SDNS ID tests (16 bit unsigned int)
     * @param id id to test
     */
    @ParameterizedTest(name = "Valid ID = {0}")
    @ValueSource(ints = {0, 1, 65535, 65280, 32768, 34952, 34824})
    void testValidIDs(int id){
        try {
            assertEquals(id, this.setGetID(id));
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Invalid SDNS ID tests (16 bit unsigned int)
     * @param id id to test
     */
    @ParameterizedTest(name = "Invalid ID = {0}")
    @ValueSource(ints = {65536, -1, -2147418113})
    void testInvalidIDs(int id){
        assertThrows(ValidationException.class, () -> this.setGetID(id));
    }

    /**
     * Factory method for calling the appropriate function you want to test for SDNS ID validity
     * @param id id to test
     * @return the result of a getID on the respective object
     * @throws ValidationException if invalid SDNS ID
     */
    protected abstract int setGetID(int id) throws ValidationException;
}
