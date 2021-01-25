/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 4
 * Class: Data Communications
 *
 ************************************************/

package sdns.app.tcp.client;

import sdns.app.ClientBoilerplate;
import sdns.serialization.Message;
import sdns.serialization.Query;
import sdns.serialization.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The TCP DNS client
 */
public class Client {
    private static final int TIMEOUT = 000;
    private static Map<Integer, String> expectedList = new HashMap<>(); //EL
    private static List<Query> sendPackets = new ArrayList<>(); //queries to send
    private static int nextMessageID = 1;

    /**
     * The main TCP client method
     * @param args contains args in the format <serverIP/name> <port> <Query> [<Query>]+
     * @throws IOException
     *      If there is a socket connection issue such as being unable to establish connection.
     */
    public static void main(String[] args) throws IOException {
        if(args.length < 3) {
            throw new IllegalArgumentException("Parameters: <serverIP/name> <Port> <Query> [<Query>]+");
        }
        InetAddress serverAddress = InetAddress.getByName(args[0]);
        int serverPort = Integer.parseInt(args[1]);

        Socket socket = new Socket(serverAddress, serverPort);
        socket.setSoTimeout(TIMEOUT);

        ClientBoilerplate.getQueryStringsFromArgs(args, sendPackets, expectedList, nextMessageID);

        InputStream in = socket.getInputStream(); //incoming responses
        OutputStream out = socket.getOutputStream(); //outgoing queries

        try {
            ClientBoilerplate.sendAllPacketsTCP(out, sendPackets);
            socket.shutdownOutput(); //close write
        } catch (IOException | ValidationException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            socket.close();
            return;
        }

        List<Message> responses = ClientBoilerplate.receiveResponsesTCP(in, sendPackets, expectedList);
        ClientBoilerplate.printResponses(responses, sendPackets, expectedList);

        socket.close();
    }

}
