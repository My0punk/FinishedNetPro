//Contains the PreferenceTestFactory class (see comments below)
//Created: 10/18/20
package sdns.serialization.test.factories;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything with a preference
 *      16-bit, unsigned integer which specifies the preference given to this RR
 *        among others at the same owner.  Lower values are preferred.
 */
public abstract class PreferenceTestFactory {
    /**
     * Test valid preferences
     * @param pref preference to test
     */
    @ParameterizedTest(name = "Valid Preference = {0}")
    @ValueSource(ints = {0, 1, 65535})
    void testValidPreference(int pref){
        try {
            assertEquals(pref, setGetPreference(pref));
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Test valid preferences
     * @param pref preference to test
     */
    @ParameterizedTest(name = "Invalid Preference = {0}")
    @ValueSource(ints = {-1, -2147483648, -32768, 65536})
    void testInvalidPreference(int pref){
        if(getTestInvalid())
            assertThrows(ValidationException.class, () -> setGetPreference(pref));
    }

    /**
     * Factory method for calling the appropriate function you want to test for preference validity
     * @param pref preference to test
     * @return the result of a getPreference on the respective object
     * @throws ValidationException if invalid object
     */
    protected abstract int setGetPreference(int pref) throws ValidationException;

    /**
     * Gives the subclass the option of whether or not to run invalid tests (if decoding an unsigned int, you can't
     *   have invalid numbers there because they're unsigned and only fit into 2 bytes so...)
     * @return whetehr or not to run the invalid tests
     */
    protected boolean getTestInvalid() { return true; }
}
