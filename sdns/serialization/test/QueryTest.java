//Contains the Query testing class (see comments below)
//Created: 9/18/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.*;
import sdns.serialization.test.factories.DomainNameTestFactory;
import sdns.serialization.test.factories.EqualsAndHashCodeCaseInsensitiveTestFactory;
import sdns.serialization.test.factories.SdnsIDTestFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
class QueryTest {
    /**
     * Query constructor tests
     */
    @Nested
    class QueryConstructorTests {
        /**
         * Query constructor SDNS ID tests (valid and invalid)
         */
        @Nested
        class QueryConstructorIDTests extends SdnsIDTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for SDNS ID validity
             *
             * @param id id to test
             * @return the result of a getID on the respective object
             * @throws ValidationException if invalid SDNS ID
             */
            @Override
            protected int setGetID(int id) throws ValidationException {
                Message q = new Query(id, ".");
                return q.getID();
            }
        }

        /**
         * Query constructor query tests (valid and invalid)
         */
        @Nested
        class QueryConstructorQueryTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                Message q = new Query(0, dm);
                return q.getQuery();
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
     * Query toString basic test
     */
    @Test
    void testToString() {
        String expected = "Query: id=0 query=.";
        try {
            Query test = new Query(0, ".");
            String output = test.toString();

            assertEquals(expected, output);
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Query equals() and hashcode() tests
     */
    @Nested
    class EqualsAndHashCode extends EqualsAndHashCodeCaseInsensitiveTestFactory<Query> {
        /**
         * Factory method for generating a default object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Query getDefaultObject0() throws ValidationException {
            return new Query(123, "good.com.");
        }

        /**
         * Factory method for generating a second object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Query getDefaultObject1() throws ValidationException {
            return new Query(123, "good.com.q.");
        }

        /**
         * Factory method for generating a third object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Query getDefaultObject2() throws ValidationException {
            return new Query(379, "good.com.");//this makes 0000 0000 0111 1011 into 0000 0001 0111 1011
        }

        /**
         * Factory method for generating a SIMILAR object (to default0) of a different type to test for inequality
         * in types and hashcodes
         *
         * @return instantiation of different class object with similar field definitions
         * @throws ValidationException if invalid object
         */
        @Override
        protected Response getDifferentTypeObject() throws ValidationException {
            return new Response(123, "good.com.", RCode.NOERROR);
        }

        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Query getDefaultObjectDifferentCase1() throws ValidationException {
            return new Query(123, "GOOD.COM.");
        }
    }


    /**
     * Encode tests
     */
    @Nested
    class encodeTest {
        //Basic encode test
        @Test
        @DisplayName("Query valid encode basic")
        void basicEncodeTest() {
            byte[] queryExpect = {
                    0, 42,
                    1, 0,
                    0, 1,
                    0, 0,
                    0, 0,
                    0, 0,
                    3, 'f','o','o',3,'c','o','m',0,
                    0, -1,
                    0, 1
            };

            try {
                byte[] result;
                Query testQuery = new Query(42, "foo.com.");
                result = testQuery.encode();

                assertArrayEquals(queryExpect, result);
            } catch(Exception e) {
                fail("Exception Thrown", e);
            }
        }

        //Encoding with numbers and hyphens
        @Test
        @DisplayName("Query valid encode num and hyphens")
        void numAndHyphenQueryEncode() {
            byte[] queryExpect = {
                    0, 42,
                    1, 0,
                    0, 1,
                    0, 0,
                    0, 0,
                    0, 0,
                    3, 'f','-','9',3,'c','0','m',0,
                    0, -1,
                    0, 1
            };

            try {
                byte[] result;
                Query testQuery = new Query(42, "f-9.c0m.");
                result = testQuery.encode();
                assertArrayEquals(queryExpect, result);
            } catch(Exception e) {
                fail("Exception Thrown", e);
            }
        }
    }
}