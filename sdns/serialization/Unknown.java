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

public class Unknown extends ResourceRecord implements Cloneable{
    private int type_value;

    /**
     *encode. Will always throw unsupported operation exception
     *
     * @param out the output stream to send the data to
     */
    @Override
    public void encode(OutputStream out) {
        throw new UnsupportedOperationException("Unknown or unsupported record type");
    }

    /**
     * Useless in this class but was forced to implement.
     * always throws unsupported operation exception
     * Pray you never end up here.
     *
     * @param out the output stream to send final data to
     * @param middleMan the middleman for byte splitting
     * @param encodeBuffer the buffer to keep putting result into
     * @throws IOException
     */
    @Override
    protected void encodeData(OutputStream out, DataOutputStream middleMan, ByteArrayOutputStream encodeBuffer) throws IOException {
        throw new UnsupportedEncodingException("Unknown or unsupported record type. If you get here, god help you");
    }

    /**
     * gets the unknown type value
     *
     * @return the type value
     */
    public int getTypeValue() {
        return type_value;
    }

    /**
     * sets the type value
     *
     * @param type the type number to set
     */
    public void setType_Value(int type) {
        type_value = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unknown unknown = (Unknown) o;
        return type_value == unknown.type_value &&
                this.getName().toLowerCase().equals(unknown.getName().toLowerCase()) &&
                this.getTTL() == unknown.getTTL() &&
                this.getRDLength() == unknown.getRDLength();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type_value, this.getName().toLowerCase(), this.getTTL(), this.getRDLength());
    }

    public String toString() {
        return "Unknown: name=" + this.getName() + " ttl=" +this.getTTL();
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
