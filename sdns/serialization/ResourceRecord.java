/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 0
 * Class: Data Communications
 *
 ************************************************/

package sdns.serialization;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Represents a Resource Record.
 *
 * @version 1.0
 */
public abstract class ResourceRecord implements Cloneable{
    //add member vars when needed.
    private String name;

    //This needs to be treated as unsigned
    //32bit unsigned int
    private int ttl;

    //16-bit unsigned int
    //used in the unknown type to make each unknown instance more unique
    private int RDLength;

    /**
     * Deserializes the RR from an Input source
     *
     * @param in the input source
     * @return a new Resource Record
     * @throws ValidationException
     *      If parse or validation problem
     * @throws IOException
     *      If I/O problem
     */
    public static ResourceRecord decode(InputStream in) throws ValidationException, IOException {
        String readName = null; //the domain name
        int readTtl; //ttl
        int readRDLength; //rdlength
        short type; //RR type
        byte[] RData = null; //whatever is found in the rdata field

        //helpful buffer
        ByteBuffer bBuf;

        if(in == null) {
            throw new NullPointerException("Input cannot be null");
        }

        try {
            readName = readNameFromInput(in);

            //use byte buffer as an aid
            bBuf = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
            bBuf.put((byte)in.read()).put((byte)in.read());
            bBuf.rewind();
            type = bBuf.getShort();

            //check if the bytes read had any premature EoS indicators.
            if(type < 0) {
                throw new EOFException();
            }

            //read the padding bytes
            if(in.read() == -1) {
                throw new EOFException();
            }
            int secondPadByteCheck = in.read();
            if(secondPadByteCheck == -1) {
                throw new EOFException();
            }
            else if(secondPadByteCheck != 1) {
                throw new ValidationException("bad padding bytes", Integer.toString(secondPadByteCheck));
            }

            //read ttl
            bBuf.clear();
            bBuf = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
            for(int i = 0; i < 4; i++) {
                bBuf.put((byte)in.read());
            }
            bBuf.rewind();
            readTtl = bBuf.getInt();

            //get RDLength
            bBuf.clear();
            bBuf = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
            bBuf.put((byte)in.read()).put((byte)in.read());
            bBuf.rewind();
            readRDLength = bBuf.getShort();

            switch (type) {
                case 2 -> {
                    return new NS(readName, readTtl, readRDLength, in);
                }
                case 5 -> {
                    return new CName(readName, readTtl, readRDLength, in);
                }
                case 1 -> {
                    return new A(readName, readTtl, readRDLength, in);
                }
                case 28 -> {
                    return new AAAA(readName, readTtl, readRDLength, in);
                }
                case 15 -> {
                    return new MX(readName, readTtl, readRDLength, in, bBuf);
                }
                case 257 -> {
                    return new CAA(readName, readTtl, readRDLength, in);
                }
                case 6 -> {
                    return new SOA(readName, readTtl, readRDLength, in, bBuf);
                }
                default -> {
                    Unknown whoGoesThere = new Unknown();
                    readUnknownRData(in, readRDLength);
                    whoGoesThere.setType_Value(type);
                    if (readTtl > -1) {
                        whoGoesThere.setTTL(readTtl);
                    } else {
                        throw new ValidationException("TTL out of Range", Integer.toString(readTtl));
                    }
                    whoGoesThere.setName(readName);
                    whoGoesThere.setRDLength(readRDLength);
                    return whoGoesThere;
                }
            }

        }catch(IOException e) {
            if(e instanceof EOFException) {
                throw new EOFException(e.getMessage());
            } else if(e instanceof UnknownHostException) {
                throw new ValidationException(e.getMessage(), e.getCause(), "IP received was bad");
            }
            throw new IOException(e.getMessage());
        }
    }

    /**
     * encode to be used by all resource record types. dispatches to the correct
     * subclass to encode RData
     *
     * @param out the output stream to send the data to
     * @throws IOException
     */
    public void encode(OutputStream out) throws IOException {
        if(out == null) {
            throw new NullPointerException("Output cannot be null");
        }

        ByteArrayOutputStream encodeBuffer = new ByteArrayOutputStream();
        encodeName(encodeBuffer, this.getName());

        //use dataOutputStream as a middle-layer to do byte splitting
        DataOutputStream middleMan = new DataOutputStream(encodeBuffer);

        //encode type as 2 bytes
        middleMan.writeShort((short)this.getTypeValue());

        //encode buffer bytes
        middleMan.writeShort(1);
        middleMan.writeInt(this.getTTL());

        this.encodeData(out, middleMan, encodeBuffer);
    };

    /**
     * Reads the RData portion for an unknown type while deserializing
     *
     * @param in the input stream with the data
     * @param rdlength the expected length of the data
     * @throws IOException
     *      If there is a premature EOS
     */
    private static void readUnknownRData(InputStream in, int rdlength) throws IOException{
        for(int i = 0; i < rdlength; i++) {
            int data = in.read();
            if(data < 0) {
                throw new EOFException("Premature EOS");
            }
        }
    }

    /**
     * reads name from input source
     *
     * @param in input source
     * @return a string of the name
     * @throws IOException
     */
    public static String readNameFromInput(InputStream in) throws IOException, ValidationException {
        return readNameFromInput(in, -69);
    }

    /**
     * reads name from input source and makes sure that the data read was as many bytes as told
     *
     * @param in input source
     * @param rdlength the rdlength
     * @return a string of the name
     * @throws IOException
     *      If an I/O error occurs
     * @throws ValidationException
     *      If rdlength does not match the counted bytes
     */
    public static String readNameFromInput(InputStream in, int rdlength) throws IOException, ValidationException{
        //count how many bytes are read
        int byteCount = 0;
        byte nextByte; //treat between 0 and 255
        int toCount = -1;
        List<Byte> chars = new ArrayList<>(); //array to put bytes read into.

        do {
            //read the number to count ahead first
            toCount = in.read();
            byteCount++;

            //if toCount is already -1, just throw an exception
            if(toCount < 0) {
                throw new EOFException("Reached end of stream");
            }
            if(toCount == 0) {
                break;
            }

            if(highTwoBitCheck((byte)toCount)) {
                in.read();
                byteCount++;
                break;
            } else {
                for (int i = 0; i < toCount; i++) {
                    nextByte = (byte)in.read();
                    if(nextByte <= 0) { //TODO: change to only accept between -1 and 0 as an early eof. Need to provide better detection to fail if unicode is handed to me.
                        //throw eof if 0 or -1 is encountered before finishing the read.
                        throw new EOFException("Reached end of stream");
                    }
                    chars.add(nextByte);
                    byteCount++;
                }
                chars.add((byte)46);
            }

        } while(toCount > 0);

        if(chars.size() == 0) {
            chars.add((byte)46);
        }

        if(rdlength != -69 && rdlength < byteCount) {
            throw new ValidationException("RDLength is less than the byte count of RData.", null);
        }
        else if(rdlength != -69 && rdlength > byteCount) {
            throw new EOFException();
        }

        //take the bytes read and put them into a byte array
        var intermediary = new byte[chars.size()];

        //possibly remove
        if(chars.size() > 255) {
            throw new ValidationException("too many characters in name", new String(intermediary, StandardCharsets.US_ASCII));
        }

        for(int i= 0; i < chars.size(); i++) {
            intermediary[i] = chars.get(i);
        }
        return new String(intermediary, StandardCharsets.US_ASCII);
    }

    /**
     * checks the top two bits of a byte value to see if they are set. (util for finding end of name)
     * @param countValueToCheck the byte to check
     * @return a boolean.
     */
    private static boolean highTwoBitCheck(byte countValueToCheck) {
        return (countValueToCheck & (byte)0xC0) == (byte)-64;
    }

    /**
     * For polymorphic behavior on encoding the unique portion of a resource record.
     * Must be implemented by all RR types
     *
     * @param out the output stream to send final data to
     * @param middleMan the middleman for byte splitting
     * @param encodeBuffer the buffer to keep putting result into
     * @throws IOException
     *         Self-explanitory
     */
    protected abstract void encodeData(OutputStream out, DataOutputStream middleMan,
                                    ByteArrayOutputStream encodeBuffer) throws IOException;

    /**
     * the getter for type value that all resource record types must implement.
     *
     * @return a long of the type value.
     */
    public abstract int getTypeValue();

    /**
     * get domain name
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * set the name
     *
     * @param name the domain name
     * @return this RR with the new name
     * @throws ValidationException - if new name invalid or null
     */
    public ResourceRecord setName(String name) throws ValidationException {
        if(validateDomainName(name)) {
            this.name = Objects.requireNonNull(name, "Name must be a non-null");
        }else {
            throw new ValidationException("Bad Domain Name", name);
        }
        return this;
    }

    /**
     * get TTL
     *
     * @return returns the TTL as an int
     */
    //Make sure to remember when using the value returned here that you need to treat it as unsigned.
    public int getTTL() {
        return (int)this.ttl;
    }

    /**
     * set a new TTL
     *
     * @param ttl
     * @return this RR with a new  TTL
     * @throws ValidationException
     *      If the ttl is invalid
     */
    public ResourceRecord setTTL(int ttl) throws ValidationException {
        if(ttl > -1) {
            this.ttl = ttl;
        }else {
            throw new ValidationException("TTL out of allowed range", Integer.toString(ttl));
        }
        return this;
    }

    /**
     * get the RDLength value for serialization
     *
     * @return the RDLength as an int but bitmasked to 16-bits
     */
    protected int getRDLength() {
        return this.RDLength & 0xFFFF;
    }

    /**
     * sets the RDLength
     *
     * @param RDLength the RDLength retrieved during decode
     * @return this RR with a new RDLength
     * @throws ValidationException
     *      If rd length is invalid
     */
    protected ResourceRecord setRDLength(int RDLength) throws ValidationException{
        if(RDLength > 65535) {
            throw new ValidationException("RDLength Exceeds size limit", Integer.toString(RDLength));
        }
        if(RDLength < 0) {
            throw new ValidationException("RDLength exceeds size limit", Integer.toString(RDLength));
        }
        this.RDLength = RDLength & 0xFFFF; //using bitmask to make sure that it is 16-bits
        return this;
    }

    /**
     * Helper to encode domain names
     *
     * @param buf the byte array output stream buffer to put the name in
     */
    protected static void encodeName(ByteArrayOutputStream buf, String toEncode) {
        String[] domainName;

        if(toEncode.length() == 1 && toEncode.contains(".")) {
            buf.write(0);
            return;
        }
        //get each part between the '.'
        domainName = toEncode.split("\\.");

        //for every label, get the length and write it, and write each character of the label.
        for(String label : domainName) {
            buf.write(label.length()); //write length of label
            for(int i= 0; i < label.length(); i++) {
                buf.write((byte)label.charAt(i)); // write char at index in string
            }
        }
        buf.write((byte)0); //terminate with the 0
    }

    /**
     * Checks the validity of a domain name
     *
     * @param nameToCheck the name to validate
     * @return a boolean for whether or not it passed validation
     */
    public static boolean validateDomainName(String nameToCheck) {
        if(nameToCheck == null) {
            return false;
        }
        if(nameToCheck.isBlank()) {
            return false;
        }

        //Checks if the name contains 2 '.' at the end.
        if(nameToCheck.matches(".*[.]{2,}.*$")) {
            return false;
        }
        if(nameToCheck.length() == 1 && nameToCheck.contains(".")) {
            return true;
        } else if(nameToCheck.length() > 255) {
            return false;
        }
        if(nameToCheck.charAt(nameToCheck.length()-1) == '.') {
            int lengthBeforeCheck;
            ArrayList<String> tokenized = new ArrayList<>(Arrays.asList(nameToCheck.split("\\.")));
            lengthBeforeCheck = tokenized.size();
            int lengthAfter = (int) tokenized.stream().filter(e -> {
                if (e.length() > 63) {
                    return false;
                }
                String domainRegex = "^(?=[a-zA-Z])[a-zA-Z0-9-_]*([a-zA-Z0-9])$";
                Pattern domainPattern = Pattern.compile(domainRegex);
                Matcher verify = domainPattern.matcher(e);
                boolean test = verify.matches();
                return test;
            }).count();
            return lengthBeforeCheck == lengthAfter;
        }
        return false;
    }

    /**
     * Clones this object
     * @return a clone of this object
     * @throws CloneNotSupportedException
     *      If clone isn't supported
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return (ResourceRecord)super.clone();
    }
}
