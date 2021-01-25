//Contains the NIODeframerTest class (see comments below)
//Created: 11/15/20
package sdns.serialization.test;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.NIODeframer;
import sdns.serialization.test.factories.DeframerTestFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
public class NIODeframerTest {
    /**
     * Test get message
     */
    @Nested
    class TestGetMsg extends DeframerTestFactory{
        /**
         * Factory method for calling the appropriate function you want to test for deframing validity
         *
         * @param buff buffer to test
         * @return the message without the frame
         */
        @Override
        protected byte[] getNextMsg(byte[] buff) {
            return (new NIODeframer()).getMessage(buff);
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

        /**
         * Allows the concrete class to specify which exception it wants to be thrown when a
         * length-too-short buffer is passed to the function.
         *
         * @return if the function should throw when presented with a length that is too short
         */
        @Override
        protected boolean getShouldThrowTooShortLength() { return false; }

        /* ************** Start custom tests beyond factory ************** */
        /**
         * Runs a test where the input array is cut up into @param numPieces pieces and sent to framer
         * @param size size of buffer to test
         * @param numPieces number of pieces to cut the array into
         */
        void runPiecesTest(int size, int numPieces){
            //make byte array of size actual
            byte[] message = generateRandomByteArray(size);

            //Put message in buffer
            byte[] buff = putHeader(message, size);
            size += 2;

            NIODeframer deframer = new NIODeframer();
            //Send buffer to Framer and validate
            //Send message in numPieces pieces
            for(int i=0; i<numPieces-1; i++){
                assertNull(deframer.getMessage(Arrays.copyOfRange(buff, i*(size/numPieces), (i+1)*(size/numPieces))));
            }
            assertArrayEquals(message, deframer.getMessage(Arrays.copyOfRange(buff, (numPieces-1)*(size/numPieces), size)));
        }

        /**
         * Test buffering of messages until you have enough
         * @param size size of buffer to make
         */
        @ParameterizedTest(name = "2 pieces: Size: {0}")
        @ValueSource(ints = {0, 1, 10, 18, 20, 100, 255, 256, 65535})
        void validHalfAndHalfInsert(int size){
            runPiecesTest(size, 2);
        }

        /**
         * Test buffering of messages until you have enough
         * @param size size of buffer to make
         */
        @ParameterizedTest(name = "100 pieces: Size: {0}")
        @ValueSource(ints = {0, 1, 10, 18, 20, 100, 255, 256, 65535})
        void valid100PiecesInsert(int size){
            runPiecesTest(size, 100);
        }

        /**
         * Insert at the same time the array twice and see if we can pull both back out
         * @param size size of buffer to make
         */
        @ParameterizedTest(name = "Double insert: Size: {0}")
        @ValueSource(ints = {0, 1, 10, 18, 20, 100, 255, 256, 65535})
        void validDoubleInsert(int size){
            //make byte array of size actual
            byte[] message = generateRandomByteArray(size);

            //Put message in buffer
            byte[] buff = putHeader(message, size);
            size += 2;//account for header size

            byte[] buffDouble = new byte[size*2];
            System.arraycopy(buff, 0, buffDouble, 0, size);
            System.arraycopy(buff, 0, buffDouble, size, size);

            NIODeframer deframer = new NIODeframer();
            assertArrayEquals(message, deframer.getMessage(buffDouble));
            assertArrayEquals(message, deframer.getMessage(new byte[0]));
        }

        /**
         * Insert at the same time the array and then the array with 100 extra bytes on the end and
         *   see if we can pull both back out
         * @param size size of buffer to make
         */
        @ParameterizedTest(name = "Double insert different arrays: Size: {0}")
        @ValueSource(ints = {0, 1, 10, 18, 20, 100, 255, 256, 65535})
        void validDoubleInsertDifferent(int size){
            int size2 = (size+100) % 65535;//keep it within the bounds of an unsigned short

            //make byte array of size actual
            byte[] message = generateRandomByteArray(size);
            byte[] message2 = generateRandomByteArray(size2);

            //Put message in buffer
            byte[] buff = putHeader(message, size);
            byte[] buff2 = putHeader(message2, size2);
            size += 2;//account for header size
            size2 += 2;//account for header size

            byte[] buffDouble = new byte[size+size2];
            System.arraycopy(buff, 0, buffDouble, 0, size);
            System.arraycopy(buff2, 0, buffDouble, size, size2);

            NIODeframer deframer = new NIODeframer();
            assertArrayEquals(message, deframer.getMessage(buffDouble));
            assertArrayEquals(message2, deframer.getMessage(new byte[0]));
        }
    }
}
