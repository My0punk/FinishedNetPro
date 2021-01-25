//Contains the Unknown testing class (see comments below)
//Created: 9/8/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sdns.serialization.*;
import sdns.serialization.test.factories.EqualsAndHashCodeCaseInsensitiveTestFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

//Note: rerun all tests with different character encodings (UTF-32, UTF-16, etc) at runtime
/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
class UnknownTest {
    //The type values for various subtypes (CName, NS, A, AAAA, MX)
    protected static final int CN_TYPE_VALUE = 5;
    protected static final int NS_TYPE_VALUE = 2;
    protected static final int A_TYPE_VALUE = 1;
    protected static final int AAAA_TYPE_VALUE = 28;
    protected static final int MX_TYPE_VALUE = 15;

    /**
     * Verifies a RR
     * @param rr RR to verify
     * @param name Correct name
     * @param ttl correct ttl
     * @param type correct type
     */
    private static void verifyRR(ResourceRecord rr, String name, int ttl, int type){
        Objects.requireNonNull(rr);
        assertEquals(name, rr.getName());
        assertEquals(ttl, rr.getTTL());
        assertEquals(type, rr.getTypeValue());
    }

    /**
     * Helper construct
     * @return Unknown with ttl = 321, name = ".", type = 8
     */
    private static Unknown constructUnknownDot321(){
        byte[] buff = { 0,
                0, 8,
                0, 1, //0x0001
                0, 0, 1, 65,
                0, 8,
                0, 0, 0, 0, 0, 0, 0, 0};
        Unknown temp = constructUnknown(buff);
        verifyRR(temp, ".", 321, 8);
        return temp;
    }

    /**
     * Helper construct
     * @return Unknown with ttl = 123, name = "good.com.", type = 2056
     */
    private static Unknown constructUnknownGood123(){
        return constructUnknownGood123(2056);
    }

    /**
     * Helper construct
     * @return Unknown with ttl = 123, name = "good.com.", type = ttl
     */
    private static Unknown constructUnknownGood123(int type){
        byte[] buff = { 4, 'g', 'o', 'o', 'd', 3, 'c', 'o', 'm', -64, 5,
                (byte) ((type >> 8) & 0x00FF), (byte) (type & 0x00FF),
                0, 1, //0x0001
                0, 0, 0, 123,
                0, 8,
                0, 0, 0, 0, 0, 0, 0, 0};
        Unknown temp = constructUnknown(buff);
        verifyRR(temp, "good.com.", 123, type);
        return temp;
    }

    /**
     * Decode an Unknown type to use from an array buffer
     * @param buff byte buffer
     * @return decoded Unknown object
     */
    private static Unknown constructUnknown(byte[] buff){
        ResourceRecord temp = null;
        try {
            temp = ResourceRecord.decode(new ByteArrayInputStream(buff));
        } catch (ValidationException | IOException e) {
            assert(false);
        }
        assertEquals(Unknown.class, temp.getClass());
        return (Unknown)temp;
    }

    /**
     * Test that it always throws an exception no matter what (varied input) (DONE)
     */
    @Test @DisplayName("Default encode fail")
    void test0() {
        Unknown ut = constructUnknownDot321();
        assertThrows(UnsupportedOperationException.class, () -> ut.encode(System.out));
    }

    /**
     * Test that it doesn't equal either of the two known values (DONE)
     */
    @Test @DisplayName("Assert that unknown type isn't any of the other types")
    void getTypeValue() {
        Unknown un = constructUnknownDot321();
        try {
            assertNotEquals(CN_TYPE_VALUE, un.getTypeValue());
            assertNotEquals(NS_TYPE_VALUE, un.getTypeValue());
            assertNotEquals(A_TYPE_VALUE, un.getTypeValue());
            assertNotEquals(AAAA_TYPE_VALUE, un.getTypeValue());
            assertNotEquals(MX_TYPE_VALUE, un.getTypeValue());
        } catch(Exception e){
            assert(false);
        }
    }

    /**
     * To string test (DONE)
     */
    @Test
    void testToString() {
        String expected = "Unknown: name=foo.com. ttl=500";
        try {
            byte[] buff = { 3, 'f', 'o', 'o', 3, 'c', 'o', 'm', 0,
                    0, 116,
                    0, 1, //0x0001
                    0, 0, 1, -12,//500
                    0, 8,
                    0, 0, 0, 0, 0, 0, 0, 0};
            ResourceRecord test = ResourceRecord.decode(new ByteArrayInputStream(buff));
            verifyRR(test, "foo.com.", 500, 116);
            String output = test.toString();

            assertEquals(expected, output);
        } catch (ValidationException | IOException e) {
            fail();
        }
    }

    /**
     * Equals and hashcode tests (DONE)
     */
    @Nested
    class EqualsAndHashCode extends EqualsAndHashCodeCaseInsensitiveTestFactory<Unknown> {
        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Unknown getDefaultObjectDifferentCase1() throws ValidationException {
            return (Unknown) constructUnknownGood123().setName(constructUnknownGood123().getName().toUpperCase());
        }

        /**
         * Factory method for generating a default object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Unknown getDefaultObject0() throws ValidationException {
            return constructUnknownGood123();
        }

        /**
         * Factory method for generating a second object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Unknown getDefaultObject1() throws ValidationException {
            return (Unknown) constructUnknownGood123().setName("good.com.q.");
        }

        /**
         * Factory method for generating a third object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Unknown getDefaultObject2() throws ValidationException {
            return (Unknown) constructUnknownGood123().setTTL(379);
        }

        /**
         * Factory method for generating a fourth object to test for (in)equality
         * Defaults to getDefaultObject0
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Unknown getDefaultObject3() throws ValidationException {
            return constructUnknownGood123(2056);
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
        protected Unknown getDefaultObject4() throws ValidationException {
            return constructUnknownGood123(8);
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
            return new CName("good.com.", 123, ".");
        }
    }
}