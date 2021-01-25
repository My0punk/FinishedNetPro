/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 5
 * Class: Data Communications
 *
 ************************************************/
package sdns.app.tcp.server;

import sdns.app.ServerBoilerplate;
import sdns.app.ServerLogger;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The TCP server
 *
 * @version 1.0
 */
public class Server {
    private static final Logger logger = ServerLogger.getLogger();
    private ServerSocket serverSocket;
    private ExecutorService threadPool;


    public static void main(String[] args) {
        Server server = new Server();
        server.init(args);
    }

    /**
     * Initializes the server and runs it.
     * @param args the input parameters
     */
    public void init(String[] args) {
        try {
            if (args.length != 2) {
                logger.log(Level.SEVERE, "Unable to start: bad param");
                throw new IllegalArgumentException("Parameter: <portNumber> <threadPoolSize>");
            }

            //Get the port. Handle if bad
            int listeningPort = Integer.parseInt(args[0]);
            int threadPoolSize = Integer.parseInt(args[1]);
            ServerBoilerplate.checkPort(listeningPort);

            //create thread pool
            threadPool = Executors.newFixedThreadPool(threadPoolSize);

            serverSocket = new ServerSocket(listeningPort, 20000);
            logger.log(Level.INFO, "Server started...");
            logger.log(Level.INFO, "Running on: " + serverSocket.getLocalSocketAddress().toString());

            //Listen for connection requests. If a connection is made, create a socket hand it to a task that is
            // submitted to the threadpool queue.
            while(true) {
                Socket clientConnection = this.serverSocket.accept();
                this.threadPool.execute(new requestHandlingTask(clientConnection));
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Unable to start: " + e.getMessage());
            threadPool.shutdown();
        }
    }
}
