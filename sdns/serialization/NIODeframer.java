/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 6
 * Class: Data Communications
 *
 ************************************************/

package sdns.serialization;

import java.util.Arrays;

public class NIODeframer {
    private byte[] buffer;
    private int bufferPos = 0; //the position of the next empty index to be filled
    private int firstFrameLength = -1; //-1 at start, will be calculated if available

    public NIODeframer() {
        buffer = new byte[40];
    }

    /**
     * Gets the next message. If there is no complete frame, returns null. If a completed frame exists in the buffer,
     *  return that.
     * @param buffer the data to put in the internal buffer
     * @return
     * @throws NullPointerException
     */
    public byte[] getMessage(byte[] buffer) throws NullPointerException{
        if(buffer == null) {
            throw new NullPointerException("input buffer cannot be null");
        }

        //grow the array if it can't fit the amount of bytes given
        if(buffer.length + bufferPos >= this.buffer.length) {
            growArray(buffer.length);
        }

        //put the given data into the buffer and add it's length to the position
        System.arraycopy(buffer, 0, this.buffer, bufferPos, buffer.length);
        bufferPos += buffer.length;

        //get the frame length if it hasn't been calculated yet
        if(bufferPos >= 2 && firstFrameLength == -1) {
            firstFrameLength = (((this.buffer[0] & 0xFF) << 8) | (this.buffer[1] & 0xFF));
        }

        //if the current position is larger than the length of the frame needed, return the frame and pack the buffer
        if((bufferPos > 2 && bufferPos >= firstFrameLength + 2) || firstFrameLength == 0) {
            byte[] fullFrame = new byte[firstFrameLength];
            System.arraycopy(this.buffer, 2, fullFrame, 0, firstFrameLength);
            if(bufferPos >= firstFrameLength + 2) {
                moveToFrntAfterFullFrame();
            } else {
                bufferPos = 0;
            }
            return fullFrame;
        }
        return null;
    }

    /**
     * Moves the next frame being built up to the front after a full frame has been completed and
     * is about to be returned
     */
    private void moveToFrntAfterFullFrame() {
        //pack the buffer in to the front
        System.arraycopy(this.buffer, firstFrameLength + 2,
                this.buffer, 0, this.bufferPos - (firstFrameLength + 2));

        //this gets the new position after packing the buffer to the front by
        // subtracting the size of the old frame and it's 2 byte length field from the old pos.
        this.bufferPos = this.bufferPos - (firstFrameLength + 2);

        //get the new next expected frame length if it's there
        if(bufferPos >= 2) {
            firstFrameLength = (((this.buffer[0] & 0xFF) << 8) | (this.buffer[1] & 0xFF));
        } else {
            firstFrameLength = -1;
        }
    }

    /**
     * Grows the internal buffer by the needed size * 1.5
     *
     * @param sizeNeeded the size needed to grow by.
     *                   This will typically be the length of the given buffer in the getMessage method
     */
    private void growArray(int sizeNeeded) {
        byte[] temp = new byte[this.buffer.length +(int) ((double)sizeNeeded * 1.5)];
        System.arraycopy(this.buffer, 0, temp, 0, this.buffer.length);
        this.buffer = temp;
    }
}
