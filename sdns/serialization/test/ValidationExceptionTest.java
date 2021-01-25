//Contains the ValidationExceptionTest class (see comments below)
//Created: 9/8/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
class ValidationExceptionTest {
    /**
     * Constructor tests (and, by extension, getBadToken test)
     */
    @Nested
    class constructorTests {
        /**
         * Basic valid test
         */
        @Test @DisplayName("Basic valid test")
        void testConst(){
            ValidationException ve = new ValidationException("hi", "null pointer");
            assertAll("Members", () -> assertEquals("hi", ve.getMessage()),
                        () -> assertEquals("null pointer", ve.getBadToken()));
        }

        /**
         * Test the throwable cause
         */
        @Test @DisplayName("Throwable cause")
        void testConstThrowable(){
            Throwable t = new ValidationException("Hehe nested validation exception", "/shrug");
            ValidationException ve = new ValidationException("hi", t, "null pointer");
            assertAll("Throwable", () -> assertEquals(ValidationException.class, ve.getCause().getClass()),
                    () -> assertEquals("Hehe nested validation exception", ve.getCause().getMessage()),
                    () -> assertEquals("/shrug", ((ValidationException)ve.getCause()).getBadToken()));
        }
    }
}