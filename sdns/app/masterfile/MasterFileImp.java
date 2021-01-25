/************************************************
 *
 * Author: Harrison Rogers
 * Assignment: Program 3
 * Class: Data Communications
 *
 ************************************************/

package sdns.app.masterfile;

import sdns.serialization.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class MasterFileImp implements MasterFile {
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
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);

            byte[] encoded = new Query(1, question).encode();
            socket.send(new DatagramPacket(encoded, encoded.length, InetAddress.getByName(DNSServerIP), port));

            int tries = 0;
            DatagramPacket receivedPacket = new DatagramPacket(new byte[512], 512);
            Message received = null;
            while(tries < 3) {
                try {
                    socket.receive(receivedPacket);

                    if(!receivedPacket.getAddress().equals(InetAddress.getByName(DNSServerIP))) {
                        throw new IOException("Packet received from unknown source");
                    }

                    byte[] dataReceived = receivedPacket.getData();
                    received = Message.decode(Arrays.copyOfRange(dataReceived, 0, receivedPacket.getLength()));

                    if(received instanceof Query) {
                        continue;
                    }

                    if(received.getRCode() != RCode.NOERROR) {
                        if(received.getRCode() == RCode.NAMEERROR) {
                            throw new NoSuchElementException("Name does not exist");
                        }
                        throw new ValidationException("Bad RCode", "");
                    }
                } catch (InterruptedIOException e) {
                    tries++;
                    continue;
                }
                break;
            }
            if(tries == 3) {
                throw new ValidationException("Maximum retries reached", "");
            }

            //Intellij says this prevents a class cast exception
            assert received instanceof Response;
            Response response = (Response) received; //its stupid i have to do this but I know no better way
            answers.addAll(response.getAnswerList());
            nameservers.addAll(response.getNameServerList());
            additionals.addAll(response.getAdditionalList());

        } catch (SocketException | UnknownHostException e) {
            throw new ValidationException("Error creating socket", "");
        } catch (IOException e) {
            throw new ValidationException("Failure to send", "");
        }


    }
}
