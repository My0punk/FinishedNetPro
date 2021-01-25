package sdns.app.udp.client.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @author Ethan
 * Creates a test server for a UDP DNS client
 */
public class TestUDPEchoServer {
    //Echo max is max size of byte buffer (DNS UDP <= 512)
    //NUM_TESTS is number of tests to run
    //TIMEOUT_TIME is the amount of time it takes to timeout the client (to abuse for testing purposes)
    private static final int ECHOMAX = 512, NUM_TESTS = 24, TIMEOUT_TIME = 3000;
    //Current test allows switching between tests between subsequent connections
    //It starts at -1 because the first test tests the double timeout, so it needs 2 runs
    private static int currTest = -1;

    /**
     * Tests response delay > 3 seconds
     * @param sout socket to write to
     */
    private static void testTimeout(DatagramSocket sout, InetAddress servAddr, int servPort){
        try {
            Thread.sleep(TIMEOUT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This contains a list of bad buffers to test (along with why they fail)
     */
    private static final byte[][] badBuffs = {
            {//0:Test single byte response, which should throw some sort of Validation exception (or too short)
                0
            }, {//1:Basic header test error short query name
                0, 0,//id
                0, 0, //0 0000 [ignored bit]x4 000 0000
                0, 1, //0x0001
                0, 0, //ANCount
                0, 0, //NSCount
                0, 0, //ARCount
                3, 'f', 'o', -64, 5,//query
                0, -1,//0x00FF
                0, 1  //0x0001
            }, {//2:Basic header test error short query name ending
                0, 0,//id
                0, 0, //0 0000 [ignored bit]x4 000 0000
                0, 1, //0x0001
                0, 0, //ANCount
                0, 0, //NSCount
                0, 0, //ARCount
                3, 'f', 'o', 'o', -64,//query
                0, -1,//0x00FF
                0, 1  //0x0001
            }, {//3:Basic header test error short query name beginning
                0, 0,//id
                0, 0, //0 0000 [ignored bit]x4 000 0000
                0, 1, //0x0001
                0, 0, //ANCount
                0, 0, //NSCount
                0, 0, //ARCount
                'f', 'o', 'o', -64, 5,//query
                0, -1,//0x00FF
                0, 1  //0x0001
            }, {//4:Basic header test error short query 0x00FF
                0, 0,//id
                0, 0, //0 0000 [ignored bit]x4 000 0000
                0, 1, //0x0001
                0, 0, //ANCount
                0, 0, //NSCount
                0, 0, //ARCount
                3, 'f', 'o', 'o', -64, 5,//query
                0,//0x00FF
                0, 1  //0x0001
            }, { //5:Basic header test error short query 0x00FF
                0, 0,//id
                0, 0, //0 0000 [ignored bit]x4 000 0000
                0, 1, //0x0001
                0, 0, //ANCount
                0, 0, //NSCount
                0, 0, //ARCount
                3, 'f', 'o', 'o', -64, 5,//query
                -1,//0x00FF
                0, 1  //0x0001
            }, { //6:Basic header test error short query 0x0001
                0, 0,//id
                0, 0, //0 0000 [ignored bit]x4 000 0000
                0, 1, //0x0001
                0, 0, //ANCount
                0, 0, //NSCount
                0, 0, //ARCount
                3, 'f', 'o', 'o', -64, 5,//query
                0, -1,//0x00FF
                0  //0x0001
            }, { //7:Basic header test error short query 0x0001
                0, 0,//id
                0, 0, //0 0000 [ignored bit]x4 000 0000
                0, 1, //0x0001
                0, 0, //ANCount
                0, 0, //NSCount
                0, 0, //ARCount
                3, 'f', 'o', 'o', -64, 5,//query
                0, -1,//0x00FF
                1  //0x0001
            }
    };

    /**
     * This contains a list of invalid buffers to test (along with why they fail)
     */
    private static final byte[][] invalidBuffs = {
        { //0:Basic query test invalid 0x0001
            0, 0,//id
            0, 0, //0 0000 [ignored bit]x4 000 0000
            0, 2, //0x0001
            0, 0, //ANCount
            0, 0, //NSCount
            0, 0, //ARCount
            3, 'f', 'o', 'o', -64, 5,//query
            0, -1,//0x00FF
            0, 1  //0x0001
        }, { //1: Basic query test invalid 0x0001
            0, 0,//id
            0, 0, //0 0000 [ignored bit]x4 000 0000
            1, 1, //0x0001
            0, 0, //ANCount
            0, 0, //NSCount
            0, 0, //ARCount
            3, 'f', 'o', 'o', -64, 5,//query
            0, -1,//0x00FF
            0, 1  //0x0001
        }, { //2: Basic query test invalid ANCount
            0, 0,//id
            0, 0, //0 0000 [ignored bit]x4 000 0000
            0, 1, //0x0001
            0, 1, //ANCount
            0, 0, //NSCount
            0, 0, //ARCount
            3, 'f', 'o', 'o', -64, 5,//query
            0, -1,//0x00FF
            0, 1  //0x0001
        }, { //3: Basic query test invalid NSCount
            0, 0,//id
            0, 0, //0 0000 [ignored bit]x4 000 0000
            0, 1, //0x0001
            0, 0, //ANCount
            0, 1, //NSCount
            0, 0, //ARCount
            3, 'f', 'o', 'o', -64, 5,//query
            0, -1,//0x00FF
            0, 1  //0x0001
        }, { //4: Basic query test invalid ARCount
            0, 0,//id
            0, 0, //0 0000 [ignored bit]x4 000 0000
            0, 1, //0x0001
            0, 0, //ANCount
            0, 0, //NSCount
            0, 1, //ARCount
            3, 'f', 'o', 'o', -64, 5,//query
            0, -1,//0x00FF
            0, 1  //0x0001
        }
    };

    /**
     * Returns the next invalid buffer to test
     * @return next invalid buffer to test
     */
    private static byte[] invalidHeaderByte1(){
        byte in;
        switch(currTest%4){
            case 0: in = 64; break;
            case 1: in = 32; break;
            case 2: in = 16; break;
            default: in = 8; break;
        }
        return new byte[]{0, 0,//id
                in, 0, //0 0001 [ignored bit]x4 000 0000
                0, 1, //0x0001
                0, 0, //ANCount
                0, 0, //NSCount
                0, 1, //ARCount
                3, 'f', 'o', 'o', -64, 5,//query
                0, -1,//0x00FF
                0, 1  //0x0001
        };
    }

    /**
     * Returns the next invalid buffer to test
     * @return next invalid buffer to test
     */
    private static byte[] invalidHeaderByte2(){
        byte in;
        switch(currTest%7){
            case 0: in = 64; break;
            case 1: in = 32; break;
            case 2: in = 16; break;
            case 3: in = 8; break;
            case 4: in = 4; break;
            case 5: in = 2; break;
            default: in = 1; break;
        }
        return new byte[]{0, 0,//id
                0, in, //0 0001 [ignored bit]x4 000 0000
                0, 1, //0x0001
                0, 0, //ANCount
                0, 0, //NSCount
                0, 1, //ARCount
                3, 'f', 'o', 'o', -64, 5,//query
                0, -1,//0x00FF
                0, 1  //0x0001
        };
    }


    /**
     * Decides which packet to send next
     * @param sout socket to write to
     * @param servAddr server address
     * @param servPort server port
     */
    private static void sendNextPacket(DatagramSocket sout, InetAddress servAddr, int servPort){
        byte[] buff = null;
        switch(currTest){
            case -1:
            case 0: testTimeout(sout, servAddr, servPort); break;
            case 1: buff = badBuffs[0]; break;
            case 2: buff = badBuffs[1]; break;
            case 3: buff = badBuffs[2]; break;
            case 4: buff = badBuffs[3]; break;
            case 5: buff = badBuffs[4]; break;
            case 6: buff = badBuffs[5]; break;
            case 7: buff = badBuffs[6]; break;
            case 8: buff = badBuffs[7]; break;
            case 9: buff = invalidBuffs[0]; break;
            case 10: buff = invalidBuffs[1]; break;
            case 11: buff = invalidBuffs[2]; break;
            case 12: buff = invalidBuffs[3]; break;
            case 13: buff = invalidBuffs[4]; break;
            case 14://these will switch based off of curr test
            case 15:
            case 16:
            case 17: buff = invalidHeaderByte1(); break;
            case 18://these will switch based off of curr test
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24: buff = invalidHeaderByte2(); break;
        }
        if(buff != null){
            DatagramPacket p = new DatagramPacket(buff, buff.length, servAddr, servPort);
            try {
                sout.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        currTest++;
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 1) { // Test for correct argument list
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }

        int servPort = Integer.parseInt(args[0]);

        DatagramSocket socket = new DatagramSocket(servPort);
        DatagramPacket packet = new DatagramPacket(new byte[ECHOMAX], ECHOMAX);

        while (currTest <= NUM_TESTS) { // Run until run out of tests, receiving datagrams and sending back trash
            socket.receive(packet); // Receive packet from client
            System.out.println("Handling client at " + packet.getAddress().getHostAddress() + " on port " + packet.getPort());
            sendNextPacket(socket, packet.getAddress(), packet.getPort());
        }
    }
}
