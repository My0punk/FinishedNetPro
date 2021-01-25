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
import java.util.*;

public class Response extends Message {
    private List<ResourceRecord> answers;
    private List<ResourceRecord> nameServers;
    private List<ResourceRecord> additionalRecords;

    /**
     * Constructor that finishes the decoding work
     *
     * @param id the id of this message
     * @param query the query value
     * @param header the header in a byte array
     * @param in the input stream to continue from
     * @throws ValidationException
     *      If any flags, the id, or query string, are invalid. Also on premature EOFs
     */
    protected Response(int id, String query, byte[] header, ByteArrayInputStream in) throws ValidationException{
        this(id,query,RCode.NOERROR); //RCode from deserialization is dealt with in this constructor but the super needs something so I just hand it NOERROR
        DataInputStream middleMan = new DataInputStream(new ByteArrayInputStream(header));

        try {
            middleMan.readShort(); //read the id that was already read. This is hacky but oh well.

            this.flagsFirstHalf = middleMan.readByte();

            //figure out RCode stuff in here, not before calling this constructor. Hacky but I don't want to rewrite what
            // works just fine while on limited time.
            this.setResponseCode(middleMan.readByte());

            if(!checkFlagValidity(this.flagsFirstHalf)) {
                throw new ValidationException("Un-permitted flags set " +flagsFirstHalf, "");
            }

            //this wraps the bytes in the header containing the counts into a bytebuffer
            ByteBuffer countBuf = ByteBuffer.wrap(header, 6, 6);

            //get counts. If any count is other than 0, throw validation exception
            this.ANCOUNT = countBuf.getShort();
            this.NSCOUNT = countBuf.getShort();
            this.ARCOUNT = countBuf.getShort();


            //Get all of the resource records.
            for (int i = 0; i < ANCOUNT; i++) {
                answers.add(ResourceRecord.decode(in));
            }
            for (int i = 0; i < NSCOUNT; i++) {
                nameServers.add(ResourceRecord.decode(in));
            }
            for (int i = 0; i < ARCOUNT; i++) {
                additionalRecords.add(ResourceRecord.decode(in));
            }
            if(in.available() > 0) {
                throw new ValidationException("too many bytes for response", "");
            }
        } catch (IOException e) {
            throw new ValidationException(e.getMessage(), "Bad RR or Header");
        }
    }

    /**
     * Basic Response constructor
     *
     * @param id the id of this response
     * @param query the query value
     * @param rcode the rcode.
     *              this is useless to me as I derive the rcode from deserializing it in the constructor since query and response
     *              treat the byte where rcode is found in different manners.
     * @throws ValidationException
     *      If the id or query are invalid
     */
    public Response(int id, String query, RCode rcode) throws ValidationException{
        this.setID(id);
        this.setQuery(query);
        this.answers = new ArrayList<>();
        this.nameServers = new ArrayList<>();
        this.additionalRecords = new ArrayList<>();
        this.setRCode(rcode);
    }

    /**
     * checks if the current flags are valid
     * @param flags a bit set containing the flags
     * @return a bool if it was valid or not
     */
    private boolean checkFlagValidity(byte flags) {
        //checks opcode for only containing 0s
        //makes sure the only things set for response are QR and maybe RD
        return flags <= -121;
    }

    //TODO: needs to be changed for server to properly provide a byte size containing the proper code.
    /**
     * gets the current response code
     * @return the current rcode
     */
    @Override
    public RCode getRCode() {
        return this.rCode;
    }

    /**
     * sets the response code from a given int(useful in deserialization
     * @param rcode the new response code
     * @return this
     * @throws ValidationException
     */
    protected Response setResponseCode(int rcode) throws ValidationException {
        //this checks if the value of the second byte of the flags is within range. when this is used on decode
        // Z is not permitted but RA is so I need to check values of a byte within the range of RA off and RA on.
        //THIS IS NOW POINTLESS BECUASE Z DOESNT MATTER ON DECODE
        if(((byte)rcode & 0xF) > 5) {
            throw new ValidationException("invalid rcode " + rcode, Integer.toString(rcode));
        }
        //mask the int so only the rcode bits are used.
        // Luckily, rcode is at the end so i don't have to extract it's value from the middle of the flag bytes.
        setRCode(RCode.getRCode((byte) (rcode & 0xF)));
        return this;
    }

    /**
     * sets the response code
     * @param rcode the RCode enum object to set rcode to
     * @return this
     * @throws ValidationException
     */
    public Response setRCode(RCode rcode) throws ValidationException {
        if(rcode == null) {
            throw new ValidationException("RCode object cannot be null", "");
        }
        this.rCode = rcode;
        return this;
    }

    /**
     * gets the list of answer RRs
     * @return a list of RRs
     */
    public List<ResourceRecord> getAnswerList() {
        return answers;
    }

    /**
     * adds an RR to the list of Answers
     * @param answer the RR to add
     * @return this
     * @throws ValidationException
     */
    public Response addAnswer(ResourceRecord answer) throws ValidationException {
        if(answer == null) {
            throw new ValidationException("Answer cannot be null", "Answer was null");
        }

        if(answers.contains(answer)) {
            return this;
        }
        try {
            this.answers.add((ResourceRecord) answer.clone());
            this.ANCOUNT++;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * gets the list of name server RRs
     * @return a list of name servers
     */
    public List<ResourceRecord> getNameServerList() {
        return nameServers;
    }

    /**
     * adds a name server to the list of name servers
     * @param nameServer the RR to add
     * @return this
     * @throws ValidationException
     */
    public Response addNameServer(ResourceRecord nameServer) throws ValidationException {
        if(nameServer == null) {
            throw new ValidationException("Name Server cannot be null", "Name Server was null");
        }
        if(nameServers.contains(nameServer)) {
            return this;
        }
        try {
            this.nameServers.add((ResourceRecord)nameServer.clone());
            this.NSCOUNT++;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * gets list of Additional RR
     * @return a list of Additional RRs
     */
    public List<ResourceRecord> getAdditionalList() {
        return additionalRecords;
    }

    /**
     * adds additional RR
     * @param additional the new RR
     * @return this
     * @throws ValidationException
     */
    public Response addAdditional(ResourceRecord additional) throws ValidationException {
        if(additional == null) {
            throw new ValidationException("additional RR cannot be null", "RR was null");
        }
        if(additionalRecords.contains(additional)) {
            return this;
        }
        try {
            this.additionalRecords.add((ResourceRecord)additional.clone());
            this.ARCOUNT++;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * The to string for Response
     * @return a string representation
     */
    public String toString() {
        return "Response: id=" + getID() + " query=" + getQuery() + " answers=" + getAnswerList().toString() +
                " nameservers=" + getNameServerList().toString() + " additionals=" + getAdditionalList().toString();
    }

    /**
     * sets flags for encoding according to concrete class type.
     * @return a bitset of the new flags
     */
    protected void setFlagsForEncoding() {
        this.flagsFirstHalf = -127;
    }

    /**
     * calculates final counts
     * @return an array of the counts
     */
    protected int[] getFinalCounts() {
        int[] counts = new int[3];
        counts[0] = getAnswerList().size();
        counts[1] = getNameServerList().size();
        counts[2] = getAdditionalList().size();
        return counts;
    }

    /**
     * Encodes the unique portion of the response
     * @param encodedHeader the header
     * @return a byte array with the header and the unique portion
     */
    @Override
    public byte[] encodeUnique(byte[] encodedHeader) {
        ByteArrayOutputStream responseFrame = new ByteArrayOutputStream();
        DataOutputStream middleMan = new DataOutputStream(responseFrame);
        responseFrame.writeBytes(encodedHeader);

        ByteArrayOutputStream listsStream = new ByteArrayOutputStream();
        this.ANCOUNT = getAnswerList().size();
        this.NSCOUNT = getNameServerList().size();
        this.ARCOUNT = getAdditionalList().size();
        try {
            //serialize all answer RRs
            for(ResourceRecord rr : getAnswerList()) {
                try {
                    rr.encode(listsStream);
                }catch(UnsupportedOperationException e) {
                    this.ANCOUNT--;
                }
            }
            //serialize all NS RRs
            for(ResourceRecord rr : getNameServerList()) {
                try {
                    rr.encode(listsStream);
                }catch(UnsupportedOperationException e) {
                    this.NSCOUNT--;
                }
            }
            //serialize all Additional RRs
            for(ResourceRecord rr : getAdditionalList()) {
                try {
                    rr.encode(listsStream);
                }catch(UnsupportedOperationException e) {
                    this.ARCOUNT--;
                }
            }

            //moved these down here because I needed to recalculate the counts if any RRs are unknown.
            //I no longer add the counts to the header in the parent encode. I add them in the child encode for the above reason.
            // I couldn't easily edit the bytes of the counts if they were already in the ByteArrayOutputBuffer
            //write the counts after serializing the lists
            middleMan.writeShort((short)this.ANCOUNT); //ANCOUNT
            middleMan.writeShort((short)this.NSCOUNT); //NSCOUNT
            middleMan.writeShort((short)this.ARCOUNT); //ARCOUNT

            //encode the question
            ResourceRecord.encodeName(responseFrame,this.getQuery());

            //encode the question padding
            responseFrame.writeBytes(new byte[]{(byte)0x00, (byte)0xFF});
            responseFrame.writeBytes(new byte[]{(byte)0x00, 1});

            responseFrame.write(listsStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseFrame.toByteArray();

    }

    /**
     * Equals for response objects
     * @param o the object being compared to
     * @return a bool determining if the two objects were equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return this.getQuery().toLowerCase().equals(response.getQuery().toLowerCase()) &&
                this.getID() == response.getID() &&
                this.getAnswerList().equals(response.getAnswerList()) &&
                this.getNameServerList().equals(response.getNameServerList()) &&
                this.getAdditionalList().equals(response.getAdditionalList()) &&
                this.getRCode().getRCodeValue() == response.getRCode().getRCodeValue();
    }

    /**
     * HashCode for response objects
     * @return a hash of this response object as an int
     */
    @Override
    public int hashCode() {
        return Objects.hash(answers, nameServers, additionalRecords, this.getQuery().toLowerCase(), this.getID(), this.getRCode());
    }
}
