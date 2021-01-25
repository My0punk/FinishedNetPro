package sdns.serialization;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class SOA extends ResourceRecord implements Cloneable {

    private String mName;
    private String rName;
    private long serial;
    private long refresh;
    private long retry;
    private long expire;
    private long minimum;

    private final int TYPE_VALUE = 6;

    /**
     *  The normal constructor for SOA
     * @param name the domain name
     * @param ttl the ttl
     * @param mName the mName for this SOA
     * @param rName the rName for this SOA
     * @param serial the serial value
     * @param refresh the refresh time of this zone
     * @param retry the retry interval time
     * @param expire the expiry time for this zone
     * @param minimum the minimum TTL for any RR exported with this zone
     * @throws ValidationException if the TTL, name, mname, rname, or any other value is invalid or null
     */
    public SOA(String name, int ttl, String mName, String rName, long serial, long refresh, long retry, long expire, long minimum) throws ValidationException {
        if(ttl > -1) {
            this.setTTL(ttl);
        } else {
            throw new ValidationException("TTL out of valid range", Integer.toString(ttl));
        }
        if(validateDomainName(name) && validateDomainName(mName) && validateDomainName(rName)) {
            this.setName(name);
            this.setMName(mName);
            this.setRName(rName);
        } else {
            throw new ValidationException("Bad Domain name for name, mName, or rName", name);
        }

        this.setSerial(serial & 0xFFFFFFFFL);
        this.setRefresh(refresh & 0xFFFFFFFFL);
        this.setRetry(retry & 0xFFFFFFFFL);
        this.setExpire(expire & 0xFFFFFFFFL);
        this.setMinimum(minimum & 0xFFFFFFFFL);
    }

    /**
     * This is for polymorphic creation of SOA from decode.
     * Takes the input stream and bytebuf to finish reading RData.
     *
     * @param name the domain name of this RR
     * @param ttl this ttl for this RR
     * @param in the inputstream this RR is being read from
     * @param bBuf the bytebuffer to get integer values from the stream.
     */
    public SOA(String name, int ttl, int RDLength, InputStream in, ByteBuffer bBuf) throws ValidationException, IOException{
        this.setTTL(ttl);
        if(validateDomainName(name)) {
            this.setName(name);
        } else {
            throw new ValidationException("Bad Domain name", name);
        }
        this.setRDLength(RDLength);

        //just read the names straight from the stream. We don't know how long they are.
        this.setMName(readNameFromInput(in));
        this.setRName(readNameFromInput(in));

        //Get the 32-bit values for serial through minimum.
        bBuf.clear();
        bBuf = ByteBuffer.allocate(20).order(ByteOrder.BIG_ENDIAN);
        byte[] serialThroughMin = in.readNBytes(20);
        if(serialThroughMin.length < 20) {
            throw new EOFException("Reached end of stream while reading serial through minimum in SOA");
        }
        bBuf.put(serialThroughMin);
        bBuf.flip();

        //start reading the bytes out into ints that will be put into longs.
        // Don't forget to mask the high order bytes in case of sign extension.
        this.setSerial(bBuf.getInt() & 0xFFFFFFFFL);
        this.setRefresh(bBuf.getInt() & 0xFFFFFFFFL);
        this.setRetry(bBuf.getInt() & 0xFFFFFFFFL);
        this.setExpire(bBuf.getInt() & 0xFFFFFFFFL);
        this.setMinimum(bBuf.getInt() & 0xFFFFFFFFL);
    }

    /**
     * gets the value of expire
     * @return the value of expire
     */
    public long getExpire() {
        return expire;
    }

    /**
     * gets the value of MName
     * @return the string of MName
     */
    public String getMName() {
        return mName;
    }

    /**
     * gets the value of RName
     * @return the string value of RName
     */
    public String getRName() {
        return rName;
    }

    /**
     * gets the value of serial
     * @return the value of serial
     */
    public long getSerial() {
        return serial;
    }

    /**
     * gets the value of refresh
     * @return the value of refresh
     */
    public long getRefresh() {
        return refresh;
    }

    /**
     * gets the value of retry
     * @return the value of retry
     */
    public long getRetry() {
        return retry;
    }

    /**
     * gets the value of minimum
     * @return the value of minimum
     */
    public long getMinimum() {
        return minimum;
    }

    /**
     * Sets the value of MName
     * @param mName the value of MName. a domain name
     * @return this RR
     * @throws ValidationException if the given mName is an invalid domain name or null
     */
    public SOA setMName(String mName) throws ValidationException {
        try {
            if (validateDomainName(mName)) {
                this.mName = Objects.requireNonNull(mName, "mName cannot be null");
            } else {
                throw new ValidationException("Bad mName", mName);
            }
        } catch(Exception e) {
            throw new ValidationException(e.getMessage(), e, mName);
        }
        return this;
    }

    /**
     * Sets the value of RName
     * @param rName the value of RName. A domain name
     * @return this RR
     * @throws ValidationException if the given rName is an invalid domain name or null
     */
    public SOA setRName(String rName) throws ValidationException {
        try {
            if (validateDomainName(rName)) {
                this.rName = Objects.requireNonNull(rName, "mName cannot be null");
            } else {
                throw new ValidationException("Bad mName", rName);
            }
        } catch(Exception e) {
            throw new ValidationException(e.getMessage(), e, rName);
        }
        return this;
    }

    /**
     * Sets the value of serial
     * @param serial the value of serial. A 32-bit unsigned int
     * @return this RR
     * @throws ValidationException if serial is invalid
     */
    public SOA setSerial(long serial) throws ValidationException {
        //leave no room for error
        if(serial > 0xFFFFFFFFL || serial < 0) {
            throw new ValidationException("Invalid serial value. Must be a value within an unsigned 32-bit int", Long.toString(serial));
        }
        this.serial = serial & 0xFFFFFFFFL;
        return this;
    }

    /**
     * Sets the value of refresh
     * @param refresh the value of refresh. A 32-bit unsigned int
     * @return this RR
     * @throws ValidationException if refresh is invalid
     */
    public SOA setRefresh(long refresh) throws ValidationException {
        if(refresh > 0xFFFFFFFFL || refresh < 0) {
            throw new ValidationException("Invalid serial value. Must be a value within an unsigned 32-bit int", Long.toString(refresh));
        }
        this.refresh = refresh & 0xFFFFFFFFL;
        return this;
    }

    /**
     * Sets the value of retry
     * @param retry the value of retry. A 32-bit unsigned int
     * @return this RR
     * @throws ValidationException if retry is invalid
     */
    public SOA setRetry(long retry) throws ValidationException {
        if(retry > 0xFFFFFFFFL || retry < 0) {
            throw new ValidationException("Invalid serial value. Must be a value within an unsigned 32-bit int", Long.toString(retry));
        }
        this.retry = retry & 0xFFFFFFFFL;
        return this;
    }

    /**
     * Sets the value of Expire
     * @param expire the value of expire. A 32-bit unsigned int
     * @return this RR
     * @throws ValidationException if expire is invalid
     */
    public SOA setExpire(long expire) throws ValidationException {
        if(expire > 0xFFFFFFFFL || expire < 0) {
            throw new ValidationException("Invalid serial value. Must be a value within an unsigned 32-bit int", Long.toString(expire));
        }
        this.expire = expire & 0xFFFFFFFFL;
        return this;
    }

    /**
     * Sets the value of minimum
     * @param minimum the value of minimum. A 32-bit unsigned int
     * @return this RR
     * @throws ValidationException If minimum is invalid
     */
    public SOA setMinimum(long minimum) throws ValidationException {
        if(minimum > 0xFFFFFFFFL || minimum < 0) {
            throw new ValidationException("Invalid serial value. Must be a value within an unsigned 32-bit int", Long.toString(minimum));
        }
        this.minimum = minimum & 0xFFFFFFFFL;
        return this;
    }

    /**
     * For polymorphic behavior on encoding the unique portion of a resource record.
     * Must be implemented by all RR types
     *
     * @param out          the output stream to send final data to
     * @param middleMan    the middleman for byte splitting. should contain the encodeBuffer as it's underlying stream
     * @param encodeBuffer the buffer to keep putting result into
     * @throws IOException Self-explanitory
     */
    @Override
    protected void encodeData(OutputStream out, DataOutputStream middleMan, ByteArrayOutputStream encodeBuffer) throws IOException {
        int RDataCalculatedLen = 20; //start with the given 20 bytes for the unsigned ints
        if(this.getMName().equals(".")) {
            RDataCalculatedLen++;
        } else {
            RDataCalculatedLen += this.getMName().length() + 1;
        }
        if(this.getRName().equals(".")) {
            RDataCalculatedLen++;
        } else {
            RDataCalculatedLen += this.getRName().length() + 1;
        }

        //Encode RDLength and RData
        middleMan.writeShort(RDataCalculatedLen);
        middleMan.flush();
        encodeName(encodeBuffer, this.getMName());
        encodeName(encodeBuffer, this.getRName());

        middleMan.writeInt((int)this.getSerial());
        middleMan.writeInt((int)this.getRefresh());
        middleMan.writeInt((int)this.getRetry());
        middleMan.writeInt((int)this.getExpire());
        middleMan.writeInt((int)this.getMinimum());
        middleMan.flush();
        encodeBuffer.writeTo(out);
    }

    /**
     * the getter for type value that all resource record types must implement.
     *
     * @return a long of the type value.
     */
    @Override
    public int getTypeValue() {
        return TYPE_VALUE;
    }

    /**
     * Compares this SOA to another object
     * @param o the object to compare to
     * @return true or false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SOA soa = (SOA) o;
        return getSerial() == soa.getSerial() &&
                getRefresh() == soa.getRefresh() &&
                getRetry() == soa.getRetry() &&
                getExpire() == soa.getExpire() &&
                getMinimum() == soa.getMinimum() &&
                TYPE_VALUE == soa.TYPE_VALUE &&
                mName.toLowerCase().equals(soa.getMName().toLowerCase()) &&
                rName.toLowerCase().equals(soa.getRName().toLowerCase()) &&
                this.getName().toLowerCase().equals(soa.getName().toLowerCase()) &&
                this.getTTL() == soa.getTTL();
    }

    /**
     * generates a hashcode from this instance of SOA
     * @return an integer hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getName().toLowerCase(), this.getTTL(), mName.toLowerCase(), rName.toLowerCase(),
                getSerial(), getRefresh(), getRetry(), getExpire(), getMinimum(), TYPE_VALUE);
    }

    /**
     * the to string method for SOA
     * @return a string representation of this SOA RR
     */
    @Override
    public String toString() {
        return "SOA: name=" +this.getName() + " ttl=" + this.getTTL() + " mname=" + this.getMName() + " rname=" + this.getRName()
                + " serial=" + this.getSerial() + " refresh=" + this.getRefresh() + " retry=" + this.getRetry() + " expire="
                + this.getExpire() + " minimum=" + this.getMinimum();

    }

    /**
     * Clones this object
     *
     * @return a clone of this object
     * @throws CloneNotSupportedException If clone isn't supported
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
