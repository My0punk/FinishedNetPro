package sdns.serialization;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CAA extends ResourceRecord implements Cloneable{
    private String issuer;

    private final int TYPE_VALUE = 257;

    /**
     * CAA RR Constructor
     *
     * @param name the domain name associated with this RR
     * @param ttl the ttl for this RR
     * @param issuer the issuer name
     * @throws ValidationException
     *      If there is a bad TTL, domain name, or issuer string
     */
    public CAA(String name, int ttl, String issuer) throws ValidationException{
        this.setTTL(ttl);
        this.setName(name);
        this.setIssuer(issuer);
    }

    /**
     * The polymorphic constructor for CAA
     * @param name the RR name
     * @param ttl the ttl
     * @param RDLength the RDLength of the RData
     * @param in the input stream
     * @throws IOException -
     *      If there is an error reading from the input stream
     * @throws ValidationException -
     *      If any given or read values are invalid
     */
    public CAA(String name, int ttl, int RDLength, InputStream in) throws IOException, ValidationException {
        String issuer;
        int lengthOfIssuer = RDLength - 7;

        //byte padding
        int paddingHalf1 = in.read(); //needs to be 0
        int paddingHalf2 = in.read();
        if(paddingHalf1 != 0 || paddingHalf2 != 5) {
            throw new ValidationException("Padding bytes were not as expected: " + paddingHalf1 + " and " + paddingHalf2 , "");
        }

        //char padding
        byte[] issueCharPadding = in.readNBytes(5);
        String charPaddingString = new String(issueCharPadding, StandardCharsets.US_ASCII);
        if(!charPaddingString.equals("issue")) {
            throw new ValidationException("Char Padding does not read as 'issue'. String assembled: " + charPaddingString, charPaddingString);
        }

        //issuer string
        issuer = new String(in.readNBytes(lengthOfIssuer), StandardCharsets.US_ASCII);
        if(issuer.length() != lengthOfIssuer) {
            throw new ValidationException("Issuer is too short", issuer);
        }
        this.setName(name);
        this.setTTL(ttl);
        this.setIssuer(issuer);
        this.setRDLength(RDLength);
    }

    /**
     * Gets the issuer string value
     * @return the issuer
     */
    public String getIssuer() {
        return this.issuer;
    }

    /**
     * Sets the issuer string
     * @param issuer a string containing the issuer name
     * @return this RR
     * @throws ValidationException
     *      If issuer is null or invalid.
     */
    public CAA setIssuer(String issuer) throws ValidationException {
        if(issuer == null) {
            throw new ValidationException("Issuer cannot be null", issuer);
        }
        for(int i = 0; i < issuer.length(); i ++) {
            char currChar = issuer.charAt(i);
            if(currChar < 0x21 || currChar > 0x7E) {
                throw new ValidationException("Bad issuer string", issuer);
            }
        }
        this.issuer = issuer;
        return this;
    }

    /**
     * For polymorphic behavior on encoding the unique portion of a resource record.
     * Must be implemented by all RR types
     *
     * @param out          the output stream to send final data to
     * @param middleMan    the middleman for byte splitting
     * @param encodeBuffer the buffer to keep putting result into
     * @throws IOException Self-explanitory
     */
    @Override
    protected void encodeData(OutputStream out, DataOutputStream middleMan, ByteArrayOutputStream encodeBuffer) throws IOException {
        int dataLength = 7 + this.getIssuer().length();

        middleMan.writeShort(dataLength);

        middleMan.writeShort(5); //covers the first 2 bytes of the RData
        middleMan.writeBytes("issue");
        middleMan.writeBytes(this.getIssuer());

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
     * Returns a string representation of this RR
     * @return a string containing a representation of this RR
     */
    @Override
    public String toString() {
        return "CAA: name=" + this.getName() + " ttl=" +getTTL() + " issuer=" + this.getIssuer();
    }

    /**
     * Compares this RR instance and another object
     * @param o the object to compare to
     * @return a bool whether or not they are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CAA caa = (CAA) o;
        return TYPE_VALUE == caa.TYPE_VALUE &&
                getIssuer().toLowerCase().equals(caa.getIssuer().toLowerCase()) &&
                getTTL() == caa.getTTL() &&
                getName().toLowerCase().equals(caa.getName().toLowerCase());
    }

    /**
     * Generates a hashCode from this RR instance
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(getIssuer().toLowerCase(), TYPE_VALUE, getName().toLowerCase(), getTTL());
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
