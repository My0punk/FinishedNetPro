/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 0
 * Class: Data Communications
 *
 ************************************************/

package sdns.serialization;

import java.io.*;
import java.util.Objects;

/**
 * Represents an NS and provides serialization.
 *
 * @version 1.0
 */
public class NS extends ResourceRecord implements Cloneable{
    private String nameServer;
    private final int TYPE_VALUE = 2;

    /**
     * The NS constructor using name, ttl, and nameServer
     *
     * @param name the RR name
     * @param ttl the time to live for this RR
     * @param nameServer the NSDName from the RData field of the decoded request
     * @throws ValidationException
     *      if validation fails, including null name or nameServer
     */
    public NS(String name, int ttl, String nameServer) throws ValidationException {
        this.setTTL(ttl);
        if (validateDomainName(name) && validateDomainName(nameServer)) {
            this.setName(name);
            this.setNameServer(nameServer); //validity is checked in method so no need to check here.
        } else {
            throw new ValidationException("Invalid name for name or nameServer", name + " or " + nameServer);
        }
    }

    /**
     * Polymorphic NS constructor for decode
     *
     * @param name the RR name
     * @param ttl the TTL for this RR
     * @param RDLength the RDLength of the related RData
     * @param in the input stream
     * @throws ValidationException -
     *      If any names or values are bad.
     * @throws IOException -
     *      If reading anything from the stream fails
     */
    public NS(String name, int ttl, int RDLength, InputStream in) throws ValidationException, IOException {
        String nameServer = readNameFromInput(in, RDLength);
        this.setRDLength(RDLength);
        this.setTTL(ttl);
        if (validateDomainName(name) && validateDomainName(nameServer)) {
            this.setName(name);
            this.setNameServer(nameServer); //validity is checked in method so no need to check here.
        } else {
            throw new ValidationException("Invalid name for name or nameServer", name + " or " + nameServer);
        }
    }

    /**
     * Equals for NS
     *
     * @param o the RR to compare to
     * @return if it was equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NS ns = (NS) o;
        return TYPE_VALUE == ns.TYPE_VALUE &&
                getNameServer().toLowerCase().equals(ns.getNameServer().toLowerCase()) &&
                this.getTTL() == ns.getTTL() &&
                this.getName().toLowerCase().equals(ns.getName().toLowerCase());
    }

    /**
     * Hashcode for NS
     *
     * @return a hashcode for this RR
     */
    @Override
    public int hashCode() {
        return Objects.hash(getNameServer().toLowerCase(), TYPE_VALUE, this.getName().toLowerCase(), this.getTTL());
    }

    /**
     * The encode for NS RR type
     *
     * @param out the OutputStream to output to
     * @throws IOException
     */
    @Override
    protected void encodeData(OutputStream out, DataOutputStream middleMan,
                              ByteArrayOutputStream encodeBuffer) throws IOException {
        //to be safe, get the length of the data if not provided.
        //this should include the length bytes for each label due to the periods in the data
        int tempDataLength = (short)this.getNameServer().length();

        //if the data isn't just a dot, compensate for the first label length byte
        if(tempDataLength > 1) {
            tempDataLength+=1;
        }

        middleMan.writeShort(tempDataLength);
        middleMan.flush();
        encodeName(encodeBuffer, this.getNameServer());

        //finish
        encodeBuffer.writeTo(out);
    }

    /**
     * gets the name server
     * @return the name server
     */
    public String getNameServer() {
        return this.nameServer;
    }

    /**
     * Set name server
     *
     * @param nameServer the new name server
     * @return this NS with new name server
     * @throws ValidationException
     *      if invalid name server, including null
     */
    public NS setNameServer(String nameServer) throws ValidationException {
        try {
            if(this.validateDomainName(nameServer)) {
                this.nameServer = Objects.requireNonNull(nameServer, "nameServer must be a non-null");
            } else {
                throw new ValidationException("Bad nameServer name", nameServer);
            }
        } catch (Exception e) {
            throw new ValidationException(e.getMessage(), e, nameServer);
        }
        return this;
    }

    /**
     * returns the type value of this concrete type
     *
     * @return the type value as a long
     */
    @Override
    public int getTypeValue() {
        return TYPE_VALUE;
    }

    /**
     * Returns a string representation
     *
     * @return the String representation
     */
    @Override
    public String toString() {
        return "NS: name=" + this.getName() + " ttl=" +this.getTTL() + " nameserver=" + this.getNameServer();
    }

    /**
     * Clones this object
     * @return a clone of this object
     * @throws CloneNotSupportedException
     *      If clone isn't supported
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
