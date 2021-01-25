//Contains the DomainNameDecodeTestFactory class (see comments below)
//Created: 11/6/20
package sdns.serialization.test.factories;

import sdns.serialization.ValidationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything decoding with a domain name
 */
public abstract class DomainNameDecodeTestFactory extends DomainNameTestFactory {
    /**
     * Serializes the given domain name.  Does not check that name is a valid domain name.
     * @param name the domain name to validate
     * @param b the byte arraylist to append to
     */
    private static void serialize(String name, ArrayList<Byte> b) {
        if(name.length() == 1 && name.charAt(0) == '.'){
            b.add((byte)0);
            return;
        }

        if(name.length() == 0){
            return;
        }

        for(int i=0;i<name.length();i++){
            if((name.charAt(i) == '.' && i != name.length()-1) || i == 0){
                byte len = 0;
                //find how long next segment is
                for(int j=i+1;j<name.length();j++){
                    if(name.charAt(j) == '.'){
                        len = (byte)(j-i-1);
                        break;
                    }
                }
                if(i == 0 && name.length() != 1){
                    len++;
                }
                if(len == 0){//two periods in a row
                    b.add((byte)0);
                } else {// if(len > 0)
                    b.add(len);
                }

//                if(len == -1){//at the end without finding a period
//                    //just end the serialization
//                    break;
//                }

                if(i == 0){//insert the first letter
                    b.add((byte) name.charAt(0));
                }
            } else {
                if(i != name.length()-1){
                    b.add((byte) name.charAt(i));
                }
            }
        }

        //Add the final '0' to signal the end of the array
        b.add((byte)0);
    }

    /**
     * Factory method for calling the appropriate function you want to test for domain name validity
     *
     * @param dm domain name to test
     * @return the result of a getDM on the respective object
     * @throws ValidationException if invalid domain name
     */
    @Override
    protected String setGetDomainName(String dm) throws ValidationException {
        ArrayList<Byte> b = new ArrayList<>();
        serialize(dm, b);
        try{
            //convert to byte array
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            b.forEach(bout::write);
            return this.setGetDomainNameDecode(bout.toByteArray());
        } catch(IOException e){
            throw new ValidationException("IOException convered ValidationException for compatibility: " + e.getMessage(), e, "IOException");
        }
    }

    /**
     * Allows the concrete class to specify which exception it wants to be thrown when a
     * null string is passed to the function
     *
     * @return class to throw
     */
    @Override
    protected Class<? extends Throwable> getNullThrowableType() {
        return NullPointerException.class;
    }

    /**
     * Factory method for calling the appropriate function you want to test for domain name validity
     *   Extends setGetDomainName to be able to throw IOExceptions (in the case of decoding)
     *
     * @param dm domain name to test
     * @return the result of a getDM on the respective object
     * @throws ValidationException if invalid domain name
     * @throws IOException if io error
     */
    protected abstract String setGetDomainNameDecode(byte[] dm) throws ValidationException, IOException;
}
