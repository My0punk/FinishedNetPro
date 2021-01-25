//Contains the EqualsTestFactory class (see comments below)
//Created: 10/18/20
package sdns.serialization.test.factories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sdns.serialization.ValidationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * Makes an abstract class for testing anything with an Equals (so every class)
 * Guarantees: 0 and getDifferentTypeObject are similar but different classes
 *             3 and 4 are complex but slightly different
 *             0 and 1 are slightly different
 *             0 and 2 are slightly different (in a different way than 0 and 1)
 *             0 and 5 are slightly different (in a different way than previously)
 */
public abstract class EqualsAndHashCodeTestFactory<T> {
    /**
     * Test for inequality between different types
     */
    @Test @DisplayName("Equals different types")
    void testDifferentTypes(){
        try {
            assertNotEquals(this.getDefaultObject0(), this.getDifferentTypeObject());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for inequality with null
     */
    @Test @DisplayName("Equals null comparison")
    void testNull() {
        try {
            assertNotEquals(null, this.getDefaultObject0());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for complex equality
     */
    @Test @DisplayName("Equals complex objects")
    void testComplex(){
        try {
            T a = getDefaultObject0();
            assertEquals(getDefaultObject4(), getDefaultObject4());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for reflexive equality
     */
    @Test @DisplayName("Equals reflexive")
    void testReflexive(){
        try {
            T a = getDefaultObject0();
            assertEquals(a, a);
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for symmetric equality
     */
    @Test @DisplayName("Equals symmetric")
    void testSymmetric(){
        try {
            T a = getDefaultObject0(), b = getDefaultObject0();
            assertAll( () -> assertEquals(a, b), () -> assertEquals(b, a));
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for transitive equality
     */
    @Test @DisplayName("Equals transitive")
    void testTransitive(){
        try {
            T a = getDefaultObject0(), b = getDefaultObject0(), c = getDefaultObject0();
            assertAll( () -> assertEquals(a, b), () -> assertEquals(b, c), () -> assertEquals(c, a));
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for consistent equality
     */
    @Test @DisplayName("Equals transitive")
    void testConsistentEquals(){
        try {
            T a = getDefaultObject0(), b = getDefaultObject0();
            assertEquals(a, b);
            assertEquals(a, b);
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for inequality (object and hash code)
     */
    @Test @DisplayName("Inequality (0, 1) (object and hash code)")
    void testNotEquals(){
        try {
            T a = getDefaultObject0(), b = getDefaultObject1();
            assertNotEquals(a, b);
            assertNotEquals(a.hashCode(), b.hashCode());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for inequality (object and hash code)
     */
    @Test @DisplayName("Inequality (0, 2) (object and hash code)")
    void testNotEquals2(){
        try {
            T a = getDefaultObject0(), b = getDefaultObject2();
            assertNotEquals(a, b);
            assertNotEquals(a.hashCode(), b.hashCode());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for inequality (object and hash code)
     */
    @Test @DisplayName("Inequality (3, 4) (object and hash code)")
    void testNotEquals3(){
        try {
            T a = getDefaultObject3(), b = getDefaultObject4();
            assertNotEquals(a, b);
            assertNotEquals(a.hashCode(), b.hashCode());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for inequality (object and hash code)
     */
    @Test @DisplayName("Inequality (0, 5) (object and hash code)")
    void testNotEquals4(){
        try {
            T a = getDefaultObject0(), b = getDefaultObject5();
            assertNotEquals(a, b);
            assertNotEquals(a.hashCode(), b.hashCode());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for consistent hashcodes
     */
    @Test @DisplayName("Repeated x.hashCode -> same value")
    void testConsistentHashCode(){
        try {
            T a = getDefaultObject4();
            assertEquals(a.hashCode(), a.hashCode());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for equal hashcodes for equal objects
     */
    @Test @DisplayName("Equal objects -> equal hash codes")
    void testEqualHashCodes(){
        try {
            T a = getDefaultObject0(), b = getDefaultObject0();
            assertEquals(a.hashCode(), b.hashCode());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Test for inequal hashcodes for similar objects but different types
     */
    @Test @DisplayName("Non-equal objects prefer non-equal hash codes")
    void testUnequalHashCodesDifferentTypes(){
        try {
            T a = getDefaultObject0(), b = getDifferentTypeObject();
            assertNotEquals(a.hashCode(), b.hashCode());
        } catch(ValidationException e){
            fail();
        }
    }

    /**
     * Factory method for generating a default object to test for (in)equality
     * @return the default object for this class
     * @throws ValidationException if invalid object
     */
    protected abstract T getDefaultObject0() throws ValidationException;
    /**
     * Factory method for generating a second object to test for (in)equality
     * @return the default object for this class
     * @throws ValidationException if invalid object
     */
    protected abstract T getDefaultObject1() throws ValidationException;
    /**
     * Factory method for generating a third object to test for (in)equality
     * @return the default object for this class
     * @throws ValidationException if invalid object
     */
    protected abstract T getDefaultObject2() throws ValidationException;
    /**
     * Factory method for generating a fourth object to test for (in)equality
     * Defaults to getDefaultObject0
     *
     * @return the default object for this class
     * @throws ValidationException if invalid object
     */
    protected T getDefaultObject3() throws ValidationException { return getDefaultObject0(); }
    /**
     * Factory method for generating a fifth object to test for (in)equality
     * RESERVED FOR COMPLEX EQUALS
     * Defaults to getDefaultObject1
     *
     * @return the default object for this class
     * @throws ValidationException if invalid object
     */
    protected T getDefaultObject4() throws ValidationException { return getDefaultObject1(); }
    /**
     * Factory method for generating a sixth object to test for (in)equality
     * Defaults to getDefaultObject2
     *
     * @return the default object for this class
     * @throws ValidationException if invalid object
     */
    protected T getDefaultObject5() throws ValidationException { return getDefaultObject2(); }

    /**
     * Factory method for generating a SIMILAR object (to default0) of a different type to test for inequality
     *   in types and hashcodes
     * @param <V> different class than T
     * @return instantiation of different class object with similar field definitions
     * @throws ValidationException if invalid object
     */
    protected abstract <V> V getDifferentTypeObject() throws ValidationException;
}
