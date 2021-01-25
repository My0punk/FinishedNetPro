//Contains the IPv4TestFactory class (see comments below)
//Created: 10/18/20
package sdns.serialization.test.factories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.ValidationException;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything with an IPv4
 */
public abstract class IPv4TestFactory {
    /**
     * Test valid IPs
     * @param ip IPs to test
     */
    @ParameterizedTest(name = "IPv4 = {0}")
    @ValueSource(strings = {"0.0.0.0", "1.1.1.1", "255.255.255.255"})
    void testValidIPv4(String ip){
        try {
            Inet4Address n = (Inet4Address)Inet4Address.getByName(ip);
            assertEquals(n, setGetIPv4(n));
        } catch (UnknownHostException | ValidationException e) {
            fail();
        }
    }

    /**
     * Test throw null error with null IPv4
     */
    @Test
    @DisplayName("Null IPv4 Error")
    void testNullIPv4() {
        assertThrows(getNullThrowableType(), () -> setGetIPv4(null));
    }

    /**
     * Factory method for calling the appropriate function you want to test for IPv4 validity
     * @param ip ip to test
     * @return the result of a getIP on the respective object
     * @throws ValidationException if invalid object
     */
    protected abstract Inet4Address setGetIPv4(Inet4Address ip) throws ValidationException;

    /**
     * Allows the concrete class to specify which exception it wants to be thrown when a
     *   null IPv4 is passed to the function
     * @return class to throw
     */
    protected abstract Class<? extends Throwable> getNullThrowableType();
}
