/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 1
 * Class: Data Communications
 *
 ************************************************/

package sdns.serialization;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class A extends ResourceRecord implements Cloneable{
    private final int TYPE_VALUE = 1;
    private Inet4Address ipv4Addr;

    /**
     * The A RR constructor. Uses name, ttl, and and IPv4 address
     *
     * @param name the domain name
     * @param ttl the TTL of this RR
     * @param address the IPv4 address associated with this domain name
     * @throws ValidationException
     *      If the name is invalid or the ttl is out of range
     */
    public A(String name, int ttl, Inet4Address address) throws ValidationException {
        this.setTTL(ttl);
        this.setName(name);
        //I believe it is technically impossible to give an invalid ip here since
        //it takes an Inet4Address which validates the address upon creation of the object.
        this.setAddress(address);
    }

    /**
     * The polymorphic constructor for A
     * @param name the RR name
     * @param ttl the TTL
     * @param RDLength the RDLength of the RData
     * @param in the input stream
     * @throws IOException -
     *      If there is an error reading from the stream
     * @throws ValidationException -
     *      If any values given or read are invalid
     */
    public A(String name, int ttl, int RDLength, InputStream in) throws IOException, ValidationException {
        Inet4Address ipAddress;
        try {
            byte[] readAddr = in.readNBytes(RDLength);
            if(readAddr.length < 4) {
                throw new EOFException("addr too short");
            }
            ipAddress  = (Inet4Address) InetAddress.getByAddress(readAddr);
            this.setTTL(ttl);
            this.setRDLength(RDLength);
            this.setName(name);
            this.setAddress(ipAddress);
        } catch (IOException e) {
            if( e instanceof UnknownHostException) {
                throw new UnknownHostException(e.getMessage());
            }
            if(e instanceof EOFException) {
                throw e;
            }
        }
    }

    /**
     * Gets the IPv4 address associated with this A RR
     * @return the IPv4 address
     */
    public Inet4Address getAddress() {
        return this.ipv4Addr;
    }

    /**
     * Sets the IPv4 address associated with this RR
     * @param address the address to set
     * @return this RR but with the new address
     * @throws ValidationException
     *      If the address is invalid or null.
     */
    public A setAddress(Inet4Address address) throws ValidationException {
        if(address == null) {
            throw new ValidationException("IP cannot be null", null);
        }
        this.ipv4Addr = address;
        return this;
    }

    /**
     * returns a string representation of this RR
     * @return a string representation of this RR
     */
    public String toString() {
        return "A: name=" + this.getName() + " ttl=" + this.getTTL() + " address=" + this.getAddress().getHostAddress();
    }

    /**
     * the equals method for the A RR type
     * @param o the other object to compare to
     * @return a boolean, true if equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        A a = (A) o;
        return this.TYPE_VALUE == a.getTypeValue() &&
                ipv4Addr.equals(a.getAddress()) &&
                this.getTTL() == a.getTTL() &&
                this.getName().toLowerCase().equals(a.getName().toLowerCase());
    }

    /**
     * returns a hashcode of this instance
     * @return a hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(ipv4Addr, this.getTTL(), this.getName().toLowerCase(), this.TYPE_VALUE);
    }

    /**
     * A RR type's encodeData
     * *See ResourceRecord for the purpose of this method*
     *
     * @param out the output stream to send final data to
     * @param middleMan the middleman for byte splitting that uses the encodeBuffer as it's outputstream.
     * @param encodeBuffer the buffer to keep putting result into
     * @throws IOException
     *      If any write causes an IOException, this method will throw it up
     */
    @Override
    protected void encodeData(OutputStream out, DataOutputStream middleMan,
                                ByteArrayOutputStream encodeBuffer) throws IOException {

        middleMan.writeShort(4); //write the length of the data. 4 bytes since it is an ip
        middleMan.flush(); //flush to the BAOS
        encodeBuffer.write(this.ipv4Addr.getAddress()); //write the ip to the BAOS

        encodeBuffer.writeTo(out); //write the entire contents of the BAOS to the output
    }

    /**
     * Gets the type value of this RR
     *
     * @return the type value of this RR
     */
    @Override
    public int getTypeValue() {
        return TYPE_VALUE;
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
