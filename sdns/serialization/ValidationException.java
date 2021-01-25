/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 0
 * Class: Data Communications
 *
 ************************************************/

package sdns.serialization;

public class ValidationException extends Exception {

    private String badToken = null;

    public ValidationException(String message, String badToken) {
        super(message);
        this.badToken = badToken;
    }

    public ValidationException(String message, Throwable cause, String badToken) {
        super(message, cause);
        this.badToken = badToken;
    }

    public String getBadToken() {
        return badToken;
    }
}
