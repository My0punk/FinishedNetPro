/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 4
 * Class: Data Communications
 *
 ************************************************/
package sdns.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

/**
 * Frames a message for TCP
 *
 * @version 1.0
 */
public class Framer {

    /**
     * frames a message for TCP by appending a 2 byte length field to the beginning
     * @param message the message to frame
     * @return a new byte array that contains the framed message.
     * @throws ValidationException
     *      It the message is too long or null
     */
    public static byte[] frameMsg(byte[] message) throws ValidationException{
        if(message == null) {
            throw new NullPointerException("Message was null");
        }
        if(message.length > 65535) {
            throw new ValidationException("Message too long, was over 65535 bytes", "");
        }

        short length = (short) message.length;

        //1.put the two bytes of the short containing the length into a new array with 2 extra spaces.
        //2.Add the two separate bytes of the short to the beginning of the array in network byte order.
        //3.copy the contents of the original array into the new array.
        byte[] framedMessage = new byte[length + 2];
        framedMessage[0] = (byte)((length >> 8) & 0xff);
        framedMessage[1] = (byte)(length & 0xff);
        System.arraycopy(message, 0, framedMessage, 2, length);

        return framedMessage;
    }

    /**
     * Deframes a tcp message from a tcp socket's input stream
     * @param in the tcp socket's input stream
     * @return a byte array containing the deframed message. Returns a null array if the peer closed the connection
     * @throws NullPointerException
     *      If the stream is null
     * @throws IOException
     *      If the amount read from the socket's input stream is less than what the frame said to expect,
     *      an EOFException is thrown
     */
    public static byte[] nextMsg(InputStream in) throws NullPointerException, IOException {
        if(in == null) {
            throw new NullPointerException("Input stream cannot be null");
        }

        //masks the first read to ensure it only gets a value within 1 byte, shifts it over 8, and ORs that result with
        // the next read that is also masked. This puts that next read into the low-order byte of the result.
        // it then fits this into an int.
        int firstRead = in.read();

        //returns null if the peer is closed.
        if(firstRead == -1) {
            return null;
        }
        int frameLength = (((firstRead & 0xFF) << 8) | (in.read() & 0xFF));
        byte[] unboxedData = in.readNBytes(frameLength);
        if(unboxedData.length != frameLength) {
            throw new EOFException("Data given was shorter than the frame size indicated");
        }

        return unboxedData;

    }
}
