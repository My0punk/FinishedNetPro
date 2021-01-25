//Contains the DomainNameTestFactory class (see comments below)
//Created: 10/18/20
package sdns.serialization.test.factories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything with a domain name
 */
public abstract class DomainNameTestFactory {
    /**
     * Valid domain name tests
     * Name: -- these tests apply to all domain name field tests
     *   Each label must start with a letter, end with a letter or digit, and have as interior characters only letters
     *     (A-Z and a-z), digits (0-9), and hypen/underscore (-_).
     *   A name with a single, empty label (".") is acceptable
     *
     * @param dm domain name to test
     */
    @ParameterizedTest(name = "Valid DM = {0}")
    @ValueSource(strings = {"a23456789012345678901234567890123456789012345678901234567890123.",
            "a23456789012345678901234567890123456789012345678901234567890.a23456789012345678901234567890123456789012345678901234567890." +//122
                    "a23456789012345678901234567890123456789012345678901234567890.a23456789012345678901234567890123456789012345678901234567890." +//244
                    "a234567890.",//255
            "foo.", "foo.com.", "f0-9.c0m.", "google.com.", "www.baylor.edu.", "f0.c-0.", ".", "f-0.", "f-0a.",
            "f0_9.c0m.", "f0.c_0.", "f_0.", "f_0a.", "f0_-9.c0m.", "f-_-_-_-_0."
    })
    void testValidDomainNames(String dm){
        try {
            assertEquals(dm, this.setGetDomainName(dm));
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Invalid domain name tests
     * Name: -- these tests apply to all domain name field tests
     *   Each label must start with a letter, end with a letter or digit, and have as interior characters only letters
     *     (A-Z and a-z), digits (0-9), and hypen/underscore (-_).
     *   A name with a single, empty label (".") is acceptable
     *
     * @param dm domain name to test
     */
    @ParameterizedTest(name = "Invalid DM = {0}")
    @ValueSource(strings = {"", "asdf", "asdf.].", "asdf.Ƞ.", "asdf..", "www.baylor.edu/", "..", "asdf.asdf", "f0-9.c0m-.", "Ẵ.Ẓ.㛃.\u2B50.⭕.",
            "-a.f", "-.", "-",
            "a234567890123456789012345678901234567890123456789012345678901234.",//64
            "a23456789012345678901234567890123456789012345678901234567890.a23456789012345678901234567890123456789012345678901234567890." +//122
                    "a23456789012345678901234567890123456789012345678901234567890.a23456789012345678901234567890123456789012345678901234567890." +//244
                    "a2345678901.",//256
            "f0-9.c0m_.", "_a.f", "_.", "_",
    })
    void testInvalidDomainNames(String dm){
        assertThrows(ValidationException.class, () -> this.setGetDomainName(dm));
    }

    /**
     * Null domain name test (with variable throwbale class)
     */
    @Test @DisplayName("Null")
    void testNullDomainName(){
        assertThrows(this.getNullThrowableType(), () -> this.setGetDomainName(null));
    }

    /**
     * Factory method for calling the appropriate function you want to test for domain name validity
     * @param dm domain name to test
     * @return the result of a getDM on the respective object
     * @throws ValidationException if invalid domain name
     */
    protected abstract String setGetDomainName(String dm) throws ValidationException;

    /**
     * Allows the concrete class to specify which exception it wants to be thrown when a
     *   null string is passed to the function
     * @return class to throw
     */
    protected abstract Class<? extends Throwable> getNullThrowableType();
}
