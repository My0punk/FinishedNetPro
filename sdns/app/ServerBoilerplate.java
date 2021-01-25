/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 5
 * Class: Data Communications
 *
 ************************************************/
package sdns.app;

import sdns.app.masterfile.MasterFile;
import sdns.app.masterfile.MasterFileFactory;
import sdns.serialization.*;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Boilerplate code for the Blocking IO servers. UDP and TCP.
 *
 * This is also used in the case of delegating client handling to task threads.
 * The task thread classes can extend this class to implement proper protocol handling.
 *
 * @version 1.0
 */
public abstract class ServerBoilerplate {
    private static final Logger logger = ServerLogger.getLogger();
    protected MasterFile masterFile;

    /**
     * Checks to see if the given port number is a valid port number
     * @param port the port number being checked
     */
    public static void checkPort(int port) {
        if(port < 0 || port > 65535) {
            logger.log(Level.SEVERE, "Unable to start: bad param, port out of valid range");
            throw new IllegalArgumentException("bad param, port out of valid range. Parameter: <validPortNumber>");
        }
    }

    /**
     * Sends a response to the client of the server.
     * Dispatches to the right server type that implements the protocol specific response
     * @param encodedResp the byte array of the encoded response
     */
    protected abstract void sendResponse(byte[] encodedResp);

    /**
     * Sets the masterfile for the server.
     * @param masterFile the masterfile to set
     */
    public void setMasterFile(MasterFile masterFile) {
        this.masterFile = masterFile;
    }

    /**
     * checks if the request was a proper query type
     * @param request the request to check
     * @return a bool. true if it wasn't a query.
     * @throws ValidationException
     *      - if the error handling function throws an error
     */
    protected boolean checkRequestType(Message request) throws ValidationException{
        if(request instanceof Response) {
            this.requestWasResponseError(request);
            return true;
        }
        return false;
    }

    /**
     * Handles the packet being received.
     * New version. Only takes in a decoded request and delegates sends to the relevant
     * protocol through dispatch
     *
     * @param request the decoded request
     */
    public void handlePacket(Message request) {
        try {
            if(checkRequestType(request)) {
                return;
            }

            logger.log(Level.INFO, "Query Received: " + request.toString());

            //The response to send back if successful. uses the same ID as the query
            Response response = new Response(request.getID(), request.getQuery(), RCode.NOERROR);

            //get the references to the new empty lists in the just created response object
            List<ResourceRecord> answers = response.getAnswerList();
            List<ResourceRecord> nameServers = response.getNameServerList();
            List<ResourceRecord> additionals = response.getAdditionalList();

            //attempt getting answers from masterfile
            try {
                this.masterFile.search(request.getQuery(), answers, nameServers, additionals);
            } catch (Exception e) {
                if(e instanceof NoSuchElementException) {
                    nameNotExistError(request);
                    return;
                }
                if(e instanceof NullPointerException || e instanceof ValidationException) {
                    otherError(request, e);
                    return;
                }
            }

            //if successful, send a good response
            byte[] encodedResp = response.encode();
            logger.log(Level.INFO, "Sending good response: " + response.toString());
            this.sendResponse(encodedResp);
        } catch (ValidationException e) {
            logger.log(Level.SEVERE, "Unable to parse message: " + e.getMessage() + ", Bad Token: " + e.getBadToken());
            //TODO: handle this better, try and process the rest
        }
    }

    /**
     * The handler if an 'other error' comes from the masterfile search
     * @param request the request that was made
     * @throws ValidationException
     *      If the creation of the response to send back runs into an error
     */
    protected void otherError(Message request, Throwable e) throws ValidationException{
        logger.log(Level.SEVERE, "Problem resolving: " + request.toString(), e);
        Message response = new Response(request.getID(), request.getQuery(), RCode.SERVERFAILURE);
        byte[] encodedResp = response.encode();
        logger.log(Level.INFO, "Attempting to respond to client with RCode 2 response: " + response.toString());
        this.sendResponse(encodedResp);
    }

    /**
     * The handler if an 'Name not exist' comes from the masterfile search
     * @param request the request that was made
     * @throws ValidationException
     *      If the creation of the response to send back runs into an error
     */
    protected void nameNotExistError(Message request) throws ValidationException {
        logger.log(Level.SEVERE, "Domain name does not exist: " + request.toString());
        Message response = new Response(request.getID(), request.getQuery(), RCode.NAMEERROR);
        byte[] encodedResp = response.encode();
        logger.log(Level.INFO, "Attempting to respond to client with RCode 3 response: " + response.toString());
        this.sendResponse(encodedResp);
    }

    /**
     * The handler if a 'query' received from the client is actually a response object
     * @param request the request that was made
     * @throws ValidationException
     *      If the creation of the response to send back runs into an error
     */
    protected void requestWasResponseError(Message request) throws ValidationException{
        logger.log(Level.SEVERE, "Unexpected message type: " + request.toString());
        Message response = new Response(request.getID(), request.getQuery(), RCode.REFUSED);
        byte[] encodedResp = response.encode();
        logger.log(Level.INFO, "Attempting to respond to client with RCode 5 response: " + response.toString());
        this.sendResponse(encodedResp);
    }
}
