//Contains the CAATest class (see comments below)
//Created: 10/25/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.CAA;
import sdns.serialization.NS;
import sdns.serialization.ValidationException;
import sdns.serialization.test.factories.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
public class CAATest {
    //MX type value
    private final int CAA_TYPE_VALUE = 257;

    /**
     * test get type value (trivial) (DONE)
     */
    @Test @DisplayName("Get type value")
    void getTypeValue() {
        CAA caa;
        try {
            caa = new CAA(".", 0, "asdf1234e#$%^.");
            assertEquals(CAA_TYPE_VALUE, caa.getTypeValue());
        } catch(Exception e){
            fail();
        }
    }

    /**
     * Test to string (DONE)
     */
    @Test @DisplayName("ToString")
    void testToString() {
        String expected = "CAA: name=foo.com. ttl=500 issuer=pki.goog.";
        try {
            CAA caa = new CAA("foo.com.", 500, "pki.goog.");
            String output = caa.toString();

            assertEquals(expected, output);
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Issue setter/getter tests (DONE)
     */
    @Nested
    class IssuerSetterGetter extends VisibleAsciiTestFactory {
        /**
         * Factory method for calling the appropriate function you want to test for visible name validity
         *
         * @param name name to test
         * @return the result of a getName on the respective object
         * @throws ValidationException if invalid domain name
         */
        @Override
        protected String setGetVisibleName(String name) throws ValidationException {
            CAA caa = null;
            try {
                caa = new CAA(".", 0, ".");
            } catch(ValidationException e){
                fail();
            }
            caa.setIssuer(name);
            return caa.getIssuer();
        }

        /**
         * Allows the concrete class to specify which exception it wants to be thrown when a
         * null string is passed to the function
         *
         * @return class to throw
         */
        @Override
        protected Class<? extends Throwable> getNullThrowableType() {
            return ValidationException.class;
        }
    }


    /**
     * Constructor tests (DONE)
     */
    @Nested
    class ConstructorTests {
        /**
         * Name (domain name) tests
         */
        @Nested
        class ConstructorNameTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                CAA caa = new CAA(dm, 123, "foo.");
                return caa.getName();
            }

            /**
             * Allows the concrete class to specify which exception it wants to be thrown when a
             * null string is passed to the function
             *
             * @return class to throw
             */
            @Override
            protected Class<? extends Throwable> getNullThrowableType() {
                return ValidationException.class;
            }
        }

        /**
         * TTL tests
         */
        @Nested
        class ConstructorTTLTests extends TTLTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for TTL validity
             *
             * @param ttl ttl to test
             * @return the result of a getTTL on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected int setGetTTL(int ttl) throws ValidationException {
                CAA caa = new CAA("good.com.", ttl, "foo.");
                return caa.getTTL();
            }
        }

        /**
         * Issue (visible name) tests
         */
        @Nested
        class ConstructorIssueTests extends VisibleAsciiTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for visible name validity
             *
             * @param name name to test
             * @return the result of a getName on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetVisibleName(String name) throws ValidationException {
                CAA mx = new CAA("foo.", 123, name);
                return mx.getIssuer();
            }

            /**
             * Allows the concrete class to specify which exception it wants to be thrown when a
             * null string is passed to the function
             *
             * @return class to throw
             */
            @Override
            protected Class<? extends Throwable> getNullThrowableType() {
                return ValidationException.class;
            }
        }
    }


    /**
     * Equals and hash code testing (DONE)
     */
    @Nested
    class EqualsAndHashCode extends EqualsAndHashCodeCaseInsensitiveTestFactory<CAA> {
        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CAA getDefaultObjectDifferentCase1() throws ValidationException {
            return new CAA("GOOD.COM.", 123, "foo.a1.");
        }

        /**
         * Factory method for generating the second same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         * Defaults to getDefaultObjectDifferentCase0 (if only 1 field to test)
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CAA getDefaultObjectDifferentCase2() throws ValidationException {
            return new CAA("good.com.", 123, "FOO.A1.");
        }

        /**
         * Factory method for generating a default object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CAA getDefaultObject0() throws ValidationException {
            return new CAA("good.com.", 123, "foo.a1.");
        }

        /**
         * Factory method for generating a second object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CAA getDefaultObject1() throws ValidationException {
            return new CAA("good.com.q.", 123, "foo.a1.");
        }

        /**
         * Factory method for generating a third object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CAA getDefaultObject2() throws ValidationException {
            return new CAA("good.com.", 123, "foo.a1.q");
        }

        /**
         * Factory method for generating a fourth object to test for (in)equality
         * Defaults to getDefaultObject0
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CAA getDefaultObject3() throws ValidationException {
            return new CAA("good.com.", 123, "foo.a1.");
        }

        /**
         * Factory method for generating a fifth object to test for (in)equality
         * RESERVED FOR COMPLEX EQUALS
         * Defaults to getDefaultObject1
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CAA getDefaultObject4() throws ValidationException {
            return new CAA("good.com.", 379, "foo.a1.");
        }

        /**
         * Factory method for generating a sixth object to test for (in)equality
         * Defaults to getDefaultObject2
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CAA getDefaultObject5() throws ValidationException {
            return new CAA("goodcom.", 123, "foo.a1.");
        }

        /**
         * Factory method for generating a SIMILAR object (to default0) of a different type to test for inequality
         * in types and hashcodes
         *
         * @return instantiation of different class object with similar field definitions
         * @throws ValidationException if invalid object
         */
        @Override
        protected NS getDifferentTypeObject() throws ValidationException {
            return new NS("good.com.", 123, "foo.a1.");
        }
    }

    /**
     * Valid encode tests
     */
    @Nested
    class ValidEncodeTests {
        ByteArrayOutputStream encodedStream = new ByteArrayOutputStream();

        /**
         * Basic encode tests
         */
        @Test @DisplayName("CAA RR Basic Valid Encode")
        void basicEncodeTest() {
            byte[] expectedArr = {3, 102, 111, 111, 3, 99, 111, 109, 0,
                    1, 1,
                    0, 1,
                    0, 0, 0, 42,
                    0, 14,
                    0, 5,
                    'i', 's', 's', 'u', 'e',
                    'f', 'o', 'o', '.', 'a', '1', '.'
            };
            try {
                CAA caa = new CAA("foo.com.", 42, "foo.a1.");
                caa.encode(encodedStream);
                assertArrayEquals(expectedArr, encodedStream.toByteArray());
                encodedStream.reset();
            } catch(ValidationException | IOException e) {
                fail();
            }
        }
    }
}
