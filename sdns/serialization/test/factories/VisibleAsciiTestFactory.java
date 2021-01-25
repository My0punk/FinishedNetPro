//Contains the VisibleAsciiTestFactory class (see comments below)
//Created: 10/25/20
package sdns.serialization.test.factories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything with a visible ascii
 */
public abstract class VisibleAsciiTestFactory {
    //Min and max ascii text encodings
    private static final byte ASCII_VISIBLE_MIN = 0x21;
    private static final byte ASCII_VISIBLE_MAX = 0x7E;

    /**
     * Valid visible name tests
     *
     * @param name visible name to test
     */
    @ParameterizedTest(name = "Visible name = {0}")
    @ValueSource(strings = {"", ".", "foo.com.", "asdf", "asdf.].", "asdf..", "www.baylor.edu/", "..",
            "asdf.asdf", "f0-9.c0m-.", "-a.f", "-.", "-",
            "foo.", "foo.com.", "f0-9.c0m.", "google.com.", "www.baylor.edu.", "f0.c-0.", ".", "f-0.", "f-0a.",
            "asdf;lkj!@#$%^&*()_+~`1234567890-=qwertyuiop[]\\QWERTYUIOP{}|asdfghjkl;'ASDFGHJKL:\"zxcvbnm,./ZXCVBNM<>?",
            "a234567890123456789012345678901234567890123456789012345678901234.",//64
            "a23456789012345678901234567890123456789012345678901234567890.a23456789012345678901234567890123456789012345678901234567890." +//122
                "a23456789012345678901234567890123456789012345678901234567890.a23456789012345678901234567890123456789012345678901234567890." +//244
                "a2345678901.",//256
            "a23456789012345678901234567890123456789012345678901234567890.a23456789012345678901234567890123456789012345678901234567890." +//122
                "a23456789012345678901234567890123456789012345678901234567890.a23456789012345678901234567890123456789012345678901234567890." +//244
                "a23456789012345678901234567890123456789012345678901234567890.a23456789012345678901234567890123456789012345678901234567890." //366
    })
    void testVisibleNames(String name){
        try {
            assertEquals(name, this.setGetVisibleName(name));
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Test all single visible characters
     */
    @Test @DisplayName("Testing all visible characters")
    void testAllVisibleCharacters() {
        try {
            for (int i = ASCII_VISIBLE_MIN; i <= ASCII_VISIBLE_MAX; i++) {
                assertEquals((char)i + "", this.setGetVisibleName((char)i + ""));
            }
        } catch (ValidationException e) {
            fail();
        }
    }


    /**
     * Invalid visible name tests
     * Any ASCII-encoded string of visible characters (0x21-0x7E).
     *
     * @param name visible name to test
     */
    @ParameterizedTest(name = "Invisible name = {0}")
    @ValueSource(strings = {"aasdf" + '\n' + "asdf", " ", "              ", "asdfqwer ", "#$%^Dasdf\n",
            "aasdf" + '\r', "aasdf" + '\t', "aasdf" + '\b',  "aasdf" + '\f', "Ẵ.Ẓ.㛃.\u2B50.⭕.", "asdf.Ƞ."
    })
    void testInvisibleName(String name){
        assertThrows(ValidationException.class, () -> this.setGetVisibleName(name));
    }

    /**
     * Test all single invisible characters
     */
    @Test @DisplayName("Testing all invisible characters")
    void testAllInvisibleCharacters(){
        for(int i=0;i<ASCII_VISIBLE_MIN;i++){
            int finalI = i;
            assertThrows(ValidationException.class, () -> this.setGetVisibleName("" + (char)(finalI)), "Testing " + (char)(finalI));
        }
        for(int i=ASCII_VISIBLE_MAX+1;i<256;i++){
            int finalI = i;
            assertThrows(ValidationException.class, () -> this.setGetVisibleName("" + (char)(finalI)), "Testing " + (char)(finalI));
        }
    }

    /**
     * Null visible name test (with variable throwbale class)
     */
    @Test @DisplayName("Null")
    void testNullName(){
        assertThrows(this.getNullThrowableType(), () -> this.setGetVisibleName(null));
    }

    /**
     * Factory method for calling the appropriate function you want to test for visible name validity
     * @param name name to test
     * @return the result of a getName on the respective object
     * @throws ValidationException if invalid domain name
     */
    protected abstract String setGetVisibleName(String name) throws ValidationException;

    /**
     * Allows the concrete class to specify which exception it wants to be thrown when a
     *   null string is passed to the function
     * @return class to throw
     */
    protected abstract Class<? extends Throwable> getNullThrowableType();
}
