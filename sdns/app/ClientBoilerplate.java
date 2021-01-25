/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 4
 * Class: Data Communications
 *
 ************************************************/
package sdns.app;

import sdns.serialization.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A helper class to aid the Clients
 */
public class ClientBoilerplate {
    /**
     * Retrieves all the query strings from the client args
     * @param args args
     * @param sendPackets the list of queries to send
     * @param expectedList the map of IDs to their queries for expected responses
     * @param nextMessageID the next ID var to assign to the queries.
     */
    public static void getQueryStringsFromArgs(String[] args, List<Query> sendPackets, Map<Integer, String> expectedList, int nextMessageID) {
        //get all query strings
        for(int i = 2; i < args.length; i++) {
            try {
                //ensure the query string ends with '.'
                if (!args[i].endsWith(".")) {
                    args[i] = args[i].concat(".");
                }
                Query nextQuery = new Query(nextMessageID, args[i]);
                sendPackets.add(nextQuery);
                expectedList.put(nextMessageID, args[i]);
            } catch (ValidationException e) {
                throw new IllegalArgumentException("Malformed question: " + args[i] +
                        " Message from inner exception: " + e.getMessage());
            }
            nextMessageID++;
        }
    }

    /**
     * Sends all the packets for tcp
     * @param tcpSend the output stream for a tcp socket
     * @param sendPackets the queries to send
     * @throws IOException
     *      If sending to the tcp output stream has an error
     * @throws ValidationException
     *      If there is an error on encode or framing the message
     */
    public static void sendAllPacketsTCP(OutputStream tcpSend, List<Query> sendPackets) throws IOException, ValidationException {
        for(Query query : sendPackets) {
            byte[] encoded = query.encode();
            encoded = Framer.frameMsg(encoded);
            tcpSend.write(encoded);
        }
    }

    /**
     * Checks that the list of expected responses contains the ID of the received message.
     * Then checks if the query in the received response is the same as the one mapped to its ID.
     * @param received the message received
     * @param expectedList the list of expected responses
     * @return true if error
     */
    public static boolean checkELForIDAndQueryMatch(Message received, Map<Integer, String> expectedList) {
        if(!expectedList.containsKey(received.getID())) {
            System.err.println("No Such ID: " + received.toString());
            return true;
        }
        else if(!received.getQuery().equals(expectedList.get(received.getID()))) {
            System.err.println("Non-matching query: " + received.toString());
            return true;
        }
        return false;
    }

    /**
     * If an RCode is not NOERROR, it returns false, and spits the error to system.err
     * @param received the message to examine
     * @param expectedList the expectedlist to remove the message from
     * @return a bool. true if bad RCode
     */
    public static boolean checkRCodeFromReceived(Message received, Map<Integer, String> expectedList) {
        if(received.getRCode() != RCode.NOERROR) {
            switch(received.getRCode().getRCodeValue()) {
                case 1 -> {
                    System.err.println("Format Error: " + received.getRCode().getRCodeMessage());
                }
                case 2 -> {
                    System.err.println("Server Failure: " + received.getRCode().getRCodeMessage());
                }
                case 3 -> {
                    System.err.println("Name Error: " + received.getRCode().getRCodeMessage());
                }
                case 4 -> {
                    System.err.println("Not Implemented: " + received.getRCode().getRCodeMessage());
                }
                case 5 -> {
                    System.err.println("Refused: " + received.getRCode().getRCodeMessage());
                }
            }
            String removed = expectedList.remove(received.getID()); //remove from EL
            return true;
        }
        return false;
    }

    /**
     * Processes a list of responses.
     * @param responses the responses received
     * @param sendPackets the queries that were sent
     * @param expectedList the map of expected responses
     */
    public static void printResponses(List<Message> responses, List<Query> sendPackets, Map<Integer, String> expectedList) {
        if(responses.size() != sendPackets.size()) {
            expectedList.forEach((k,v) -> System.err.println("No Response: " + v));
        }

        if(responses.size() != 0) {
            System.out.println("Good Responses: ");
            for(Message response : responses) {
                System.out.println(response.toString());
            }
        }
    }

    /**
     * Receives the TCP responses for the queries sent in sentPackets
     * @param in the TCP socket input stream to receive the response from
     * @param sendPackets the queries sent
     * @param expectedList the map of IDs to their queries to verify responses
     * @return a list of messages containing all the received responses
     */
    public static List<Message> receiveResponsesTCP(InputStream in, List<Query> sendPackets, Map<Integer, String> expectedList) {
        boolean retried = false;
        List<Message> results = new ArrayList<>();
        do {
            try {
                byte[] receivedData = Framer.nextMsg(in);
                if(receivedData == null) {
                    System.err.println("The server has terminated the connection");
                    return results;
                }
                Message received = Message.decode(receivedData);

                if(received instanceof Query) {
                    System.err.println("Unexpected query: " + received.toString());
                    continue;
                }

                if(checkELForIDAndQueryMatch(received, expectedList)) continue;

                if(checkRCodeFromReceived(received, expectedList)) continue;

                String removed = expectedList.remove(received.getID());
                results.add(received);
            } catch(SocketTimeoutException e) {
                if(!retried) {
                    retried = true;
                    System.out.println("Server timed out, wait one more time");
                } else {
                    return results;
                }
            } catch (ValidationException e) {
                System.err.println(e.getMessage() + " " + e.getBadToken());
                if(retried) {
                    return results;
                }
            } catch (IOException e) {
                if(e instanceof EOFException) {
                    System.err.println("Error de-framing a response: " + e.getMessage());
                }
                e.printStackTrace();
            }
        } while(expectedList.size() > 0);

        return results;
    }

    /**
     * Resends only the queries left in the ExpectedList for UDP
     * @param socket the socket to send with
     * @param serverAddress the address of the server to send to
     * @param serverPort the port of the server to send to
     * @throws IOException
     *      If there is any i/o issue with a send
     */
    private static void resendRemainingEL(DatagramSocket socket, InetAddress serverAddress, int serverPort, Map<Integer, String> expectedList) throws IOException {
        List<Query> resendList = new ArrayList<>();
        expectedList.forEach((k,v) -> {
            try {
                resendList.add(new Query(k,v));
            } catch (ValidationException e) {
                System.err.println("If this appears something seriously wrong happened when constructing the queries to resend.");
                e.printStackTrace();
            }
        });
        for(Query query : resendList) {
            byte[] encoded = query.encode();
            socket.send(new DatagramPacket(encoded, encoded.length, serverAddress, serverPort));
        }
    }

    /**
     * Receives all packets
     *
     * @param socket the socket being used
     * @param serverAddress the address of the server being contacted
     * @param serverPort the server port
     * @return a list of messages.
     */
    public static List<Message> receiveResponsesUDP(DatagramSocket socket, InetAddress serverAddress, int serverPort, Map<Integer, String> expectedList) {
        boolean retried = false;
        List<Message> result = new ArrayList<>();
        do {
            DatagramPacket receivedPacket = new DatagramPacket(new byte[512], 512);
            try {
                //if failed last time, resend.
                if(retried) {
                    resendRemainingEL(socket, serverAddress, serverPort, expectedList);
                }

                socket.receive(receivedPacket);
                if(!receivedPacket.getAddress().equals(serverAddress)) {
                    throw new IOException("Packet received from unknown source");
                }

                byte[] dataOfRecentlyReceived = receivedPacket.getData();

                //messy but works i hope. decodes the received packet data
                Message received = Message.decode(Arrays.copyOfRange(dataOfRecentlyReceived, 0, receivedPacket.getLength()));

                if(received instanceof Query) {
                    System.err.println("Unexpected Query: " + received.toString());
                    continue; //just try and receive again. act like this didn't happen.
                }

                if(ClientBoilerplate.checkELForIDAndQueryMatch(received, expectedList)) continue;

                if(ClientBoilerplate.checkRCodeFromReceived(received, expectedList)) continue;

                String removed = expectedList.remove(received.getID()); //remove from EL
                result.add(received);
            } catch(InterruptedIOException e) {
                if(!retried) {
                    retried = true;
                    System.out.println("One Timeout, 1 more try");
                } else {
                    return result; //return what i have at this point after a failed 2nd try.
                }
            } catch (IOException | ValidationException e) {
                e.printStackTrace();
                if(e instanceof ValidationException) {
                    System.out.println(((ValidationException) e).getMessage() + " " + ((ValidationException) e).getBadToken());
                }
                if(retried) {
                    return result;
                }
            }
        }while(expectedList.size() > 0);

        return result;
    }

}
