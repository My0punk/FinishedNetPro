package sdns.app;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Creates and gives the logger to use for the server protocol.
 *
 * This is so the TCP and UDP server can use the same logger without duplicating code.
 *
 * Also, this allows the server class and the server boilerplate class to use the same logger
 *  so logging is consistent between both files and goes to the same log file by using the
 *  same file handler..
 *
 * @version 1.0
 */
public class ServerLogger {
    private static final Logger logger = Logger.getLogger(ServerLogger.class.getName());
    private static FileHandler logFile;

    static {
        //only allow file output. remove console log handler
        Logger consoleLog = Logger.getLogger("");
        consoleLog.removeHandler(consoleLog.getHandlers()[0]);
        try {
            logFile = new FileHandler("./connections.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addHandler(logFile);
        SimpleFormatter formatter = new SimpleFormatter();
        logFile.setFormatter(formatter);
    }

    /**
     * Just returns the single instance of the logger to use for all server related classes.
     *
     * @return the logger instance;
     */
    public static Logger getLogger() {
        return logger;
    }

}
