//Contains the FramerTest class (see comments below)
//Created: 10/25/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.*;
import sdns.serialization.test.factories.DeframerTestFactory;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
public class FramerTest {
    /**
     * Test frame message
     */
    @Nested
    class TestFrameMessage {
        /**
         * Null check
         */
        @Test @DisplayName("Null error")
        void testNull(){
            assertThrows(NullPointerException.class, () -> Framer.frameMsg(null));
        }

        /**
         * Valid tests
         */
        @Nested
        class TestValidMessages {
            /**
             * Frames a byte array and validates that it was performed correctly
             * @param buff byte array to frame
             */
            void frameMessageAndValidate(byte[] buff){
                //Frame it
                byte[] framed = null;
                try {
                    framed = Framer.frameMsg(buff);
                } catch (ValidationException e) {
                    fail();
                }

                int lengthAdded = (framed[0] << 8) + (framed[1]);

                //Check length added was correct
                assertEquals(buff.length, lengthAdded);

                //Check the rest of the byte array was preserved
                assertArrayEquals(buff, Arrays.copyOfRange(framed, 2, framed.length));
            }

            /**
             * Basic test
             */
            @Test @DisplayName("Basic encoding test")
            void basicTest(){
                //Get encoded arr
                byte[] buff = { 1, 2, 3, 4, 5, 99, 'a', '!', '/', '\n', '\"'};
                frameMessageAndValidate(buff);
            }

            /**
             * Complex test with a response message
             */
            @Test @DisplayName("Complex Response test")
            void testComplexResponse(){
                Response r = null;
                try {
                    r = new Response(0, ".", RCode.NOERROR);
                    CName cn = new CName(".", 5, "good.com.");
                    CName cn2 = new CName(".", 6, "good.com.");
                    NS ns = new NS(".", 123, "good.server.");
                    A a = new A(".", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
                    r.addAnswer(cn);
                    r.addAnswer(ns);
                    r.addAnswer(cn2);
                    r.addAnswer(a);
                } catch (ValidationException | UnknownHostException e) {
                    fail();
                }

                //Get encoded arr
                byte[] buff = r.encode();
                frameMessageAndValidate(buff);
            }
        }

        /**
         * Invalid tests
         */
        @Nested
        class TestInvalidMessages {
            /**
             * Too long
             */
            @ParameterizedTest(name = "Test too long of a message: {0}")
            @ValueSource(ints = {65536, 70000, 1000000})
            void testTooLongMessage(){
                byte[] veryLongArray = new byte[65536];
                assertThrows(ValidationException.class, () -> Framer.frameMsg(veryLongArray));
            }
        }
    }

    /**
     * Test nextMsg
     */
    @Nested
    class TestNextMsg extends DeframerTestFactory {
        /**
         * Factory method for calling the appropriate function you want to test for deframing validity
         *
         * @param buff buffer to test
         * @return the message without the frame
         */
        @Override
        protected byte[] getNextMsg(byte[] buff) throws IOException {
            return Framer.nextMsg(new ByteArrayInputStream(buff));
        }

        /**
         * Allows the concrete class to specify which exception it wants to be thrown when a
         * null buffer is passed to the function
         *
         * @return class to throw
         */
        @Override
        protected Class<? extends Throwable> getNullThrowableType() {
            return NullPointerException.class;
        }
    }
}
