/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 2
 * Class: Data Communications
 *
 ************************************************/

package sdns.app.udp.client;

import sdns.app.ClientBoilerplate;
import sdns.serialization.Message;
import sdns.serialization.Query;
import sdns.serialization.ValidationException;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 * Main class to run the UDP DNS client.
 * Some code referenced from Donahoo's UDPEchoClientTimeout.java in the TCP/IP Sockets book 2nd Edition
 */
public class Client {
    private static final int TIMEOUT = 3000;
    private static Map<Integer, String> expectedList = new HashMap<>(); //EL
    private static List<Query> sendPackets = new ArrayList<>(); //queries to send
    private static int nextMessageID = 1;

    /**
     * Main
     * @param args in the format of "<serverIP/name> <Port> <Query> [<Query>]+"
     * @throws IOException
     *      Any general IOException
     */
    public static void main(String[] args) throws IOException {

        if(args.length < 3) {
            throw new IllegalArgumentException("Parameters: <serverIP/name> <Port> <Query> [<Query>]+");
        }

        InetAddress serverAddress = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT);

        ClientBoilerplate.getQueryStringsFromArgs(args, sendPackets, expectedList, nextMessageID);

        try {
            sendAllPackets(socket, serverAddress, serverPort);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            socket.close();
            return;
        }

        List<Message> deserializedResponses = ClientBoilerplate.receiveResponsesUDP(socket, serverAddress, serverPort, expectedList);
        //If the amount received back is not the amount sent originally, then there was an issue.
        ClientBoilerplate.printResponses(deserializedResponses, sendPackets, expectedList);

        socket.close();
    }

    /**
     * sends all packets in query list
     * @param socket the socket being used
     * @param serverAddress server address being used by socket
     * @param serverPort server port being used by socket
     * @throws IOException
     *      If encode throws an IOException during query encoding.
     */
    private static void sendAllPackets(DatagramSocket socket, InetAddress serverAddress, int serverPort) throws IOException {
        for(Query query : sendPackets) {
            byte[] encoded = query.encode();
            socket.send(new DatagramPacket(encoded, encoded.length, serverAddress, serverPort));
        }
    }

}
