package dcm3203.network;

import java.io.IOException;
import java.net.*;

/**
 * Created by Michael on 31/10/2014.
 *
 * UDPDiscoveryHandle is used to handle discovery
 */
public class UDPDiscoveryHandle implements Runnable {
    private int port;
    private DatagramSocket socket;
    private static final String discoverRequest = "p2p_chat_discovery_packet";
    private static final String discoverResponse = "p2p_chat_discovery_response";

    public UDPDiscoveryHandle(int port) {
        this.port = port;
    }

    //  Needed to add for use within the UDPRequester class
    public static String getDiscoverRequestString() { return (discoverRequest); }
    public static String getDiscoverResponseString() { return (discoverResponse); }

    /**
     * constantly loops over actions
     * waits for a UDP packet
     * checks if the message is the discovery message
     * if yes, reply with reply message (and user count)
     */
    @Override
    public void run(){
        System.out.println("Port is: " + this.port);
        try {
            socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);

            while (true) {
                //receive packet
                byte[] recvBuff = new byte[4096];
                DatagramPacket packet = new DatagramPacket(recvBuff, recvBuff.length);
                socket.receive(packet);

                //get data from packet
                String message = new String(packet.getData()).trim();
                if (message.equals(discoverRequest)) {
                    byte[] sendData = discoverResponse.getBytes();

                    //send response to origin
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    socket.send(sendPacket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
