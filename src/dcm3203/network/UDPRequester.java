package dcm3203.network;

import dcm3203.ui.ConnectDialog;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by Michael on 24/11/2014.
 *
 */
public class UDPRequester implements Runnable {

    private volatile boolean    running;

    private final static int    listenTimeout = 10000;

    private int                 udpPort;
    private ConnectDialog       owner;

    public UDPRequester(int udpPort, ConnectDialog owner) {
        this.udpPort = udpPort;
        this.owner = owner;
    }

    public void terminate() {
        running = false;
    }

    public void run() {
        Vector<String> foundIPs = new Vector<String>();
        running = true;

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            byte[] send = UDPDiscoveryHandle.getDiscoverRequestString().getBytes();

            try {
                DatagramPacket sendPacket = new DatagramPacket(send, send.length,
                        InetAddress.getByName("255.255.255.255"), udpPort);
                socket.send(sendPacket);
                System.out.println("Sent packet to 255.255.255.255");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            while (running) {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements() && running) {
                    NetworkInterface networkInterface = interfaces.nextElement();

                    if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()){
                            InetAddress broadcast = interfaceAddress.getBroadcast();
                            if (broadcast != null) {
                                try {
                                    DatagramPacket sendPacket = new DatagramPacket(send, send.length, broadcast, udpPort);
                                    socket.send(sendPacket);
                                } catch (Exception e) {
                                    System.out.println(e.getMessage());
                                }
                                System.out.println("Sent packet to " + broadcast.getHostAddress());
                                System.out.println("               " + networkInterface.getDisplayName());
                            }
                        }
                    }
                }
                running = false; // TODO remove for testing
            }
        } catch (IOException e) {

        }

        System.out.println("Terminated");
    }

}
