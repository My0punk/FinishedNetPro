//Contains the SOA testing class (see comments below)
//Created: 11/4/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.NS;
import sdns.serialization.SOA;
import sdns.serialization.ValidationException;
import sdns.serialization.test.factories.DomainNameTestFactory;
import sdns.serialization.test.factories.EqualsAndHashCodeCaseInsensitiveTestFactory;
import sdns.serialization.test.factories.TTLTestFactory;
import sdns.serialization.test.factories.UnsignedIntTestFactory;


import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
public class SOATest {
    //SOA type value
    private final long SOA_TYPE_VALUE = 6L;

    /**
     * Trivial test (DONE)
     */
    @Test @DisplayName("Type value")
    void getTypeValue() {
        SOA soa;
        try {
            soa = new SOA(".", 32, ".", ".", 1, 1, 1, 1, 1);
            assertEquals(SOA_TYPE_VALUE, soa.getTypeValue());
        } catch(Exception e){
            assert(false);
        }
    }

    /**
     * To string tests (DONE)
     */
    @Test @DisplayName("To string")
    void testToString() {
        String expected = "SOA: name=foo.com. ttl=500 mname=ns1.com. rname=dns.com. serial=543 refresh=900 retry=900 expire=1800 minimum=60";
        try {
            SOA test = new SOA("foo.com.", 500, "ns1.com.", "dns.com.", 543, 900, 900, 1800, 60);
            String output = test.toString();

            assertEquals(expected, output);
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * All getter/setter tests
     */
    @Nested
    class GetterSetterTests {
        /**
         * MName getter/setter tests (DONE)
         */
        @Nested
        class MNameGetterSetter extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, 0, 0, 0);
                s.setMName(dm);
                return s.getMName();
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
         * RName getter/setter tests (DONE)
         */
        @Nested
        class RNameGetterSetter extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, 0, 0, 0);
                s.setRName(dm);
                return s.getRName();
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
         * Serial getter/setter tests (DONE)
         */
        @Nested
        class SerialGetterSetter extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, 0, 0, 0);
                s.setSerial(x);
                return s.getSerial();
            }
        }

        /**
         * Refresh getter/setter tests (DONE)
         */
        @Nested
        class RefreshGetterSetter extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, 0, 0, 0);
                s.setRefresh(x);
                return s.getRefresh();
            }
        }

        /**
         * Retry getter/setter tests (DONE)
         */
        @Nested
        class RetryGetterSetter extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, 0, 0, 0);
                s.setRetry(x);
                return s.getRetry();
            }
        }

        /**
         * Expire getter/setter tests (DONE)
         */
        @Nested
        class ExpireGetterSetter extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, 0, 0, 0);
                s.setExpire(x);
                return s.getExpire();
            }
        }

        /**
         * Minimum getter/setter tests (DONE)
         */
        @Nested
        class MinimumGetterSetter extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, 0, 0, 0);
                s.setMinimum(x);
                return s.getMinimum();
            }
        }
    }

    /**
     * All constructor tests
     */
    @Nested
    class ConstructorTests {
        /**
         * Constructor name tests
         */
        @Nested
        class SOAConstructorName extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                SOA s = new SOA(dm, 0, ".", ".", 0, 0, 0, 0, 0);
                return s.getName();
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
         * Constructor TTL tests
         */
        @Nested
        class SOAConstructorTTL extends TTLTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for TTL validity
             *
             * @param ttl ttl to test
             * @return the result of a getTTL on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected int setGetTTL(int ttl) throws ValidationException {
                SOA s = new SOA(".", ttl, ".", ".", 0, 0, 0, 0, 0);
                return s.getTTL();
            }
        }

        /**
         * Constructor mName tests
         */
        @Nested
        class SOAConstructorMName extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                SOA s = new SOA(".", 0, dm, ".", 0, 0, 0, 0, 0);
                return s.getMName();
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
         * Constructor rName tests
         */
        @Nested
        class SOAConstructorRName extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                SOA s = new SOA(".", 0, ".", dm, 0, 0, 0, 0, 0);
                return s.getRName();
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
         * Constructor serial tests
         */
        @Nested
        class SOAConstructorSerial extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", x, 0, 0, 0, 0);
                return s.getSerial();
            }
        }

        /**
         * Constructor refresh tests
         */
        @Nested
        class SOAConstructorRefresh extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, x, 0, 0, 0);
                return s.getRefresh();
            }
        }

        /**
         * Constructor retry tests
         */
        @Nested
        class SOAConstructorRetry extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, x, 0, 0);
                return s.getRetry();
            }
        }

        /**
         * Constructor expire tests
         */
        @Nested
        class SOAConstructorExpire extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, 0, x, 0);
                return s.getExpire();
            }
        }

        /**
         * Constructor minimum tests
         */
        @Nested
        class SOAConstructorMinimum extends UnsignedIntTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for unsigned ints validity
             *
             * @param x unsigned int to test
             * @return the result of a getPreference on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected long setGetUnsignedInt(long x) throws ValidationException {
                SOA s = new SOA(".", 0, ".", ".", 0, 0, 0, 0, x);
                return s.getMinimum();
            }
        }
    }

    /**
     * Equals and hash code tests
     */
    @Nested
    class EqualsAndHashCodeTests extends EqualsAndHashCodeCaseInsensitiveTestFactory<SOA> {
        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected SOA getDefaultObjectDifferentCase1() throws ValidationException {
            return new SOA("GOOD.COM.", 123, "foo.a1.", "q_-1.",
                    4294967295L, 2147483648L, 2155872256L, 32896L, 2155905152L);
        }

        /**
         * Factory method for generating the second same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         * Defaults to getDefaultObjectDifferentCase1 (if only 1 field to test)
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected SOA getDefaultObjectDifferentCase2() throws ValidationException {
            return new SOA("good.com.", 123, "FOO.A1.", "q_-1.",
                    4294967295L, 2147483648L, 2155872256L, 32896L, 2155905152L);
        }

        /**
         * Factory method for generating a default object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected SOA getDefaultObject0() throws ValidationException {
            return new SOA("good.com.", 123, "foo.a1.", "q_-1.",
                    4294967295L, 2147483648L, 2155872256L, 32896L, 2155905152L);
        }

        /**
         * Factory method for generating a second object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected SOA getDefaultObject1() throws ValidationException {
            return new SOA("good.com.", 123, "foo.a1.", "q_-1.",
                    4294967295L, 2147483648L, 2155872256L, 32896L, 8421504L);
        }

        /**
         * Factory method for generating a third object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected SOA getDefaultObject2() throws ValidationException {
            return new SOA("good.com.", 123, "foo.a1.", "q_-1.",
                    4294967295L, 2147483648L, 8388608L, 32896L, 2155905152L);
        }

        /**
         * Factory method for generating a fourth object to test for (in)equality
         * Defaults to getDefaultObject0
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected SOA getDefaultObject3() throws ValidationException {
            return new SOA("good.com.", 123, "foo.a1.", "q_-1.",
                    4294967295L, 2147483648L, 2155872256L, 32896L, 2155905152L);
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
        protected SOA getDefaultObject4() throws ValidationException {
            return new SOA("good.com.", 123, "foo.a1.", "q_-1.q.",
                    4294967295L, 2147483648L, 2155872256L, 32896L, 2155905152L);
        }

        /**
         * Factory method for generating a sixth object to test for (in)equality
         * Defaults to getDefaultObject2
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected SOA getDefaultObject5() throws ValidationException {
            return new SOA("good.com.", 379, "foo.a1.", "q_-1.",
                    4294967295L, 2147483648L, 2155872256L, 32896L, 2155905152L);
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
}
