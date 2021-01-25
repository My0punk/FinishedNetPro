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
 * Represents a CName and provides serialization for CName.
 *
 * @version 1.0
 */
public class CName extends ResourceRecord implements Cloneable{
    // add members as needed
    // The canonical name
    private String canonicalName;
    //The type value constant
    private final int TYPE_VALUE = 5;

    /**
     * CName constructor using name, ttl, and canonicalName
     *
     * @param name the domain name
     * @param ttl the ttl
     * @param canonicalName the canonical name
     * @throws ValidationException
     *      If any names are invalid or the TTL is out of range.
     */
    public CName(String name, int ttl, String canonicalName) throws ValidationException {
        this.setTTL(ttl);
        if(validateDomainName(name) && validateDomainName(canonicalName)) {
            this.setName(name);
            this.setCanonicalName(canonicalName);
        } else {
            throw new ValidationException("Bad Name or Canonical Name", name +" or " + canonicalName);
        }

    }

    /**
     * The polymorphic constructor for CName
     *
     * @param name the RR name
     * @param ttl the TTL
     * @param RDLength the RDLength of the associated RData
     * @param in the input stream
     * @throws ValidationException -
     *      If any values are invalid
     * @throws IOException -
     *      If there is an error reading from the stream
     */
    public CName(String name, int ttl, int RDLength, InputStream in) throws ValidationException, IOException {
        String canonicalName = readNameFromInput(in, RDLength);
        this.setRDLength(RDLength);
        this.setTTL(ttl);
        if(validateDomainName(name) && validateDomainName(canonicalName)) {
            this.setName(name);
            this.setCanonicalName(canonicalName);
        } else {
            throw new ValidationException("Bad Name or Canonical Name", name +" or " + canonicalName);
        }
    }

    /**
     * Equals for CName
     *
     * @param o the RR to compare to
     * @return if it was equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CName cName = (CName) o;
        return TYPE_VALUE == cName.TYPE_VALUE &&
                getCanonicalName().toLowerCase().equals(cName.getCanonicalName().toLowerCase()) &&
                this.getTTL() == cName.getTTL() &&
                this.getName().toLowerCase().equals(cName.getName().toLowerCase());
    }

    /**
     * Hashcode for CName
     *
     * @return a hashcode for this RR
     */
    @Override
    public int hashCode() {
        return Objects.hash(getCanonicalName().toLowerCase(), TYPE_VALUE, this.getName().toLowerCase(), this.getTTL());
    }

    /**
     * For polymorphic behavior on encoding the unique portion of a resource record.
     *
     * @param out the output stream to send final data to
     * @param middleMan the middleman for byte splitting
     * @param encodeBuffer the buffer to keep putting result into
     * @throws IOException
     *         if a EOS is reached
     */
    @Override
    protected void encodeData(OutputStream out, DataOutputStream middleMan,
                              ByteArrayOutputStream encodeBuffer) throws IOException {
        //to be safe, get the length of the data if not provided.
        //this should include the length bytes for each label due to the periods in the data
        int tempDataLength = (short)this.getCanonicalName().length();

        //if the data isn't just a dot, compensate for the first label length byte
        if(tempDataLength > 1) {
            tempDataLength+=1;
        }

        middleMan.writeShort(tempDataLength);
        middleMan.flush();
        encodeName(encodeBuffer, this.getCanonicalName());

        //finish
        encodeBuffer.writeTo(out);
    }

    /**
     * gets the canonical name
     * @return the canonical domain name
     */
    public String getCanonicalName() {
        return this.canonicalName;
    }

    /**
     *
     * @param canonicalName the canonical name to be set
     *
     * @return this RR with a new canonicalName
     * @throws ValidationException
     *       If the canonical name is invalid or null
     */
    public CName setCanonicalName(String canonicalName) throws ValidationException {
        try {
            if (validateDomainName(canonicalName)) {
                this.canonicalName = Objects.requireNonNull(canonicalName, "Canonical Name cannot be null");
            } else {
                throw new ValidationException("Bad Canonical Name", canonicalName);
            }
        } catch(Exception e) {
            throw new ValidationException(e.getMessage(), e, canonicalName);
        }
        return this;
    }

    /**
     * returns the type value of the concrete type
     *
     * @return the type value as a long
     */
    @Override
    public int getTypeValue() {
        return TYPE_VALUE;
    }

    /**
     * Gives a string representation of this RR
     *
     * @return a string representation in the format of
     *      "CName: name=<name> ttl=<ttl> canonicalname=<canonicalName>"
     */
    public String toString() {
        return "CName: name=" + this.getName() + " ttl=" + this.getTTL() + " canonicalname=" + this.getCanonicalName();
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
