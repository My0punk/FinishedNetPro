/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 1
 * Class: Data Communications
 *
 ************************************************/

package sdns.serialization;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * The Query message type
 *
 * @version 1.0
 */
public class Query extends Message {

    protected Query(int id, String query, byte[] header) throws ValidationException{
        this(id,query);
        DataInputStream middleMan = new DataInputStream(new ByteArrayInputStream(header));
        try {
            middleMan.readShort();
            this.flagsFirstHalf = middleMan.readByte();

            //gets the byte and checks to see if the first flag is set. If so, flip it off because we don't care about RA
            // on deserialization and need the rest of the byte for the RCode
            byte tempRCode = middleMan.readByte();
            if(tempRCode < 0) {
                tempRCode ^= (1 << 7);
            }
            //clear Z since we don't care on deserialization
            if(tempRCode > 15) {
                tempRCode ^= (1 << 6);
                tempRCode ^= (1 << 5);
                tempRCode ^= (1 << 4);
            }
            if(tempRCode > 0) {
                throw new ValidationException("The response code must be 0 for a query", Byte.toString(tempRCode));
            }
            this.rCode = RCode.getRCode(tempRCode);

            //this wraps the bytes in the header containing the counts into a bytebuffer
            ByteBuffer countBuf = ByteBuffer.wrap(header, 4, 8);

            //check the question count
            if(countBuf.getShort() != 0x0001) {
                throw new ValidationException("Question count bytes cannot be anything other than 0x0001", "");
            }

            //get counts. If any count is other than 0, throw validation exception
            this.ANCOUNT = countBuf.getShort();
            this.NSCOUNT = countBuf.getShort();
            this.ARCOUNT = countBuf.getShort();

            //If any of these are not 0 then throw a validation exception
            if((ANCOUNT != 0) || (NSCOUNT != 0) || (ARCOUNT != 0)) {
                throw new ValidationException("The AN/NS/AR counts cannot be a value other than 0 for queries",
                        "ARCOUNT="+ ANCOUNT +" NSCOUNT=" + NSCOUNT + " ARCOUNT=" + ARCOUNT);
            }

            //if this fails, it means only the opcode was bad.
            if(!checkQueryFlagValidity(this.flagsFirstHalf)) {
                throw new ValidationException("Opcode contained non-permitted values for a query", Byte.toString(this.flagsFirstHalf));
            }
        } catch (IOException e) {
            throw new ValidationException("error getting header values", "bad header");
        }
    }

    /**
     * The query constructor
     *
     * @param id the id of this query
     * @param query the question being asked
     * @throws ValidationException
     *      if the ID or query are invalid
     */
    public Query(int id, String query) throws ValidationException {
        //These methods verify the id and query when setting these values.
        this.setID(id);
        this.setQuery(query);
    }

    //probably don't need this but i'm just trying to get stuff to work.
    public RCode getRCode() {
        return RCode.NOERROR;
    }

    /**
     * checks if the opcode in the first half of the flags is valid
     * @param flags the flags to check
     * @return a bool
     */
    private boolean checkQueryFlagValidity(byte flags) {
        //checks opcode for only containing 0s
        return flags >= 0 && flags <= 7;
    }

    /**
     * Gives a string representation of this query
     *
     * @return the string representation
     */
    public String toString() {
        return "Query: id=" + this.getID() + " query=" + getQuery();
    }

    /**
     * Gives a hashcode of this instance
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getID(), this.getQuery().toLowerCase());
    }

    /**
     * Compares an instance of this object against another object
     *
     * @param o the object to compare with
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return this.getID() == query.getID() &&
                this.getQuery().toLowerCase().equals(query.getQuery().toLowerCase());
    }

    //this is breaking my encode and needs an overhaul
    @Override
    protected void setFlagsForEncoding() {
        this.flagsFirstHalf = 1;
    }

    @Override
    protected int[] getFinalCounts() {
        return new int[] {0,0,0};
    }

    /**
     * The encoding method for the unique portion of this message type
     *
     * @param encodedHeader the header that has already been encoded
     * @return a byte array containing the header and the newly encoded unique section
     */
    @Override
    protected byte[] encodeUnique(byte[] encodedHeader) {
//        encodedHeader[2] ^= (1<<7); //ensure that the QR bit is set to 0 for query
        ByteArrayOutputStream responseFrame = new ByteArrayOutputStream();
        DataOutputStream middleman = new DataOutputStream(responseFrame);
        responseFrame.writeBytes(encodedHeader);

        //this writes the Counts that are all supposed to be 0
        try {
            responseFrame.write(new byte[]{0,0,0,0,0,0});
        } catch (IOException e) {
            e.printStackTrace();
        }

        //encode the question
        ResourceRecord.encodeName(responseFrame,this.getQuery());

        //encode the question padding
        responseFrame.writeBytes(new byte[]{(byte)0x00, (byte)0xFF});
        responseFrame.writeBytes(new byte[]{(byte)0x00, 1});

        return responseFrame.toByteArray();
    }
}
