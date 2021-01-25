//Contains the DeframerTestFactory class (see comments below)
//Created: 11/15/20
package sdns.serialization.test.factories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.EOFException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything deframing a byte array (see TCP protocol specifications)
 *   ONLY TESTS VALID AND NULL CASES
 */
public abstract class DeframerTestFactory {
    /**
     * Generates a random byte array for testing (it's a detectable pattern because tests should be consistent)
     * @param size size of array to make
     * @return random byte array
     */
    protected byte[] generateRandomByteArray(int size){
        byte[] message;
        switch(size){
            case 18: message = new byte[]{3, 'f', 'o', 'o', 0,
                    0, 5,//type
                    0, 1,//0x0001
                    0, 0,//ttl
                    0, 5,//rdlen
                    3, 'f', 'o', 'o', 0
            };
                assertEquals(18, message.length);
                break;
            default:
                message = new byte[size];
                for(int i=0;i<size;i++){
                    message[i] = (byte) (255 - (i%256));//any valid byte from 255 -> 0
                }
        }
        return message;
    }

    /**
     * Puts a 2-byte, unsigned, big-endian int size in front of the array
     * @param message message to prepend a size to
     * @param size    size to put (DOESN'T HAVE TO BE THE ACTUAL SIZE OF THE MESSAGE)
     * @return a byte[] with 2 bytes of size prepended to message
     */
    protected byte[] putHeader(byte[] message, int size){
        //Put length header in message
        byte[] buff = new byte[message.length + 2];
        buff[0] = (byte) (size >> 8);
        buff[1] = (byte) (size);
        if(size > 0){
            System.arraycopy(message, 0, buff, 2, message.length);
        }
        return buff;
    }

    /**
     * Test lots of types of values
     *  (18 is a sentinel value to the random byte generator to generate a CName RR)
     * @param size size of the array to generate/test
     */
    @ParameterizedTest(name = "Size: {0}")
    @ValueSource(ints = {0, 1, 10, 18, 20, 100, 255, 256, 65535})
    void testValidArray(int size){
        //make byte array of size actual
        byte[] message = generateRandomByteArray(size);

        //Put message in buffer
        byte[] buff = putHeader(message, size);

        //Send buffer to Framer and validate
        try {
            assertArrayEquals(message, this.getNextMsg(buff));
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Test length at beginning being longer than actual array length
     * @param given value to prepend to array (has to be bigger than actual)
     * @param actual the actual size of the array to generate (has to be smaller than given)
     */
    @ParameterizedTest(name = "Too short:: given: {0}, actual: {1}")
    @CsvSource({"10,9", "1,0", "256,255", "100,0", "-1,32"})
    void testTooShort(int given, int actual){
        //make byte array of size actual
        byte[] message = generateRandomByteArray(actual);

        //Put message in buffer
        byte[] buff = putHeader(message, given);

        //Send buffer to Framer
        if(getShouldThrowTooShortLength()){
            assertThrows(EOFException.class, () -> this.getNextMsg(buff));
        } else {
            try {
                assertNull(this.getNextMsg(buff));
            } catch (IOException e) {
                fail();
            }
        }
    }


    /**
     * Test throw null error with null buffer
     */
    @Test @DisplayName("Null buffer error")
    void testNullBuffer() { assertThrows(getNullThrowableType(), () -> getNextMsg(null)); }

    /**
     * Factory method for calling the appropriate function you want to test for deframing validity
     * @param buff buffer to test
     * @return the message without the frame
     */
    protected abstract byte[] getNextMsg(byte[] buff) throws IOException;

    /**
     * Allows the concrete class to specify which exception it wants to be thrown when a
     *   null buffer is passed to the function
     * @return class to throw
     */
    protected abstract Class<? extends Throwable> getNullThrowableType();

    /**
     * Allows the concrete class to specify which exception it wants to be thrown when a
     *   length-too-short buffer is passed to the function.
     * @return if the function should throw when presented with a length that is too short
     */
    protected boolean getShouldThrowTooShortLength(){ return true; }
}
