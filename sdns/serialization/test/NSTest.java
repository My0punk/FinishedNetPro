//Contains the NS testing class (see comments below)
//Created: 9/8/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.CName;
import sdns.serialization.NS;
import sdns.serialization.ValidationException;
import sdns.serialization.test.factories.DomainNameTestFactory;
import sdns.serialization.test.factories.EqualsAndHashCodeCaseInsensitiveTestFactory;
import sdns.serialization.test.factories.TTLTestFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
class NSTest {
    //NS type value
    private final long NS_TYPE_VALUE = 2L;

    /**
     * Trivial test (DONE)
     */
    @Test @DisplayName("Get type value")
    void getTypeValue() {
        NS ns;
        try {
            ns = new NS(".", 32, ".");
            assertEquals(NS_TYPE_VALUE, ns.getTypeValue());
        } catch(Exception e){
            assert(false);
        }
    }

    /**
     * Test tostring (DONE)
     */
    @Test @DisplayName("ToString")
    void testToString() {
        String expected = "NS: name=foo.com. ttl=42 nameserver=thisIsABoringName.";
        try {
            NS test = new NS("foo.com.", 42, "thisIsABoringName.");
            String output = test.toString();

            assertEquals(expected, output);
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Name server setter and getter tests (DONE)
     */
    @Nested
    class NameServerSetterGetter extends DomainNameTestFactory {
        /**
         * Factory method for calling the appropriate function you want to test for domain name validity
         *
         * @param dm domain name to test
         * @return the result of a getDM on the respective object
         * @throws ValidationException if invalid domain name
         */
        @Override
        protected String setGetDomainName(String dm) throws ValidationException {
            NS ns = null;
            try {//Shouldn't fail here
                ns = new NS(".", 0, ".");
            } catch(ValidationException e){
                fail();
            }
            ns.setNameServer(dm);
            return ns.getNameServer();
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
     * Constructor valid tests (DONE)
     */
    @Nested
    class NSConstructorTests {
        /**
         * NS constructor name tests
         */
        @Nested
        class NSConstructorNameTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                NS ns = new NS(dm, 0, ".");
                return ns.getName();
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
         * NS constructor name server tests
         */
        @Nested
        class NSConstructorNameServerTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                NS ns = new NS(".", 0, dm);
                return ns.getNameServer();
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
         * NS constructor TTL tests
         */
        @Nested
        class NSConstructorTTLTests extends TTLTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for TTL validity
             *
             * @param ttl ttl to test
             * @return the result of a getTTL on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected int setGetTTL(int ttl) throws ValidationException {
                NS ns = new NS(".", ttl, ".");
                return ns.getTTL();
            }
        }
    }

    //When data enters the domain system its original case should be preserved whenever possible
    //
    //A name is serialized as a continuous sequence of labels. A label is serialized as a one octet length field
    //  followed by that number of octets
    //The final label of a name is serialized as either a single octet with value 0 or two octets where the first octet
    //  starts with two set bits (remaining 14 bits are ignored)
    //Test each section
    //  input too long, too short, null, invalid characters
    //  case insensitivity
    /**
     * Test encode
     */
    @Nested
    class EncodeTests {
        ByteArrayOutputStream testStream = new ByteArrayOutputStream();

        @Test
        void basicEncodeTest() {
            byte[] comparisonArr = {3, 102, 111, 111, 3, 99, 111, 109, 0,
                    0, 2,
                    0, 1,
                    0, 0, 0, 42,
                    0, 9,
                    7, 'N','S','D','N','a','m','e',0};
            int byteArrayBufferSize;
            try {
                NS testNS = new NS("foo.com.", 42, "NSDName.");
                testNS.encode(testStream);
                byte[] dumpArray = testStream.toByteArray();
                testStream.reset();
                assertArrayEquals(dumpArray, comparisonArr);
            } catch (ValidationException | IOException e) {
                fail();
            }
        }

        @Test
        void encodeNullOutStreamTest() {
            try {
                NS testNS = new NS("foo.com.", 42, "NSDName.");
                assertThrows(NullPointerException.class, () -> testNS.encode(null));
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }

        @Test
        void encodeTestNumbersAndHyphens() {
            byte[] comparisonArr = {3, 'f', '-', '9', 0,
                    0, 2,
                    0, 1,
                    0, 0, 0, 42,
                    0, 9,
                    7, 'N','S','D','N','a','m','e',0};
            int byteArrayBufferSize;
            try {
                NS testNS = new NS("f-9.", 42, "NSDName.");
                testNS.encode(testStream);
                byte[] dumpArray = testStream.toByteArray();
                testStream.reset();
                assertArrayEquals(dumpArray, comparisonArr);
            } catch (ValidationException | IOException e) {
                fail();
            }
        }
    }

    /**
     * Equals and hashcode tests (DONE)
     */
    @Nested
    class EqualsAndHashCode extends EqualsAndHashCodeCaseInsensitiveTestFactory<NS> {
        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected NS getDefaultObjectDifferentCase1() throws ValidationException {
            return new NS("GOOD.COM.", 0, "foo.a1.");
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
        protected NS getDefaultObjectDifferentCase2() throws ValidationException {
            return new NS("good.com.", 0, "FOO.A1.");
        }

        /**
         * Factory method for generating a default object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected NS getDefaultObject0() throws ValidationException {
            return new NS("good.com.", 0, "foo.a1.");
        }

        /**
         * Factory method for generating a second object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected NS getDefaultObject1() throws ValidationException {
            return new NS("good.com.q.", 0, "foo.a1.");
        }

        /**
         * Factory method for generating a third object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected NS getDefaultObject2() throws ValidationException {
            return new NS("good.com.", 0, "foo.a1.q.");
        }

        /**
         * Factory method for generating a fourth object to test for (in)equality
         * Defaults to getDefaultObject0
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected NS getDefaultObject3() throws ValidationException {
            return new NS("good.com.", 123, "foo.a1.");
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
        protected NS getDefaultObject4() throws ValidationException {
            return new NS("good.com.", 379, "foo.a1.");
        }

        /**
         * Factory method for generating a SIMILAR object (to default0) of a different type to test for inequality
         * in types and hashcodes
         *
         * @return instantiation of different class object with similar field definitions
         * @throws ValidationException if invalid object
         */
        @Override
        protected CName getDifferentTypeObject() throws ValidationException {
            return new CName("good.com.", 0, "foo.a1.");
        }
    }
}