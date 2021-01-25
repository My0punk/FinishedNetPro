package sdns.app.tcp.server;

import sdns.app.ServerBoilerplate;
import sdns.app.ServerLogger;
import sdns.app.masterfile.MasterFileFactory;
import sdns.serialization.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Does almost all of the work for the server so it extends the boilerplate
 */
public class requestHandlingTask extends ServerBoilerplate implements Runnable {
    private static final Logger logger = ServerLogger.getLogger();
    private static final int TIMEOUT = 20000;
    private Socket client;
    private OutputStream toClient;
    private InputStream fromClient;

    /**
     * Constructor for this runnable task
     *
     * @param client the socket to communicate with
     */
    public requestHandlingTask(Socket client) {
        try {
            setMasterFile(MasterFileFactory.makeMasterFile());
            this.client = client;
            client.setSoTimeout(TIMEOUT);
            toClient = client.getOutputStream();
            fromClient = client.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the task. Handles the request.
     */
    @Override
    public void run() {
        //Keep reading requests from the client until client closes or timeout occurs.
        while(true) {
            try {
                //de-frame the TCP message
                byte[] receivedData = Framer.nextMsg(fromClient);

                //if the client has terminated, log it, close the socket, break the loop, and finish the task.
                if (receivedData == null) {
                    logger.log(Level.INFO, "Client at: " + client.getRemoteSocketAddress().toString() + " has closed the connection.");
                    client.close();
                    break;
                }

                Message request = Message.decode(receivedData);
                this.handlePacket(request);
            } catch (ValidationException e) {
                logger.log(Level.SEVERE, "Unable to parse message: " + e.getMessage());
            } catch (SocketTimeoutException e) {
                logger.log(Level.SEVERE, "Communication problem: " + e.getMessage());
            } catch (IOException e) {
                if (e instanceof EOFException) {
                    logger.log(Level.SEVERE, "Unable to parse message: " + e.getMessage());
                }
                logger.log(Level.SEVERE, "Communication problem: " + e.getMessage());
                try {
                    client.close();
                    break;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    /**
     * Sends a TCP response with the given encoded Message
     * @param encodedResp the encoded response to send back to the client.
     */
    @Override
    protected void sendResponse(byte[] encodedResp) {
        try {
            encodedResp = Framer.frameMsg(encodedResp);
            toClient.write(encodedResp);
        }
        catch (ValidationException e) {
            logger.log(Level.SEVERE, "unable to frame message");
        }
        catch (SocketTimeoutException e) {
            logger.log(Level.SEVERE, "Communication problem: " + e.getMessage());
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Communication problem: " + e.getMessage());
        }
    }
}
