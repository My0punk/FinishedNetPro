/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 3
 * Class: Data Communications
 *
 ************************************************/

package sdns.serialization;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Represents an MX RR
 *
 * @version 1.0
 */
public class MX extends ResourceRecord implements Cloneable{

    private String exchange;
    private int preference;
    private final int TYPE_VALUE = 15;

    /**
     * The constructor for MX
     * @param name the domain name of this RR
     * @param ttl the TTL of this RR
     * @param exchange the exchange domain name
     * @param preference the preference value of this RR
     * @throws ValidationException
     *      If there are any errors with TTL or domain names
     */
    public MX(String name, int ttl, String exchange, int preference) throws ValidationException {
        this.setTTL(ttl);
        this.setName(name);
        this.setExchange(exchange);
        this.setPreference(preference);
    }

    /**
     *  The polymorphic constructor for MX
     * @param name the RR name
     * @param ttl the ttl
     * @param RDLength the RDLength of the RData
     * @param in the input stream
     * @param bBuf the bytebuffer to work with
     * @throws IOException -
     *      If a read from the input stream fails
     * @throws ValidationException -
     *      If any given or read value is invalid
     */
    public MX(String name, int ttl, int RDLength, InputStream in, ByteBuffer bBuf) throws IOException, ValidationException {
        int preference;
        String exchange;
        bBuf.clear();
        bBuf= ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
        bBuf.put(in.readNBytes(2)); //read the preference
        if(bBuf.position() < 2) {
            throw new EOFException("Reached EOF when reading an MX preference");
        }
        bBuf.flip();
        preference = bBuf.getShort(); //will sign extend, mask in constructor

        exchange = readNameFromInput(in, RDLength-2); //subtract the two bytes read for preference
        this.setTTL(ttl);
        this.setName(name);
        this.setExchange(exchange);
        this.setPreference(preference & 0xFFFF);
        this.setRDLength(RDLength);
    }

    /**
     * gets the exchange value
     * @return the exchange value
     */
    public String getExchange() {
        return exchange;
    }

    /**
     * Gets the preference value
     * @return the preference
     */
    public int getPreference() {
        return preference;
    }

    /**
     * Sets the exchange domain
     * @param exchange the domain name of the exchange
     * @return this MX RR with the new exchange value
     * @throws ValidationException
     *      If the domain name is bad or null
     */
    public MX setExchange(String exchange) throws ValidationException {
        try {
            if (validateDomainName(exchange)) {
                this.exchange = Objects.requireNonNull(exchange, "Exchange cannot be null");
            } else {
                throw new ValidationException("Bad exchange domain name", exchange);
            }
        } catch(Exception e) {
            throw new ValidationException(e.getMessage(), e, exchange);
        }
        return this;
    }

    /**
     * Sets the preference value
     * @param preference the preference value
     * @return this MX RR with the new preference
     * @throws ValidationException
     *      If the preference is an invalid value
     */
    public MX setPreference(int preference) throws ValidationException {
        if(preference > 0xFFFF || preference < 0) {
            throw new ValidationException("Invalid preference: " + preference, Integer.toString(preference));
        }

        this.preference = preference & 0xFFFF;
        return this;
    }

    /**
     * Encodes the unique portion of the MX RR
     * @param out the output stream to send final data to
     * @param middleMan the middleman for byte splitting
     * @param encodeBuffer the buffer to keep putting result into
     * @throws IOException
     *      If there is an exception on writing to any buffers or streams
     */
    @Override
    protected void encodeData(OutputStream out, DataOutputStream middleMan, ByteArrayOutputStream encodeBuffer) throws IOException {
        int tempDataLength = 2 + (short)this.getExchange().length();

        //the exchange name starts on byte 3 so if it is more than 3 bytes that means that the name is more than just a dot.
        if(tempDataLength > 3) {
            tempDataLength ++;
        }

        middleMan.writeShort(tempDataLength); //RDLength
        middleMan.writeShort(this.getPreference());
        middleMan.flush();
        encodeName(encodeBuffer, this.getExchange());

        encodeBuffer.writeTo(out);
    }

    /**
     * Gets the Type of this RR
     * @return the TYPE as an int
     */
    @Override
    public int getTypeValue() {
        return this.TYPE_VALUE;
    }

    /**
     * Gives a string representation of this RR
     * @return a string representation of this RR
     */
    @Override
    public String toString() {
        return "MX: name=" + this.getName() + " ttl=" + this.getTTL() + " exchange=" + this.getExchange() +
                " preference=" + this.getPreference();
    }

    /**
     * Compares this MX RR and another object
     * @param o the object to comapre to
     * @return a bool showing whether they were equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MX mx = (MX) o;
        return getPreference() == mx.getPreference() &&
                TYPE_VALUE == mx.TYPE_VALUE &&
                getExchange().toLowerCase().equals(mx.getExchange().toLowerCase()) &&
                this.getTTL() == mx.getTTL() &&
                this.getName().toLowerCase().equals(mx.getName().toLowerCase());
    }

    /**
     * Calculates a hash value from this object
     * @return the hashvalue from this object as an int
     */
    @Override
    public int hashCode() {
        return Objects.hash(getExchange().toLowerCase(), getPreference(), TYPE_VALUE, this.getTTL(), this.getName().toLowerCase());
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
