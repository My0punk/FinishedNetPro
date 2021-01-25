//Contains the ATest class (see comments below)
//Created: 9/18/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.*;
import sdns.serialization.test.factories.DomainNameTestFactory;
import sdns.serialization.test.factories.EqualsAndHashCodeCaseInsensitiveTestFactory;
import sdns.serialization.test.factories.IPv4TestFactory;
import sdns.serialization.test.factories.TTLTestFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
class ATest {
    //A type value
    private final long A_TYPE_VALUE = 1L;

    /**
     * Trivial test (DONE)
     */
    @Test
    void getTypeValue() {
        A a;
        try {
            a = new A(".", 32, (Inet4Address)Inet4Address.getByName("1.1.1.1"));
            assertEquals(A_TYPE_VALUE, a.getTypeValue());
        } catch(Exception e){
            assert(false);
        }
    }

    /**
     * Address name setter and getter tests (DONE)
     */
    @Nested
    class IPv4SetterGetter extends IPv4TestFactory {
        /**
         * Factory method for calling the appropriate function you want to test for IPv4 validity
         *
         * @param ip ip to test
         * @return the result of a getIP on the respective object
         * @throws ValidationException if invalid object
         */
        @Override
        protected Inet4Address setGetIPv4(Inet4Address ip) throws ValidationException {
            A a = null;
            try {
                a = new A(".", 0, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                fail();
            }
            a.setAddress(ip);
            return a.getAddress();
        }

        /**
         * Allows the concrete class to specify which exception it wants to be thrown when a
         * null IPv4 is passed to the function
         *
         * @return class to throw
         */
        @Override
        protected Class<? extends Throwable> getNullThrowableType() {
            return ValidationException.class;
        }
    }

    /**
     * To string tests (DONE)
     */
    @Test
    void testToString() {
        String expected = "A: name=foo.com. ttl=42 address=0.0.0.0";
        try {
            A test = new A("foo.com.", 42, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
            String output = test.toString();

            assertEquals(expected, output);
        } catch (ValidationException | UnknownHostException e) {
            fail();
        }
    }

    /**
     * Constructor tests (valid and invalid) (DONE)
     */
    @Nested
    class AConstructorValid {
        /**
         * A constructor name tests (valid and invalid)
         */
        @Nested
        class AConstructorNameTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                A a = null;
                try {
                    a = new A(dm, 0, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
                } catch (UnknownHostException e) {
                    fail();
                }
                return a.getName();
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
         * A constructor IPv4 tests
         */
        @Nested
        class AConstructorIPv4Tests extends IPv4TestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for IPv4 validity
             *
             * @param ip ip to test
             * @return the result of a getIP on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected Inet4Address setGetIPv4(Inet4Address ip) throws ValidationException {
                A a = new A(".", 0, ip);
                return a.getAddress();
            }

            /**
             * Allows the concrete class to specify which exception it wants to be thrown when a
             * null IPv4 is passed to the function
             *
             * @return class to throw
             */
            @Override
            protected Class<? extends Throwable> getNullThrowableType() {
                return ValidationException.class;
            }
        }

        /**
         * A constructor TTL tests
         */
        @Nested
        class AConstructorTTLTests extends TTLTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for TTL validity
             *
             * @param ttl ttl to test
             * @return the result of a getTTL on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected int setGetTTL(int ttl) throws ValidationException {
                A a = null;
                try {
                    a = new A(".", ttl, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
                } catch (UnknownHostException e) {
                    fail();
                }
                return a.getTTL();
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
    //  input too long, too short, null, invalid characters, missing sections
    //  case insensitivity

    /**
     * Test encode
     */
    @Nested
    class ValidEncodeTests {
        ByteArrayOutputStream testStream = new ByteArrayOutputStream();

        //Basic encode test
        @Test @DisplayName("A RR valid encode basic")
        void basicEncodeTest() {
            byte[] comparisonArr = {3, 102, 111, 111, 3, 99, 111, 109, 0,
                    0, 1,
                    0, 1,
                    0, 0, 0, 42,
                    0, 4,
                    -64, -88, 0, 69};
            try {
                //A object instantiation here
                A testA = new A("foo.com.", 42, (Inet4Address)Inet4Address.getByName("192.168.0.69"));
                testA.encode(testStream);
                byte[] dumpArray = testStream.toByteArray();
                testStream.reset();
                assertArrayEquals(comparisonArr, dumpArray);
            } catch (ValidationException | IOException e) {
                fail();
            }
        }

        //Encode null
        @Test @DisplayName("A RR valid encode null test")
        void encodeNullOutStreamTest() {
            try {
                A testA = new A("foo.com.", 42, (Inet4Address)Inet4Address.getByName("192.168.0.42"));
                assertThrows(NullPointerException.class, () -> testA.encode(null));
            } catch (ValidationException | IOException e) {
                fail();
            }
        }

        //Encode empty name
        @Test @DisplayName("A RR valid encode empty name test")
        void encodeEmptyName() {
            byte[] comparisonArr = {0,
                    0, 1,
                    0, 1,
                    0, 0, 0, 42,
                    0, 4,
                    -64, -88, 0, 69};
            try {
                //A object instantiation here
                A testA = new A(".", 42, (Inet4Address)Inet4Address.getByName("192.168.0.69"));
                testA.encode(testStream);
                byte[] dumpArray = testStream.toByteArray();
                testStream.reset();

                assertArrayEquals(comparisonArr, dumpArray);
            } catch (ValidationException | IOException e) {
                fail();
            }
        }

        //Encode with numbers and hyphens
        @Test @DisplayName("A RR valid encode num and hyphens in name")
        void encodeTestNumbersAndHyphens() {
            byte[] comparisonArr = {3, 'f', '-', '9', 0,
                    0, 1,
                    0, 1,
                    0, 0, 0, 42,
                    0, 4,
                    -64,-88,0,69};
            try {
                A testA = new A("f-9.", 42,  (Inet4Address)Inet4Address.getByName("192.168.0.69"));
                testA.encode(testStream);
                byte[] dumpArray = testStream.toByteArray();
                testStream.reset();
                assertArrayEquals(dumpArray, comparisonArr);
            } catch (ValidationException | IOException e) {
                fail();
            }
        }
    }

    /**
     * Equals and hash code testing
     */
    @Nested
    class EqualsAndHashCode extends EqualsAndHashCodeCaseInsensitiveTestFactory<A> {
        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected A getDefaultObjectDifferentCase1() throws ValidationException {
            A a = null;
            try {
                a = new A("GOOD.COM.", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                fail();
            }
            return a;
        }

        /**
         * Factory method for generating a default object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected A getDefaultObject0() throws ValidationException {
            A a = null;
            try {
                a = new A("good.com.", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                fail();
            }
            return a;
        }

        /**
         * Factory method for generating a second object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected A getDefaultObject1() throws ValidationException {
            A a = null;
            try {
                a = new A("good.com.q.", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                fail();
            }
            return a;
        }

        /**
         * Factory method for generating a third object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected A getDefaultObject2() throws ValidationException {
            A a = null;
            try {
                a = new A("good.com.", 379, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                fail();
            }
            return a;
        }

        /**
         * Factory method for generating a fourth object to test for (in)equality
         * Defaults to getDefaultObject0
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected A getDefaultObject3() throws ValidationException {
            A a = null;
            try {
                a = new A("good.com.", 123, (Inet4Address)Inet4Address.getByName("255.255.255.255"));
            } catch (UnknownHostException e) {
                fail();
            }
            return a;
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
        protected A getDefaultObject4() throws ValidationException {
            A a = null;
            try {
                a = new A("good.com.", 123, (Inet4Address)Inet4Address.getByName("254.255.255.255"));
            } catch (UnknownHostException e) {
                fail();
            }
            return a;
        }

        /**
         * Factory method for generating a SIMILAR object (to default0) of a different type to test for inequality
         * in types and hashcodes
         *
         * @return instantiation of different class object with similar field definitions
         * @throws ValidationException if invalid object
         */
        @Override
        protected AAAA getDifferentTypeObject() throws ValidationException {
            AAAA a = null;
            try {
                a = new AAAA("good.com.", 123,
                        (Inet6Address) Inet6Address.getByName("0000:0000:0000:0000:0000:0000:0000:0000"));
            } catch (UnknownHostException e) {
                fail();
            }
            return a;
        }
    }
}