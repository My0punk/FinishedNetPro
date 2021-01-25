package sdns.app.masterfile;

import sdns.serialization.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.NoSuchElementException;

public class MasterFileTCP implements MasterFile{
    private static final String DNSServerIP = "ns3.baylor.edu";
    private static final int port = 53;
    private static final int TIMEOUT = 3000;


    /**
     * Populate answer, name server, and additional list RRs.
     *
     * @param question    query for SDNS query
     * @param answers     RR list (allocated) to add answer RRs to
     * @param nameservers RR list (allocated) to add name server RRs to
     * @param additionals RR list (allocated) to add additional RRs to
     * @throws NoSuchElementException if no such domain name
     * @throws NullPointerException   if any parameters are null
     * @throws ValidationException    if question is invalid or anything else goes wrong while
     *                                trying to resolve question
     */
    @Override
    public void search(String question, List<ResourceRecord> answers, List<ResourceRecord> nameservers, List<ResourceRecord> additionals) throws NoSuchElementException, NullPointerException, ValidationException {
        try {
            Socket socket = new Socket(InetAddress.getByName(DNSServerIP), port);
            socket.setSoTimeout(TIMEOUT);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            byte[] encoded = new Query((int) (Math.random() * 65535), question).encode();
            encoded = Framer.frameMsg(encoded);
            out.write(encoded);
            Message received = null;
            int tries = 0;
            while(tries < 2) {
                try {
                    byte[] receivedData = Framer.nextMsg(in);
                    if(receivedData == null) {
                        throw new ValidationException("Server connected to masterfile closed the connection", "");
                    }

                    received = Message.decode(receivedData);

                    if(received instanceof Query) {
                        continue;
                    }

                    if(received.getRCode() != RCode.NOERROR) {
                        if(received.getRCode() == RCode.NAMEERROR) {
                            throw new NoSuchElementException("Name does not exist");
                        }
                        throw new ValidationException("Bad RCode", "");
                    }
                } catch (SocketTimeoutException e) {
                    tries++;
                    continue;
                }
                break;
            }
            socket.close();

            assert received instanceof Response;
            Response response = (Response) received; //its stupid i have to do this but I know no better way
            answers.addAll(response.getAnswerList());
            nameservers.addAll(response.getNameServerList());
            additionals.addAll(response.getAdditionalList());
        } catch (UnknownHostException e) {
            throw new ValidationException("Error creating socket", "");
        } catch (IOException e) {
            throw new ValidationException("Error with masterfile Socket", e, "");
        }
    }
}
