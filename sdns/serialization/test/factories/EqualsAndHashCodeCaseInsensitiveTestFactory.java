//Contains the EqualsAndHashCodeCaseInsensitiveTestFactory class (see comments below)
//Created: 10/18/20
package sdns.serialization.test.factories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * Makes an abstract class for testing anything with an Equals in a case insensitive manner (so every class)
 * Guarantees:
 *             0 and 1 are the same but have a case difference
 *             0 and 2 are the same but have a case difference
 */
public abstract class EqualsAndHashCodeCaseInsensitiveTestFactory<T> extends EqualsAndHashCodeTestFactory<T> {
    /**
     * Tests for equality with 0 and 1
     */
    @Test @DisplayName("Equals/hashcode ignore case 0 and 1")
    void testEquals0(){
        try{
            assertEquals(getDefaultObject0(), getDefaultObjectDifferentCase1());
            assertEquals(getDefaultObject0().hashCode(), getDefaultObjectDifferentCase1().hashCode());
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Tests for equality with 0 and 2
     */
    @Test @DisplayName("Equals/hashcode ignore case 0 and 2")
    void testEquals1(){
        try{
            assertEquals(getDefaultObject0(), getDefaultObjectDifferentCase2());
            assertEquals(getDefaultObject0().hashCode(), getDefaultObjectDifferentCase2().hashCode());
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Factory method for generating the first same object as getDefaultObject0 but with a different case
     *  to test for ignore case equality
     * @return the default object for this class
     * @throws ValidationException if invalid object
     */
    protected abstract T getDefaultObjectDifferentCase1() throws ValidationException;
    /**
     * Factory method for generating the second same object as getDefaultObject0 but with a different case
     *  to test for ignore case equality
     * Defaults to getDefaultObjectDifferentCase1 (if only 1 field to test)
     * @return the default object for this class
     * @throws ValidationException if invalid object
     */
    protected T getDefaultObjectDifferentCase2() throws ValidationException {
        return getDefaultObjectDifferentCase1();
    }
}
