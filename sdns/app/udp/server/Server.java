/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 3
 * Class: Data Communications
 *
 ************************************************/

package sdns.app.udp.server;

import sdns.app.ServerBoilerplate;
import sdns.app.ServerLogger;
import sdns.app.masterfile.MasterFileFactory;
import sdns.serialization.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.*;

public class Server extends ServerBoilerplate {
    //The logger
    private static final Logger logger = ServerLogger.getLogger();
    //These are self explanatory. Max buffer size for the UDP packet and timeout for the socket.
    private final int BUFFER_MAX = 512, TIMEOUT = 3000;
    private DatagramSocket socket;
    private DatagramPacket currRequestPacket;

    public static void main(String[] args) {
        Server server = new Server();
        server.init(args);
    }

    public void init(String[] args) {
        try {
            if(args.length != 1) {
                logger.log(Level.SEVERE, "Unable to start: bad param");
                throw new IllegalArgumentException("Parameter: <portNumber>");
            }

            //Make the masterfile object
            setMasterFile(MasterFileFactory.makeMasterFile());

            //Get the port. Handle if bad
            int listeningPort = Integer.parseInt(args[0]);
            checkPort(listeningPort);

            //create sockets and packet to accept requests
            this.socket = new DatagramSocket(listeningPort);
            logger.log(Level.INFO, "Running at {0}", InetAddress.getLocalHost());

            //server loop
            //Receives, logs, then handles it
            while(true) {
                currRequestPacket = new DatagramPacket(new byte[BUFFER_MAX], BUFFER_MAX);
                socket.receive(currRequestPacket);
                logger.log(Level.INFO, "Serving a request from " + currRequestPacket.getAddress().getHostAddress() + " on port " + currRequestPacket.getPort());

                //decode the packet in the UDP specific manner
                byte[] packetData = currRequestPacket.getData();
                Message request = Message.decode(Arrays.copyOfRange(packetData, 0, currRequestPacket.getLength()));
                handlePacket(request);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to start: {0}", e.getMessage());
        }
        logger.info("Server shutting down");
    }

    /**
      * UDP:
      * Sends a response to the datagram socket
      *
      * @param encodedResp the encoded response to send
      */
    @Override
    protected void sendResponse(byte[] encodedResp) {
        try {
            this.socket.send(new DatagramPacket(encodedResp, encodedResp.length, this.currRequestPacket.getAddress(), this.currRequestPacket.getPort()));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Communication Problem: " + e.getMessage());
        }
    }
}
