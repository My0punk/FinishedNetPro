//Contains the CNameTest class (see comments below)
//Created: 9/8/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.*;
import sdns.serialization.test.factories.DomainNameTestFactory;
import sdns.serialization.test.factories.EqualsAndHashCodeCaseInsensitiveTestFactory;
import sdns.serialization.test.factories.TTLTestFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
//Note: rerun all tests with different character encodings (UTF-32, UTF-16, etc) at runtime
/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
class CNameTest {
    //CName type value
    private final long CN_TYPE_VALUE = 5L;

    /**
     * test get type value (trivial) (DONE)
     */
    @Test @DisplayName("Get type value")
    void getTypeValue() {
        CName cn;
        try {
            cn = new CName(".", 32, ".");
            assertEquals(CN_TYPE_VALUE, cn.getTypeValue());
        } catch(Exception e){
            assert(false);
        }
    }

    /**
     * Test to string (DONE)
     */
    @Test @DisplayName("ToString")
    void testToString() {
        String expected = "CName: name=foo.com. ttl=42 canonicalname=thisIsABoringName.";
        try {
            CName test = new CName("foo.com.", 42, "thisIsABoringName.");
            String output = test.toString();

            assertEquals(expected, output);
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Canonical name setter and getter tests (DONE)
     */
    @Nested
    class CanonicalNameSetterGetter extends DomainNameTestFactory {
        /**
         * Factory method for calling the appropriate function you want to test for domain name validity
         *
         * @param dm domain name to test
         * @return the result of a getDM on the respective object
         * @throws ValidationException if invalid domain name
         */
        @Override
        protected String setGetDomainName(String dm) throws ValidationException {
            CName cn = null;
            try {//shouldn't fail here
                cn = new CName(".", 0, ".");
            } catch(ValidationException e){
                fail();
            }
            cn.setCanonicalName(dm);
            return cn.getCanonicalName();
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
     * Constructor tests (valid and invalid) (DONE)
     */
    @Nested
    class CNameConstructorTests {
        /**
         * CName constructor name tests
         */
        @Nested
        class CNameConstructorNameTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                CName cn = new CName(dm, 0, ".");
                return cn.getName();
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
         * CName constructor canonical name tests
         */
        @Nested
        class CNameConstructorCanonicalNameTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                CName cn = new CName(".", 0, dm);
                return cn.getCanonicalName();
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
         * CName constructor TTL tests
         */
        @Nested
        class CNameConstructorTTLTests extends TTLTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for TTL validity
             *
             * @param ttl ttl to test
             * @return the result of a getTTL on the respective object
             * @throws ValidationException if invalid object
             */
            @Override
            protected int setGetTTL(int ttl) throws ValidationException {
                CName cn = new CName(".", ttl, ".");
                return cn.getTTL();
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
    class EncodeTests {
        ByteArrayOutputStream testStream = new ByteArrayOutputStream();

        @Test
        void basicEncodeTest() {
            byte[] comparisonArr = {3, 102, 111, 111, 3, 99, 111, 109, 0,
                    0, 5,
                    0, 1,
                    0, 0, 0, 42,
                    0, 15,
                    13, 'c','a','n','o','n','i','c','a','l','N','a','m','e',0};
            int byteArrayBufferSize;
            try {
                CName testCName = new CName("foo.com.", 42, "canonicalName.");
                testCName.encode(testStream);
                byte[] dumpArray = testStream.toByteArray();
                testStream.reset();
                assertArrayEquals(comparisonArr, dumpArray);
            } catch (ValidationException | IOException e) {
                fail();
            }
        }

        @Test
        void encodeNullOutStreamTest() {
            try {
                CName testCName = new CName("foo.com.", 42, "canonicalName.");
                assertThrows(NullPointerException.class, () -> testCName.encode(null));
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }

        @Test
        void encodeTestNumbersAndHyphens() {
            byte[] comparisonArr = {3, 'f', '-', '9', 0,
                    0, 5,
                    0, 1,
                    0, 0, 0, 42,
                    0, 15,
                    13, 'c','a','n','o','n','i','c','a','l','N','a','m','e',0};
            int byteArrayBufferSize;
            try {
                CName testCName = new CName("f-9.", 42, "canonicalName.");
                testCName.encode(testStream);
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
    class EqualsAndHashCode extends EqualsAndHashCodeCaseInsensitiveTestFactory<CName> {
        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CName getDefaultObjectDifferentCase1() throws ValidationException {
            return new CName("GOOD.COM.", 0, "foo.a1.");
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
        protected CName getDefaultObjectDifferentCase2() throws ValidationException {
            return new CName("good.com.", 0, "FOO.A1.");
        }

        /**
         * Factory method for generating a default object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CName getDefaultObject0() throws ValidationException {
            return new CName("good.com.", 0, "foo.a1.");
        }

        /**
         * Factory method for generating a second object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CName getDefaultObject1() throws ValidationException {
            return new CName("good.com.q.", 0, "foo.a1.");
        }

        /**
         * Factory method for generating a third object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CName getDefaultObject2() throws ValidationException {
            return new CName("good.com.", 0, "foo.a1.q.");
        }

        /**
         * Factory method for generating a fourth object to test for (in)equality
         * Defaults to getDefaultObject0
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected CName getDefaultObject3() throws ValidationException {
            return new CName("good.com.", 123, "foo.a1.");
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
        protected CName getDefaultObject4() throws ValidationException {
            return new CName("good.com.", 379, "foo.a1.");
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
            return new NS("good.com.", 0, "foo.a1.");
        }
    }
}

