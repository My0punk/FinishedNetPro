/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 6
 * Class: Data Communications
 *
 ************************************************/

package sdns.app.tcp.server;

import sdns.app.ServerBoilerplate;
import sdns.app.ServerLogger;
import sdns.app.masterfile.MasterFile;
import sdns.app.masterfile.MasterFileFactory;
import sdns.serialization.Framer;
import sdns.serialization.Message;
import sdns.serialization.NIODeframer;
import sdns.serialization.ValidationException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The TCP Asynchronous server implementation
 *
 * @version 1.0
 */
//Credit given to Dr. Donahoo's 'BasicTCPEchoAIO' example.
public class ServerAIO {
    public static final Logger logger = ServerLogger.getLogger();
    private static final int TIMEOUT = 20;
    private static final int BUFSIZE = 256;
    private static MasterFile masterFile;

    public static void main(String[] args) {
        ServerAIO server = new ServerAIO();
        server.init(args);
    }

    /**
     *  Initializes the server and runs it
     * @param args the args passed in
     */
    public void init(String[] args) {
        if(args.length != 1) {
            throw new IllegalArgumentException("Parameters: <Port>");
        }

        //only continue initializing if the listenChannel could be opened.
        try(AsynchronousServerSocketChannel listenChannel = AsynchronousServerSocketChannel.open()) {
            listenChannel.bind(new InetSocketAddress(Integer.parseInt(args[0])), 100);
            masterFile = MasterFileFactory.makeMasterFile();
            listenChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel clientChan, Void attachment) {
                    listenChannel.accept(null, this);
                    try {
                        handleAccept(clientChan);
                    } catch(IOException e) {
                        failed(e,null);
                    }
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    logger.log(Level.WARNING, "Close Failed", exc);
                }
            });
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Main thread failed to join", e);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Unable to start: " + e.getMessage(), e);
        }
    }

    /**
     * Executes after completion of accept. Sets up the read buffer, the deframer and the protocol handler for the socket channel
     * @param clientChan the client socket channel
     * @throws IOException -
     *      If there is an error closing the socket
     */
    public void handleAccept(final AsynchronousSocketChannel clientChan) throws IOException {
        ByteBuffer readBuf = ByteBuffer.allocateDirect(BUFSIZE);
        NIODeframer nioDeframer = new NIODeframer();
        ServerBoilerplate protocolHandler = this.getProtocolHandler(clientChan, readBuf, nioDeframer);
        try {
            protocolHandler.setMasterFile(masterFile);
            executeRead(clientChan, readBuf, nioDeframer, protocolHandler);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Masterfile failed to initialize");
            clientChan.close();
        }

    }

    /**
     * Executes after completion of a read.
     *
     * If no full frame is provided after processing the data that was read, it will read again.
     * If a full frame is provided, it will deserialize it and pass it to the protocol handler.
     * If the protocol handler fails, It will attempt to read for another request.
     * @param clientChan the socket channel
     * @param readBuf the buffer that was read into
     * @param bytesRead the bytes read
     * @param nioDeframer the deframer
     * @throws IOException -
     *      If the socket fails to close.
     *
     */
    public void handleRead(final AsynchronousSocketChannel clientChan, ByteBuffer readBuf, int bytesRead, NIODeframer nioDeframer, ServerBoilerplate protocolHandler) throws IOException {
        byte[] frame;
        //if the client has disconnected, close the channel
        if(bytesRead == -1) {
            clientChan.close();
        } else if (bytesRead > 0) {
            //get what was read, then try to get a frame
            readBuf.flip();
            byte[] read = new byte[readBuf.remaining()];
            readBuf.get(read);
            frame = nioDeframer.getMessage(read);

            //if not frame available, try and read again
            if(frame == null) {
                executeRead(clientChan, readBuf, nioDeframer, protocolHandler);
            } else {
                //handlePacket will eventually call the anonymous ServerBoilerplate class's sendResponse which will call
                // a write to the client socket channel.
                try {
                    Message request = Message.decode(frame);
                    protocolHandler.handlePacket(request);
                } catch (ValidationException e) {
                    logger.log(Level.SEVERE, "Unable to parse message: ", e);
                    //if an exception was thrown handling the previous packet, read again to check for another packet.
                    handleWrite(clientChan, ByteBuffer.allocateDirect(0).limit(0), readBuf, nioDeframer, protocolHandler);
                }
            }
        }
    }

    /**
     * Handles after a write. In the event of there being more frames possible to deal with, deal with them.
     *
     * @param clientChan the client channel to use
     * @param buf the buffer to write from
     * @param readBuf the buffer to read into later
     * @param nioDeframer the deframer for the given channel
     * @param protocolHandler the protocol handler
     */
    public void handleWrite(final AsynchronousSocketChannel clientChan, ByteBuffer buf, ByteBuffer readBuf, NIODeframer nioDeframer, ServerBoilerplate protocolHandler) {
        byte[] frame = nioDeframer.getMessage(new byte[0]); //check if there is already another frame ready to work with.
        //if not all bytes have been written, write again.
        if(buf.hasRemaining()) {
            clientChan.write(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer bytesWritten, ByteBuffer buf) {
                    handleWrite(clientChan, buf, readBuf, nioDeframer, protocolHandler);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        logger.log(Level.SEVERE, "Write failed, client side has most likely terminated their connection");
                        clientChan.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Close failed");
                    }
                }
            });
        } else if (frame != null){ //if there is another frame ready, process the request and write a result back
            try {
                Message request = Message.decode(frame);
                protocolHandler.handlePacket(request); //this will resolve the request and eventually write it.
            } catch (ValidationException e) {
                //if this frame is malformed or something, log the error and then run handle write again to try and write whatever might be next in the niodeframer
                logger.log(Level.SEVERE, "Unable to parse message: ", e);
                handleWrite(clientChan, buf, readBuf, nioDeframer, protocolHandler);
            }

        } else {
            //gets ready for another read. Instantiates a new protocol handler to use for the next read.
            readBuf.clear();
            executeRead(clientChan, readBuf, nioDeframer, protocolHandler);
        }
    }

    /**
     * Executes a read using the given protocol handler
     * @param clientChan the client socket channel
     * @param readBuf the buff to read into
     * @param nioDeframer the deframer for this channel
     * @param protocolHandler the protocol handler
     */
    private void executeRead(final AsynchronousSocketChannel clientChan, ByteBuffer readBuf, NIODeframer nioDeframer, ServerBoilerplate protocolHandler) {
        clientChan.read(readBuf, TIMEOUT, TimeUnit.SECONDS, readBuf, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer buf) {
                try {
                    handleRead(clientChan, buf, bytesRead, nioDeframer, protocolHandler);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Handle read failed", e);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                //If failure due to timeout, log error and close connection with client.
                if(exc instanceof InterruptedByTimeoutException) {
                    logger.log(Level.SEVERE, "Communication Problem:", exc);
                    try {
                        //in case the client doesn't shutdown their write end
                        clientChan.close();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Failed to Close connection with client");
                    }
                }
            }
        });
    }

    /**
     * Gets a protocol handler anon class instance that uses the client socket channel, read buffer, and deframer provided.
     * I needed this because in the case that I need to continue reading after handling a write, handleWrite can't take a
     * protocolHandler anon class instance because it is always initially called within the sendResponse of the anon class being instantiated and I can't pass the instance
     * of the class to a method being called within it. I don't know how to fix this as of now
     * @param clientChan the channel to use
     * @param buf the buffer to read into
     * @param nioDeframer the deframer for this channel
     * @return an instance of a ServerBoilerplate anon class tied to the parameters given
     */
    private ServerBoilerplate getProtocolHandler(final AsynchronousSocketChannel clientChan, ByteBuffer buf, NIODeframer nioDeframer) {
        return new ServerBoilerplate() {
            @Override
            protected void sendResponse(byte[] encodedResp) {
                try {
                    //frame the response, wrap in a buffer, write to the channel
                    encodedResp = Framer.frameMsg(encodedResp);
                    ByteBuffer bufAnon = ByteBuffer.wrap(encodedResp);
                    ServerBoilerplate jank = this; //this is really hacky, but I don't know a better way. So for now, it is jank
                    clientChan.write(bufAnon, bufAnon, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer bytesWritten, ByteBuffer bufAnon) {
                            handleWrite(clientChan, bufAnon, buf, nioDeframer, jank);
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            try {
                                logger.log(Level.WARNING,"Write failed", exc);
                                clientChan.close();
                            } catch (IOException e) {
                                logger.log(Level.WARNING, "Failed to close socket");
                            }
                        }
                    });
                } catch (ValidationException e) {
                    logger.log(Level.SEVERE, "Unable to parse message:", e);
                }
            }
        };
    }
}
