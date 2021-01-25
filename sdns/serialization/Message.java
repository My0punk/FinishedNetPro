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

public abstract class Message {
    private int messageID;


    //contains all the flags and codes of the second set of 2 bytes in the header.
    //protected BitSet flagsAndCodes;
    protected byte flagsFirstHalf;

    //All of the flags. Will be extracted into their respective vars upon deserialization
    protected RCode rCode;

    protected int ANCOUNT;
    protected int NSCOUNT;
    protected int ARCOUNT;

    private String queryDomain;

    /**
     * Decoding of a message. Will turn message
     * @param message
     * @return
     * @throws ValidationException
     */
    public static Message decode(byte[] message) throws ValidationException, NullPointerException{
        ByteArrayInputStream wrappedInput = new ByteArrayInputStream(message);

        //grab the expected header
        byte[] header = new byte[12];
        if(wrappedInput.read(header,0,12) < 12) {
            throw new ValidationException("Header contains too little bytes", "Bytes read: " + Integer.toString(header.length));
        }

        //get the id
        int tempID = (int)ByteBuffer.wrap(header).getShort(0) & 0xFFFF;

        //read the question
        String query = null;
        try {
            query = ResourceRecord.readNameFromInput(wrappedInput);
            if(wrappedInput.readNBytes(4).length < 4) {
                throw new EOFException("premature EOF on question");
            }
        } catch (IOException e) {
            if(e instanceof EOFException) {
                throw new ValidationException(e.getMessage(), e.getCause(), "query format was bad");
            }
        }

        //switches on if the QR bit is set or not
        //if the QR bit is 0, it will return 0. If the bit is 1 it will return a non zero number
        if ((header[2] & (1 << 7)) == 0) {
            if(wrappedInput.available() > 0) {
                throw new ValidationException("Too many bytes", "");
            }
            return new Query(tempID, query, header);
        } else {
            return new Response(tempID, query, header, wrappedInput);
        }
    }

    /**
     * Encodes the header which contains the same information for query and response
     *
     * @return the byte array of the encoded message
     */
    public byte[] encode() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream middleMan = new DataOutputStream(outputStream);


        try {
            middleMan.writeShort((short)this.getID());
            this.setFlagsForEncoding();

            //this isn't future proof but i need it to work so I just expect everything other
            middleMan.writeByte(this.getFlagsFirstHalf());
            middleMan.writeByte(this.getRCode().getRCodeValue()); //Z must be zero and so must RA so I only need to write RCode as a byte
            middleMan.writeShort(0x0001); //question count

            //removed count calculation here for reasons stated in Response

            return this.encodeUnique(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //returns just what has been written if an exception is thrown. This probably won't happen but i don't know what
        // else to do about this.
        return outputStream.toByteArray();
    }

    /**
     * Ensures flags are set properly before encoding
     */
    protected abstract void setFlagsForEncoding();

    /**
     * gets final count of each list of RRs for response
     * @return an int array containing the counts for each RR list
     */
    protected abstract int[] getFinalCounts();

    /**
     * made this abstract and moved it to message so I could use it in a polymorphic way in the message encode
     * before moving to the encodeUnique.
     *
     * @return a response code
     */
    public abstract RCode getRCode();

    /**
     * Encodes the unique portion of the message
     * @param currentHeader the encoded header
     * @return a byte array containing the header and the unique portion.
     */
    protected abstract byte[] encodeUnique(byte[] currentHeader);

    /**
     * Get the id of this message
     *
     * @return the id of this message
     */
    public int getID() {
        return this.messageID;
    }

    /**
     * Get the domain being queried
     *
     * @return the domain being queried
     */
    public String getQuery() {
        return this.queryDomain;
    }

    public byte getFlagsFirstHalf() {
        return this.flagsFirstHalf;
    }

    /**
     * Sets the id of this message
     *
     * @param id the id to be set
     * @return this Message with the new ID
     * @throws ValidationException
     *      If the id is invalid
     */
    public Message setID(int id) throws ValidationException {
        if(id > 65535 || id < 0) {
            throw new ValidationException("Id out of valid range", Integer.toString(id));
        }
        this.messageID = id & 0xFFFF; //ensure it is only in the first 2 bytes
        return this;
    }

    /**
     * Sets the queried domain value
     *
     * @param query the domain name being queried
     * @return this Message with the new query
     * @throws ValidationException
     *      If the queried domain name is invalid or null
     */
    public Message setQuery(String query) throws ValidationException {
        try {
            if (ResourceRecord.validateDomainName(query)) {
                this.queryDomain = Objects.requireNonNull(query, "Question cannot be null");
                return this;
            }
        }catch(NullPointerException e) {
            throw new ValidationException(e.getMessage(), query);
        }
        throw new ValidationException("Query domain invalid", query);
    }


}
