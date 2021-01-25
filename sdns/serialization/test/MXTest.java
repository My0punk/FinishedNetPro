//Contains the MXTest class (see comments below)
//Created: 10/18/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.MX;
import sdns.serialization.NS;
import sdns.serialization.ValidationException;
import sdns.serialization.test.factories.DomainNameTestFactory;
import sdns.serialization.test.factories.EqualsAndHashCodeCaseInsensitiveTestFactory;
import sdns.serialization.test.factories.UnsignedShortTestFactory;
import sdns.serialization.test.factories.TTLTestFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
public class MXTest {
    //MX type value
    private final int MX_TYPE_VALUE = 15;

    /**
     * test get type value (trivial) (DONE)
     */
    @Test @DisplayName("Get type value")
    void getTypeValue() {
        MX mx;
        try {
            mx = new MX(".", 0, ".", 0);
            assertEquals(MX_TYPE_VALUE, mx.getTypeValue());
        } catch(Exception e){
            fail();
        }
    }

    /**
     * Test to string (DONE)
     */
    @Test @DisplayName("ToString")
    void testToString() {
        String expected = "MX: name=foo.com. ttl=42 exchange=ex.com. preference=4";
        try {
            MX mx = new MX("foo.com.", 42, "ex.com.", 4);
            String output = mx.toString();

            assertEquals(expected, output);
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Exchange setter/getter tests (DONE)
     */
    @Nested
    class ExchangeSetterGetter extends DomainNameTestFactory {
        /**
         * Factory method for calling the appropriate function you want to test for domain name validity
         *
         * @param dm domain name to test
         * @return the result of a getDM on the respective object
         * @throws ValidationException if invalid domain name
         */
        @Override
        protected String setGetDomainName(String dm) throws ValidationException {
            MX mx = null;
            try {//shouldn't fail here
                mx = new MX(".", 0, ".", 0);
            } catch(ValidationException e){
                fail();
            }
            mx.setExchange(dm);
            return mx.getExchange();
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
     * Preference setter/getter tests (DONE)
     */
    @Nested
    class PreferenceSetterGetter extends UnsignedShortTestFactory {
        /**
         * Factory method for calling the appropriate function you want to test for preference validity
         *
         * @param pref preference to test
         * @return the result of a getPreference on the respective object
         * @throws ValidationException if invalid object
         */
        @Override
        protected int setGetUnsignedShort(int pref) throws ValidationException {
            MX mx = null;
            try {
                mx = new MX(".", 0, ".", 0);
            } catch(ValidationException e){
                fail();
            }
            mx.setPreference(pref);
            return mx.getPreference();
        }
    }

    /**
     * Constructor tests (DONE)
     */
    @Nested
    class MXConstructorTests {
        /**
         * Name (domain name) tests
         */
        @Nested
        class MXConstructorNameTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                MX mx = new MX(dm, 123, "foo.", 4);
                return mx.getName();
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
        class MXConstructorTTLTests extends TTLTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for TTL validity
             *
             * @param ttl ttl to test
             * @return the result of a getTTL on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected int setGetTTL(int ttl) throws ValidationException {
                MX mx = new MX("good.com.", ttl, "foo.", 4);
                return mx.getTTL();
            }
        }

        /**
         * Exchange (domain name) tests
         */
        @Nested
        class MXConstructorExchangeTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                MX mx = new MX("foo.", 123, dm, 5);
                return mx.getExchange();
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
         * Preference tests
         */
        @Nested
        class MXConstructorPreferenceTests extends UnsignedShortTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for Preference validity
             *
             * @param pref preference to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected int setGetUnsignedShort(int pref) throws ValidationException {
                MX mx = new MX("good.com.", 123, "foo.", pref);
                return mx.getPreference();
            }
        }
    }

    /**
     * Equals and hash code testing (DONE)
     */
    @Nested
    class EqualsAndHashCode extends EqualsAndHashCodeCaseInsensitiveTestFactory<MX> {
        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected MX getDefaultObjectDifferentCase1() throws ValidationException {
            return new MX("GOOD.COM.", 123, "foo.a1.", 0);
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
        protected MX getDefaultObjectDifferentCase2() throws ValidationException {
            return new MX("good.com.", 123, "FOO.A1.", 0);
        }

        /**
         * Factory method for generating a default object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected MX getDefaultObject0() throws ValidationException {
            return new MX("good.com.", 123, "foo.a1.", 0);
        }

        /**
         * Factory method for generating a second object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected MX getDefaultObject1() throws ValidationException {
            return new MX("good.com.q.", 123, "foo.a1.", 0);
        }

        /**
         * Factory method for generating a third object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected MX getDefaultObject2() throws ValidationException {
            return new MX("good.com.", 123, "foo.a1.q.", 0);
        }

        /**
         * Factory method for generating a fourth object to test for (in)equality
         * Defaults to getDefaultObject0
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected MX getDefaultObject3() throws ValidationException {
            return new MX("good.com.", 123, "foo.a1.", 4);
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
        protected MX getDefaultObject4() throws ValidationException {
            return new MX("good.com.", 379, "foo.a1.", 4);
        }

        /**
         * Factory method for generating a sixth object to test for (in)equality
         * Defaults to getDefaultObject2
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected MX getDefaultObject5() throws ValidationException {
            return new MX("good.com.", 123, "foo.a1.", 4);
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
         * MX RR basic valid encode
         */
        @Test @DisplayName("MX RR basic valid encode")
        void basicEncodeTest() {
            byte[] expectedArr = {3, 102, 111, 111, 3, 99, 111, 109, 0,
                    0, 15,
                    0, 1,
                    0, 0, 0, 42,
                    0, 7,
                    0, 5,
                    3, 102, 111, 111, 0};
            try {
                MX mx = new MX("foo.com.", 42, "foo.", 5);
                mx.encode(encodedStream);
                assertArrayEquals(expectedArr, encodedStream.toByteArray());
                encodedStream.reset();
            } catch (ValidationException | IOException e) {
                fail();
            }
        }

        /**
         * MX encode null
         */
        @Test @DisplayName("MX Encode Null")
        void encodeNullStreamTest() {
            try {
                MX mx = new MX("foo.com.", 42, "foo.", 5);
                assertThrows(NullPointerException.class, () -> mx.encode(null));
            } catch (ValidationException e) {
                fail();
            }
        }

        /**
         * MX encode empty name
         */
        @Test @DisplayName("MX encode empty name")
        void encodeEmptyName() {
            byte[] expectedArr = {0,
                    0, 15,
                    0, 1,
                    0, 0, 0, 42,
                    0, 7,
                    0, 5,
                    3, 102, 111, 111, 0};
            try {
                MX mx = new MX(".", 42, "foo.", 5);
                mx.encode(encodedStream);
                assertArrayEquals(expectedArr, encodedStream.toByteArray());
                encodedStream.reset();
            } catch (ValidationException | IOException e) {
                fail();
            }
        }
    }
}
