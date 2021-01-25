//Contains the ResponseTest class (see comments below)
//Created: 9/18/20
package sdns.serialization.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import sdns.serialization.*;
import sdns.serialization.test.factories.DomainNameTestFactory;
import sdns.serialization.test.factories.EqualsAndHashCodeCaseInsensitiveTestFactory;
import sdns.serialization.test.factories.RCodeSetGetAbstractTestFactory;
import sdns.serialization.test.factories.SdnsIDTestFactory;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ethan Dickey
 * @author Harrison Rogers
 */
class ResponseTest {
    /**
     * Response toString basic test
     */
    @Test
    void testToString() {
        String expected = "Response: id=500 query=ns.com. " +
                "answers=[CName: name=a.com. ttl=0 canonicalname=., A: name=b.com. ttl=0 address=0.0.0.0] " +
                "nameservers=[NS: name=ns.com. ttl=0 nameserver=ns1.com.] additionals=[]";
        try {
            Response test = new Response(500, "ns.com.", RCode.NOERROR);
            test.addAnswer(new CName("a.com.", 0, "."));
            test.addAnswer(new A("b.com.", 0, (Inet4Address)Inet4Address.getByName("0.0.0.0")));
            test.addNameServer(new NS("ns.com.", 0, "ns1.com."));

            String output = test.toString();

            assertEquals(expected, output);
        } catch (ValidationException | UnknownHostException e) {
            fail();
        }
    }

    /**
     * Test adder error conditions
     */
    @Nested
    class ResponseAddToListErrors {
        //Null add additional error
        @Test @DisplayName("Test add additional null")
        void testNullAddAdditional(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                assertThrows(ValidationException.class, () -> r.addAdditional(null));
            } catch (ValidationException e) {
                assert(false);
            }
        }

        //Null add answer error
        @Test @DisplayName("Test add answer null")
        void testNullAddAnswerList(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                assertThrows(ValidationException.class, () -> r.addAnswer(null));
            } catch (ValidationException e) {
                assert(false);
            }
        }

        //Null add name server error
        @Test @DisplayName("Test add name server null")
        void testNullAddNameServer(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                assertThrows(ValidationException.class, () -> r.addNameServer(null));
            } catch (ValidationException e) {
                assert(false);
            }
        }
    }

    /**
     * Provides lists to the duplicate tests
     */
    static class DuplicatesParamFactory implements ArgumentsProvider {
        /**
         * Provides lists to the duplicate tests
         * @param extensionContext unsure what this is
         * @return a stream of lists
         * @throws Exception if an exception occurs
         */
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            return Stream.of(Arguments.of(Arrays.asList(
                    new CName("x.y.", 5, "y.z."),
                    new CName("x.y.", 5, "y.z."),
                    new CName("x.y.", 6, "y.z."),
                    new CName("x.y.", 6, "y.z."),
                    new NS(".", 123, "good.server."),
                    new NS(".", 123, "good.server."),
                    new A(".", 123, (Inet4Address) Inet4Address.getByName("0.0.0.0")),
                    new A(".", 123, (Inet4Address) Inet4Address.getByName("0.0.0.0"))
            )), Arguments.of(Arrays.asList(
                    new CName("x.y.", 5, "y.z."),
                    new CName("x.y.", 6, "y.z."),
                    new CName("x.y.", 5, "y.z."),
                    new CName("X.Y.", 6, "y.z."),
                    new A("yurp.com.", 345, (Inet4Address) Inet4Address.getByName("1.2.3.4")),
                    new CName("foo.com.", 5, "foo.com."),
                    new CName("FOO.COM.", 5, "foo.com."),
                    new A("yurp.com.", 34, (Inet4Address) Inet4Address.getByName("1.2.3.4")),
                    new A("yurp.com.", 345, (Inet4Address) Inet4Address.getByName("1.2.3.4")),
                    new A("blurp.com.", 288, (Inet4Address) Inet4Address.getByName("1.2.3.4")),
                    new A("yurp.com.", 34, (Inet4Address) Inet4Address.getByName("1.2.3.4")),
                    new NS("gorp.net.", 4849, "yorp.net."),
                    new A("yurp.com.", 345, (Inet4Address) Inet4Address.getByName("1.2.3.4")),
                    new A("yurp.com.", 34, (Inet4Address) Inet4Address.getByName("1.2.3.4")),
                    new A("yurp.com.", 345, (Inet4Address) Inet4Address.getByName("1.2.3.4"))
            )));
        }
    }

    /**
     * Test adders and getters valid
     */
    @Nested
    class ResponseAdderGetterValid {
        //Test addAdditional
        @Test @DisplayName("Test addAdditional")
        void addAdditionalValid(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                CName cn = new CName(".", 123, "good.com.");
                CName cn2 = new CName(".", 123, "good.com.q.");
                NS ns = new NS(".", 123, "good.server.");
                A a = new A(".", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
                r.addAdditional(cn);
                r.addAdditional(ns);
                r.addAdditional(cn2);
                r.addAdditional(a);

                List<ResourceRecord> all = r.getAdditionalList();
                assertNotNull(all);
                assertAll("Records",
                        () -> assertEquals(cn, all.get(0)),
                        () -> assertEquals(ns, all.get(1)),
                        () -> assertEquals(cn2, all.get(2)),
                        () -> assertEquals(a, all.get(3))
                        );
            } catch (ValidationException | UnknownHostException e) {
                assert(false);
            }
        }

        /**
         * Test addAdditional ignore duplicates
         * @param params list to test
         */
        @ParameterizedTest(name = "Test addAdditional ignore duplicates")
        @ArgumentsSource(DuplicatesParamFactory.class)
        void addAdditionalDuplicateValid(List<ResourceRecord> params){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                List<ResourceRecord> unique = new ArrayList<>();
                for(ResourceRecord rr : params){
                    r.addAdditional(rr);
                    if(!unique.contains(rr)){
                        unique.add(rr);
                    }
                }

                List<ResourceRecord> all = r.getAdditionalList();
                assertNotNull(all);

                assertEquals(unique.size(), all.size());
                assertIterableEquals(unique, all);
            } catch (ValidationException e) {
                fail();
            }
        }

        /**
         * Test addAdditional change after insertion
         */
        @Test @DisplayName("Test addAdditional change entry after insertion")
        void addAdditionalValidChangeInside(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                CName cn = new CName(".", 5, "good.com.");
                CName cn2 = new CName(".", 6, "good.com.");
                CName cn2old = new CName(".", 6, "good.com.");
                NS ns = new NS(".", 123, "good.server.");
                A a = new A(".", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
                r.addAdditional(cn);
                r.addAdditional(ns);
                r.addAdditional(cn2);
                r.addAdditional(a);

                cn2.setTTL(5);

                List<ResourceRecord> all = r.getAdditionalList();
                assertNotNull(all);
                assertAll("Records",
                        () -> assertEquals(cn, all.get(0)),
                        () -> assertEquals(ns, all.get(1)),
                        () -> assertEquals(cn2old, all.get(2)),
                        () -> assertEquals(a, all.get(3)),
                        () -> assertEquals(4, all.size())
                );
            } catch (ValidationException | UnknownHostException e) {
                fail();
            }
        }


        //Test addAnswer
        @Test @DisplayName("Test addAnswer")
        void addAnswerValid(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                CName cn = new CName(".", 123, "good.com.");
                CName cn2 = new CName(".", 123, "good.com.q.");
                NS ns = new NS(".", 123, "good.server.");
                A a = new A(".", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
                r.addAnswer(cn);
                r.addAnswer(ns);
                r.addAnswer(cn2);
                r.addAnswer(a);

                List<ResourceRecord> all = r.getAnswerList();
                assertNotNull(all);
                assertAll("Records",
                        () -> assertEquals(cn, all.get(0)),
                        () -> assertEquals(ns, all.get(1)),
                        () -> assertEquals(cn2, all.get(2)),
                        () -> assertEquals(a, all.get(3))
                        );
            } catch (ValidationException | UnknownHostException e) {
                assert(false);
            }
        }

        /**
         * Test addAnswer ignore duplicates
         * @param params list to test
         */
        @ParameterizedTest(name = "Test addAnswer ignore duplicates")
        @ArgumentsSource(DuplicatesParamFactory.class)
        void addAnswerDuplicateValid(List<ResourceRecord> params){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                List<ResourceRecord> unique = new ArrayList<>();
                for(ResourceRecord rr : params){
                    r.addAnswer(rr);
                    if(!unique.contains(rr)){
                        unique.add(rr);
                    }
                }

                List<ResourceRecord> all = r.getAnswerList();
                assertNotNull(all);

                assertEquals(unique.size(), all.size());
                assertIterableEquals(unique, all);
            } catch (ValidationException e) {
                fail();
            }
        }

        /**
         * Test addAnswer change after insertion
         */
        @Test @DisplayName("Test addAnswer change entry after insertion")
        void addAnswerValidChangeInside(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                CName cn = new CName(".", 5, "good.com.");
                CName cn2 = new CName(".", 6, "good.com.");
                CName cn2old = new CName(".", 6, "good.com.");
                NS ns = new NS(".", 123, "good.server.");
                A a = new A(".", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
                r.addAnswer(cn);
                r.addAnswer(ns);
                r.addAnswer(cn2);
                r.addAnswer(a);

                cn2.setTTL(5);

                List<ResourceRecord> all = r.getAnswerList();
                assertNotNull(all);
                assertAll("Records",
                        () -> assertEquals(cn, all.get(0)),
                        () -> assertEquals(ns, all.get(1)),
                        () -> assertEquals(cn2old, all.get(2)),
                        () -> assertEquals(a, all.get(3)),
                        () -> assertEquals(4, all.size())
                );
            } catch (ValidationException | UnknownHostException e) {
                fail();
            }
        }


        //Test addNameServer
        @Test @DisplayName("Test addNameServer")
        void addNameServerValid(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                NS ns1 = new NS(".", 123, "good.server.");
                NS ns2 = new NS(".", 123, "good.server.a1.");
                r.addNameServer(ns1);
                r.addNameServer(ns2);

                List<ResourceRecord> all = r.getNameServerList();
                assertNotNull(all);
                assertAll("Records",
                        () -> assertEquals(ns1, all.get(0)),
                        () -> assertEquals(ns2, all.get(1))
                        );
            } catch (ValidationException e) {
                assert(false);
            }
        }

        /**
         * Test addNameServer ignore duplicates
         * @param params list to test
         */
        @ParameterizedTest(name = "Test addNameServer ignore duplicates (1)")
        @ArgumentsSource(DuplicatesParamFactory.class)
        void addNameServerDuplicateValid(List<ResourceRecord> params){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                List<ResourceRecord> unique = new ArrayList<>();
                for(ResourceRecord rr : params){
                    r.addNameServer(rr);
                    if(!unique.contains(rr)){
                        unique.add(rr);
                    }
                }

                List<ResourceRecord> all = r.getNameServerList();
                assertNotNull(all);

                assertEquals(unique.size(), all.size());
                assertIterableEquals(unique, all);
            } catch (ValidationException e) {
                fail();
            }
        }

        //Test addNameServer ignore duplicates
        @Test @DisplayName("Test addNameServer ignore duplicates (2)")
        void addNameServerDuplicateValid(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                NS ns1 = new NS(".", 123, "good.server.");
                NS ns2 = new NS(".", 123, "good.server.");
                NS ns3 = new NS(".", 356, "good.server.a1.");
                NS ns4 = new NS(".", 356, "good.server.a1.");
                r.addNameServer(ns1);
                r.addNameServer(ns2);
                r.addNameServer(ns3);
                r.addNameServer(ns4);

                List<ResourceRecord> all = r.getNameServerList();
                assertNotNull(all);
                assertAll("Records",
                        () -> assertEquals(ns1, all.get(0)),
                        () -> assertEquals(ns3, all.get(1)),
                        () -> assertEquals(2, all.size())
                        );
            } catch (ValidationException e) {
                assert(false);
            }
        }

        /**
         * Test addNameServer change after insertion
         */
        @Test @DisplayName("Test addNameServer change entry after insertion")
        void addNameServerValidChangeInside(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                CName cn = new CName(".", 5, "good.com.");
                CName cn2 = new CName(".", 6, "good.com.");
                CName cn2old = new CName(".", 6, "good.com.");
                NS ns = new NS(".", 123, "good.server.");
                A a = new A(".", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
                r.addNameServer(cn);
                r.addNameServer(ns);
                r.addNameServer(cn2);
                r.addNameServer(a);

                cn2.setTTL(5);

                List<ResourceRecord> all = r.getNameServerList();
                assertNotNull(all);
                assertAll("Records",
                        () -> assertEquals(cn, all.get(0)),
                        () -> assertEquals(ns, all.get(1)),
                        () -> assertEquals(cn2old, all.get(2)),
                        () -> assertEquals(a, all.get(3)),
                        () -> assertEquals(4, all.size())
                );
            } catch (ValidationException | UnknownHostException e) {
                fail();
            }
        }
    }

    /**
     * Test get set response code
     *   This is kind of pointless now that setRCode takes an RCode
     */
    @Nested
    class ResponseGetSetResponseCode extends RCodeSetGetAbstractTestFactory {
        /**
         * Null test rcode
         */
        @Test @DisplayName("Null check")
        void testNullRCode(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                assertThrows(ValidationException.class, () -> r.setRCode(null));
            } catch (ValidationException e) {
                fail();
            }
        }

        /**
         * Factory method for calling the appropriate function you want to test for rcode validity
         *
         * @param rcode rcode to call with
         * @return the result of a getRCode on the respective object
         * @throws ValidationException if invalid rcode
         */
        @Override
        protected RCode callSetGetRCode(int rcode) throws ValidationException {
            Response r = null;
            try {
                r = new Response(0, ".", RCode.NOERROR);
            } catch (ValidationException e) {
                fail();
            }

            r.setRCode(RCode.getRCode(rcode));
            return r.getRCode();
        }
    }

    /**
     * Response constructor tests (valid and invalid) (DONE)
     */
    @Nested
    class ResponseConstructorTests {
        //Test that all fields are initialized
        @Test @DisplayName("Testing other fields are initialized")
        void constInitializationTest(){
            Response r;
            try {
                r = new Response(0, ".", RCode.NOERROR);
                assertNotNull(r);
                assertAll("other fields", () -> assertEquals(0, r.getAdditionalList().size()),
                        () -> assertEquals(0, r.getAnswerList().size()),
                        () -> assertEquals(0, r.getNameServerList().size()),
                        () -> assertEquals(RCode.NOERROR, r.getRCode())
                );
            } catch (ValidationException | NullPointerException e) {
                assert(false);
            }
        }

        /**
         * Response constructor RCode tests
         */
        @Nested
        class ResponseConstructorRCodeTests extends RCodeSetGetAbstractTestFactory {
            /**
             * Null test rcode
             */
            @Test @DisplayName("Null check")
            void testNullRCode(){
                assertThrows(ValidationException.class, () -> new Response(0, ".", null));
            }

            /**
             * Factory method for calling the appropriate function you want to test for rcode validity
             *
             * @param rcode rcode to call with
             * @return the result of a getRCode on the respective object
             * @throws ValidationException if invalid rcode
             */
            @Override
            protected RCode callSetGetRCode(int rcode) throws ValidationException {
                Response r = new Response(0, ".", RCode.getRCode(rcode));
                return r.getRCode();
            }
        }

        /**
         * Response constructor SDNS ID tests (valid and invalid)
         */
        @Nested
        class ResponseConstructorIDTests extends SdnsIDTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for SDNS ID validity
             *
             * @param id id to test
             * @return the result of a getID on the respective object
             * @throws ValidationException if invalid SDNS ID
             */
            @Override
            protected int setGetID(int id) throws ValidationException {
                Message q = new Response(id, ".", RCode.NOERROR);
                return q.getID();
            }
        }

        /**
         * Response constructor query tests (valid and invalid)
         */
        @Nested
        class ResponseConstructorQueryTests extends DomainNameTestFactory {
            /**
             * Factory method for calling the appropriate function you want to test for domain name validity
             *
             * @param dm domain name to test
             * @return the result of a getDM on the respective object
             * @throws ValidationException if invalid domain name
             */
            @Override
            protected String setGetDomainName(String dm) throws ValidationException {
                Message q = new Response(0, dm, RCode.NOERROR);
                return q.getQuery();
            }

            /**
             * Allows the concrete class to specify which exception it wants to be thrown when a
             * null string is passed to the function
             *
             * @return class to throw
             */
            @Override
            protected Class<? extends Throwable> getNullThrowableType() { return ValidationException.class; }
        }
    }


    /**
     * Response equals() and hashcode() tests
     */
    @Nested
    class EqualsAndHashCode extends EqualsAndHashCodeCaseInsensitiveTestFactory<Response> {
        /**
         * helper setter for complex objects
         * @param u1 response to add to
         * @param cn cname1
         * @param cn2 cname2
         * @param ns nameserver1
         * @param a answer1
         * @param ns1 nameserver2
         * @param ns2 nameserver3
         * @throws ValidationException if null parameter
         */
        void complexEqualsHelper(Response u1, CName cn, CName cn2, NS ns, A a, NS ns1, NS ns2) throws ValidationException {
            u1.addNameServer(ns1);
            u1.addNameServer(ns2);
            u1.addAdditional(cn);
            u1.addAdditional(ns);
            u1.addAdditional(cn2);
            u1.addAdditional(a);
            u1.addAnswer(cn);
            u1.addAnswer(ns);
            u1.addAnswer(cn2);
            u1.addAnswer(a);
        }

        /**
         * Factory method for generating a default object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Response getDefaultObject0() throws ValidationException {
            return new Response(0, "good.com.", RCode.NOERROR);
        }

        /**
         * Factory method for generating a second object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Response getDefaultObject1() throws ValidationException {
            return new Response(123, "good.com.q.", RCode.NOERROR);
        }

        /**
         * Factory method for generating a third object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Response getDefaultObject2() throws ValidationException {
            return new Response(379, "good.com.", RCode.NOERROR);//this makes 0000 0000 0111 1011 into 0000 0001 0111 1011
        }

        /**
         * Factory method for generating a fourth object to test for (in)equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Response getDefaultObject3() throws ValidationException {
            Response u1 = new Response(0, "foo.", RCode.NOERROR);

            CName cn = new CName(".", 123, "good.com.");
            CName cn2 = new CName(".", 123, "good.com.q.");
            NS ns = new NS(".", 123, "good.server.");
            A a = null;
            try {
                a = new A(".", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                fail();
            }
            NS ns1 = new NS(".", 123, "good.server.");
            NS ns2 = new NS(".", 123, "good.server.a1.");
            complexEqualsHelper(u1, cn, cn2, ns, a, ns1, ns2);

            return u1;
        }

        /**
         * Factory method for generating a fifth object to test for (in)equality
         * RESERVED FOR COMPLEX EQUALS
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Response getDefaultObject4() throws ValidationException {
            Response u1 = new Response(0, "foo.", RCode.NOERROR);

            CName cn = new CName(".", 123, "good.com.");
            CName cn2 = new CName(".", 123, "good.com.q.");
            NS ns = new NS(".", 123, "good.server.");
            A a = null;
            try {
                a = new A(".", 123, (Inet4Address)Inet4Address.getByName("0.0.0.0"));
            } catch (UnknownHostException e) {
                fail();
            }
            NS ns1 = new NS(".", 123, "good.server.");
            NS ns2 = new NS(".", 123, "good.server.a.");//DIFFERENCE IS HERE
            complexEqualsHelper(u1, cn, cn2, ns, a, ns1, ns2);

            return u1;
        }

        /**
         * Factory method for generating a SIMILAR object of a different type to test for inequality
         * in types and hashcodes
         *
         * @return instantiation of different class object with similar field definitions
         * @throws ValidationException if invalid object
         */
        @Override
        protected Query getDifferentTypeObject() throws ValidationException {
            return new Query(0, "good.com.");
        }

        /**
         * Factory method for generating the first same object as getDefaultObject0 but with a different case
         * to test for ignore case equality
         *
         * @return the default object for this class
         * @throws ValidationException if invalid object
         */
        @Override
        protected Response getDefaultObjectDifferentCase1() throws ValidationException {
            return new Response(0, "GOOD.COM.", RCode.NOERROR);
        }
    }



    /**
     * Encode tests
     */
    @Nested
    class encodeTest {
        //Basic response encode test
        @Test
        @DisplayName("Response valid encode basic")
        void basicEncodeTest() {
            byte[] responseExpect = {
                    0, 42,
                    -127, 0,
                    0, 1,
                    0, 2,
                    0, 2,
                    0, 2,
                    3,'w','w','w', 3,'f','o','o',3,'c','o','m',0,
                    0, -1,
                    0, 1,
                    3, 'w', 'w', 'w',3,'f','o','o',3,'c','o','m', 0,
                    0, 5,
                    0, 1,
                    0, 0, 0, -56,
                    0, 9,
                    3,'f','o','o',3,'c','o','m',0,
                    3,'w','w','w',3,'f','o','o',3,'c','o','m',0,
                    0, 1,
                    0, 1,
                    0,0,0,-56,
                    0,4,
                    20,69,42,21,
                    3,'f','o','o',3,'c','o','m',0,
                    0,2,
                    0,1,
                    0,0,0,-56,
                    0, 12,
                    2,'n','s',3,'f','o','o',3,'c','o','m',0,
                    3,'f','o','o',3,'c','o','m',0,
                    0,2,
                    0,1,
                    0,0,0,-56,
                    0,13,
                    3,'n','s','2',3,'f','o','o',3,'c','o','m',0,
                    2,'n','s',3,'f','o','o',3,'c','o','m',0,
                    0,1,
                    0,1,
                    0,0,0,-56,
                    0,4,
                    100,4,5,6,
                    3,'n','s','2',3,'f','o','o',3,'c','o','m',0,
                    0,1,
                    0,1,
                    0,0,0,-56,
                    0,4,
                    100,4,5,7
            };

            try {
                byte[] result;
                Response testResponse = new Response(42, "www.foo.com.", RCode.NOERROR);
                testResponse.addAnswer(new CName("www.foo.com.", 200, "foo.com."));
                testResponse.addAnswer(new A("www.foo.com.",200,(Inet4Address)Inet4Address.getByName("20.69.42.21")));
                testResponse.addNameServer(new NS("foo.com.", 200, "ns.foo.com."));
                testResponse.addNameServer(new NS("foo.com.", 200, "ns2.foo.com."));
                testResponse.addAdditional(new A("ns.foo.com.", 200, (Inet4Address)Inet4Address.getByName("100.4.5.6")));
                testResponse.addAdditional(new A("ns2.foo.com.", 200, (Inet4Address)Inet4Address.getByName("100.4.5.7")));
                result = testResponse.encode();
                assertArrayEquals(responseExpect, result);
            } catch(Exception e) {
                fail("Exception Thrown", e);
            }
        }
    }

    /**
     * Encode and decode test
     */
    @Test @DisplayName("Testing unknown decoding and encoding")
    void unknownEncodeTest() {
        byte[] buff = {0, 0,//id
                -128, 0, //1 0000 [ignored bit]x7 0000
                0, 1, //0x0001
                0, 4, //ANCount
                0, 2, //NSCount
                0, 3, //ARCount
                //query
                3, 'f', 'o', 'o', -64, 5,
                0, -1,//0x00FF
                0, 1,  //0x0001
                //answer
                3, 'f', 'o', 'o', -64, 5,//CName
                0, 5,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 6,
                3, 'f', 'o', 'o', -64, 5,

                0,//A
                0, 1,
                0, 1,
                0, 0, 0, 0,
                0, 4,
                -1, 0, -1, -119,

                3, 'f', 'o', 'o', -64, 5,//Unknown
                1, 5,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 6,
                3, 'f', 'o', 'o', -64, 5,

                -64, 5,//NS
                0, 2,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 6,
                3, 'f', 'o', 'o', -64, 5,


                //authority
                -64, 5,//NS
                0, 2,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 6,
                3, 'f', 'o', 'o', -64, 5,

                3, 'f', 'o', 'o', -64, 5,//NS
                0, 2,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 1,
                0,

                //additional
                0,//A
                0, 1,
                0, 1,
                0, 0, 0, 0,
                0, 4,
                0, 0, 0, 0,

                3, 'f', 'o', 'o', -64, 5,//CName
                0, 5,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 6,
                3, 'f', 'o', 'o', -64, 5,

                -64, 5,//NS
                0, 2,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 6,
                3, 'f', 'o', 'o', -64, 5
        };
        byte[] expected = {0, 0,//id
                -127, 0, //1 0000 0010 000 0000
                0, 1, //0x0001
                0, 3, //ANCount
                0, 2, //NSCount
                0, 3, //ARCount
                //query
                3, 'f', 'o', 'o', 0,
                0, -1,//0x00FF
                0, 1,  //0x0001
                //answer
                3, 'f', 'o', 'o', 0,//CName
                0, 5,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 5,
                3, 'f', 'o', 'o', 0,

                0,//A
                0, 1,
                0, 1,
                0, 0, 0, 0,
                0, 4,
                -1, 0, -1, -119,

                0,//NS
                0, 2,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 5,
                3, 'f', 'o', 'o', 0,


                //authority
                0,//NS
                0, 2,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 5,
                3, 'f', 'o', 'o', 0,

                3, 'f', 'o', 'o', 0,//NS
                0, 2,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 1,
                0,

                //additional
                0,//A
                0, 1,
                0, 1,
                0, 0, 0, 0,
                0, 4,
                0, 0, 0, 0,

                3, 'f', 'o', 'o', 0,//CName
                0, 5,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 5,
                3, 'f', 'o', 'o', 0,

                0,//NS
                0, 2,
                0, 1, //0x0001
                0, 0, 0, 0,
                0, 5,
                3, 'f', 'o', 'o', 0
        };

        try {
            Message temp = Message.decode(buff);
            byte[] arr = temp.encode();
            for(int i=0; i<Math.min(arr.length, expected.length); i++){
                if(arr[i] != expected[i]){
                    System.out.println("Arrays differ at " + i + ": arr[i] = " + arr[i] + " != expected[i] = " + expected[i]);
                }
            }
            assertArrayEquals(expected, temp.encode());
        } catch (ValidationException e) {
            fail();
        }
    }
}