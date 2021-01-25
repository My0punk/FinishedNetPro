/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 3
 * Class: Data Communications
 *
 ************************************************/


package sdns.serialization;

public enum RCode {
    NOERROR, FORMATERROR, SERVERFAILURE, NAMEERROR, NOTIMPLEMENTED, REFUSED;

    /**
     * gets the RCode object value from the int equivalent
     * @param rcodevalue the int of the RCode
     * @return the java enum of the RCode value
     * @throws ValidationException
     *      If the RCode number is not a valid RCode
     */
    public static RCode getRCode(int rcodevalue) throws ValidationException {
        switch (rcodevalue) {
            case 0 -> {
                return RCode.NOERROR;
            }
            case 1 -> {
                return RCode.FORMATERROR;
            }
            case 2 -> {
                return RCode.SERVERFAILURE;
            }
            case 3 -> {
                return RCode.NAMEERROR;
            }
            case 4 -> {
                return RCode.NOTIMPLEMENTED;
            }
            case 5 -> {
                return RCode.REFUSED;
            }
            default -> {
                throw new ValidationException("rcode value given is out of valid range.", Integer.toString(rcodevalue));
            }
        }
    }

    /**
     * Gets the message associated with the RCode this is called on.
     * @return the associated RCode message
     */
    public String getRCodeMessage() {
        switch (this) {
            case NOERROR -> {
                return "No error condition";
            }
            case FORMATERROR -> {
                return "The name server was unable to interpret the query";
            }
            case SERVERFAILURE -> {
                return "The name server was unable to process this query due to a problem with the name server";
            }
            case NAMEERROR -> {
                return "The domain name referenced in the query does not exist";
            }
            case NOTIMPLEMENTED -> {
                return "The name server does not support the requested kind of query";
            }
            case REFUSED -> {
                return "The name server refuses to perform the specified operation";
            }
            default -> {
                return "You should never get this. If you do, I don't know how you got here and something has gone horribly wrong " +
                        "or you might be low on CPU fluid";
            }
        }
    }

    /**
     * Gets the associated int value of the RCode this is called on
     * @return the int of the RCode
     */
    public int getRCodeValue() {
        switch (this) {
            case NOERROR -> {
                return 0;
            }
            case FORMATERROR -> {
                return 1;
            }
            case SERVERFAILURE -> {
                return 2;
            }
            case NAMEERROR -> {
                return 3;
            }
            case NOTIMPLEMENTED -> {
                return 4;
            }
            case REFUSED -> {
                return 5;
            }
            default ->{
                return -1;
            }
        }
    }

}
