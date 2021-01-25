//Contains the IPv6TestFactory class (see comments below)
//Created: 10/18/20
package sdns.serialization.test.factories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sdns.serialization.ValidationException;

import java.net.Inet6Address;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 * Makes an abstract class for testing anything with an IPv6
 */
public abstract class IPv6TestFactory {
    /**
     * Test valid IPs
     * @param ip IPs to test
     */
    @ParameterizedTest(name = "IPv6 = {0}")
    @ValueSource(strings = {"0000:0000:0000:0000:0000:0000:0000:0000", "0123:4567:89AB:CDEF:0123:4567:89AB:CDEF",
            "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF"})
    void testValidIPv6(String ip){
        try {
            Inet6Address n = (Inet6Address)Inet6Address.getByName(ip);
            assertEquals(n, setGetIPv6(n));
        } catch (UnknownHostException | ValidationException e) {
            fail();
        }
    }

    /**
     * Test throw null error with null IPv6
     */
    @Test @DisplayName("Null IPv6 Error")
    void testNullIPv6() {
        assertThrows(getNullThrowableType(), () -> setGetIPv6(null));
    }

    /**
     * Factory method for calling the appropriate function you want to test for IPv6 validity
     * @param ip ip to test
     * @return the result of a getIP on the respective object
     * @throws ValidationException if invalid object
     */
    protected abstract Inet6Address setGetIPv6(Inet6Address ip) throws ValidationException;

    /**
     * Allows the concrete class to specify which exception it wants to be thrown when a
     *   null IPv6 is passed to the function
     * @return class to throw
     */
    protected abstract Class<? extends Throwable> getNullThrowableType();
}
