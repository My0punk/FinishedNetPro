//Contains the AAAA testing class (see comments below)
//Created: 10/1/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.A;
import sdns.serialization.AAAA;
import sdns.serialization.ValidationException;
import sdns.serialization.test.factories.DomainNameTestFactory;
import sdns.serialization.test.factories.EqualsAndHashCodeCaseInsensitiveTestFactory;
import sdns.serialization.test.factories.IPv6TestFactory;
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
class AAAATest {
    //AAAA type value
    private final long AAAA_TYPE_VALUE = 28L;

    /**
     * Creates a default IPv6 ("0123:4567:89AB:CDEF:0123:4567:89AB:CDEF")
     * @return IPv6
     * @throws UnknownHostException if incorrect IPv6
     */
    private static Inet6Address getDefault0IPv6() throws UnknownHostException {
        return (Inet6Address)Inet6Address.getByName("0123:4567:89AB:CDEF:0123:4567:89AB:CDEF");
    }
    /**
     * Creates a default IPv6 ("0000:0000:0000:0000:0000:0000:0000:0000")
     * @return IPv6
     * @throws UnknownHostException if incorrect IPv6
     */
    private static Inet6Address getDefault1IPv6() throws UnknownHostException {
        return (Inet6Address)Inet6Address.getByName("0000:0000:0000:0000:0000:0000:0000:0000");
    }
    /**
     * Creates a default IPv6 ("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF")
     * @return IPv6
     * @throws UnknownHostException if incorrect IPv6
     */
    private static Inet6Address getDefault2IPv6() throws UnknownHostException {
        return (Inet6Address)Inet6Address.getByName("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF");
    }


    /**
     * Address name setter and getter tests (DONE)
     */
    @Nested
    class IPv6SetterGetter extends IPv6TestFactory {
        /**
         * Factory method for calling the appropriate function you want to test for IPv6 validity
         *
         * @param ip ip to test
         * @return the result of a getIP on the respective object
         * @throws ValidationException if invalid object
         */
        @Override
        protected Inet6Address setGetIPv6(Inet6Address ip) throws ValidationException {
            AAAA a = null;
            try {//shouldn't fail here
                a = new AAAA(".", 0, getDefault1IPv6());
            } catch (ValidationException | UnknownHostException e) {
                fail();
            }
            a.setAddress(ip);
            return a.getAddress();
        }

        /**
         * Allows the concrete class to specify which exception it wants to be thrown when a
         * null IPv6 is passed to the function
         *
         * @return class to throw
         */
        @Override
        protected Class<? extends Throwable> getNullThrowableType() {
            return ValidationException.class;
        }
    }

    /**
     * Trivial test (DONE)
     */
    @Test
    void getTypeValue() {
        AAAA a;
        try {
            a = new AAAA(".", 32, getDefault0IPv6());
            assertEquals(AAAA_TYPE_VALUE, a.getTypeValue());
        } catch(Exception e){
            assert(false);
        }
    }

    /**
     * To string test (DONE)
     */
    @Test
    void testToString() {
        try {
            String expected = "AAAA: name=foo.com. ttl=42 address=" + getDefault0IPv6().getHostAddress();
            AAAA test = new AAAA("foo.com.", 42, getDefault0IPv6());
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
    class AAAAConstructorTests {
        /**
         * AAAA constructor query tests (valid and invalid)
         */
        @Nested
        class AAAAConstructorNameTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                AAAA a = null;
                try {
                    a = new AAAA(dm, 0, getDefault1IPv6());
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
         * AAAA constructor IPv6 tests
         */
        @Nested
        class AAAAConstructorIPv6Tests extends IPv6TestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for IPv6 validity
             * @param ip ip to test
             * @return the result of a getIP on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected Inet6Address setGetIPv6(Inet6Address ip) throws ValidationException {
                AAAA a = new AAAA(".", 0, ip);
                return a.getAddress();
            }

            /**
             * Allows the concrete class to specify which exception it wants to be thrown when a
             * null IPv6 is passed to the function
             *
             * @return class to throw
             */
            @Override
            protected Class<? extends Throwable> getNullThrowableType() {
                return ValidationException.class;
            }
        }

        /**
         * AAAA constructor TTL tests
         */
        @Nested
        class AAAAConstructorTTLTests extends TTLTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for TTL validity
             *
             * @param ttl ttl to test
             * @return the result of a getTTL on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected int setGetTTL(int ttl) throws ValidationException {
                AAAA a = null;
                try {
                    a = new AAAA(".", ttl, getDefault2IPv6());
                } catch (UnknownHostException e) {
                    fail();
                }
                return a.getTTL();
            }
        }
    }



    /**
     * Test encode
     */
    @Nested
    class ValidEncodeTests {
        ByteArrayOutputStream testStream = new ByteArrayOutputStream();

        //Basic encode test
        @Test @DisplayName("A RR valid encode basic")
        void basicEncodeTest() {
            byte[] comparisonArrNoIP = {3, 102, 111, 111, 3, 99, 111, 109, 0,//foo.com.
                    0, (byte)AAAA_TYPE_VALUE,//type
                    0, 1,//0x0001
                    0, 0, 0, 42,//ttl
                    0, 16};//rdlength

            try {
                byte[] addr = getDefault0IPv6().getAddress();
                byte[] comparisonArr = new byte[comparisonArrNoIP.length + addr.length];
                System.arraycopy(comparisonArrNoIP, 0, comparisonArr, 0, comparisonArrNoIP.length);
                System.arraycopy(addr, 0, comparisonArr, comparisonArrNoIP.length, addr.length);

                //A object instantiation here
                AAAA testA = new AAAA("foo.com.", 42, getDefault0IPv6());
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
                AAAA testA = new AAAA("foo.com.", 42, getDefault0IPv6());
                assertThrows(NullPointerException.class, () -> testA.encode(null));
            } catch (ValidationException | IOException e) {
                fail();
            }
        }

        //Encode empty name
        @Test @DisplayName("A RR valid encode empty name test")
        void encodeEmptyName() {
            byte[] comparisonArrNoIP = { 0,//.
                    0, (byte)AAAA_TYPE_VALUE,//type
                    0, 1,//0x0001
                    0, 0, 0, 42,//ttl
                    0, 16};//,//rdlength

            try {
                byte[] addr = getDefault0IPv6().getAddress();
                byte[] comparisonArr = new byte[comparisonArrNoIP.length + addr.length];
                System.arraycopy(comparisonArrNoIP, 0, comparisonArr, 0, comparisonArrNoIP.length);
                System.arraycopy(addr, 0, comparisonArr, comparisonArrNoIP.length, addr.length);

                //A object instantiation here
                AAAA testA = new AAAA(".", 42, getDefault0IPv6());
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
            byte[] comparisonArrNoIP = {3, 'f', '-', '9', 0,//foo.com.
                    0, (byte)AAAA_TYPE_VALUE,//type
                    0, 1,//0x0001
                    0, 0, 0, 42,//ttl
                    0, 16};//,//rdlength
            //-64, -88, 0, 69};
            try {
                byte[] addr = getDefault0IPv6().getAddress();
                byte[] comparisonArr = new byte[comparisonArrNoIP.length + addr.length];
                System.arraycopy(comparisonArrNoIP, 0, comparisonArr, 0, comparisonArrNoIP.length);
                System.arraycopy(addr, 0, comparisonArr, comparisonArrNoIP.length, addr.length);

                AAAA testA = new AAAA("f-9.", 42, getDefault0IPv6());
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
     * Equals and hash code testing (DONE)
     */
    @Nested
    class EqualsAndHashCode extends EqualsAndHashCodeCaseInsensitiveTestFactory<AAAA> {
        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected AAAA getDefaultObjectDifferentCase1() throws ValidationException {
            AAAA a = null;
            try {
                a = new AAAA("GOOD.COM.", 123, getDefault1IPv6());
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
        protected AAAA getDefaultObject0() throws ValidationException {
            AAAA a = null;
            try {
                a = new AAAA("good.com.", 123, getDefault1IPv6());
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
        protected AAAA getDefaultObject1() throws ValidationException {
            AAAA a = null;
            try {
                a = new AAAA("good.com.q.", 123, getDefault1IPv6());
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
        protected AAAA getDefaultObject2() throws ValidationException {
            AAAA a = null;
            try {
                a = new AAAA("good.com.", 379, getDefault1IPv6());
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
        protected AAAA getDefaultObject3() throws ValidationException {
            AAAA a = null;
            try {
                a = new AAAA(".", 123, (Inet6Address)Inet6Address.getByName("0000:0000:0000:0000:0000:0000:0000:0000"));
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
        protected AAAA getDefaultObject4() throws ValidationException {
            AAAA a = null;
            try {
                a = new AAAA(".", 123, (Inet6Address)Inet6Address.getByName("0001:0000:0000:0000:0000:0000:0000:0000"));
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
        protected A getDifferentTypeObject() throws ValidationException {
            A a = null;
            try {
                a = new A("good.com.", 123, (Inet4Address) Inet4Address.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                fail();
            }
            return a;
        }
    }
}