package dcm3203.network;

import dcm3203.ui.ConnectDialog;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by Michael on 24/11/2014.
 *
 * // TODO test the shit out of this
 *
 * UDPRequester
 *      This class sends out the UDP requests to all on the local network
 *
 *      USE:
 *          Start the thread and use terminate() to end the thread safely
 *
 */
public class UDPRequester implements Runnable {

    private volatile boolean    running;                    //  Used to determine whether to exit the thread

    private final static int    LISTEN_TIMEOUT = 10000;     //  So the socket times out after a while to resend
    private final static int    BUFFER_SIZE    = 16000;

    private int                 udpPort;                    //  The port that peers listen for request on
    private ConnectDialog       owner;                      //  To send the list to the dialog to update

    private DatagramSocket      socket;                     //  The socket to send the requests with

    public UDPRequester(int udpPort, ConnectDialog owner) {
        this.udpPort = udpPort;
        this.owner = owner;
    }

    /////
    //   This function sets the running boolean to false to safely exit from the while in run() and
    //  closes the socket to prevent it from being open when it is no longer needed
    //
    public void terminate() {
        socket.close();
        running = false;
    }

    /////
    //   Runs the search for local IPs
    //
    public void run() {
        Vector<String> foundIPs = new Vector<String>();     //  This stores the currently found IPs
        running = true;                                     //  Sets to true so the while will run until terminated

        try {

            /////
            //   First sets up the socket to a random port and creates the request to send
            //

            socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(LISTEN_TIMEOUT);

            byte[] send = UDPDiscoveryHandle.getDiscoverRequestString().getBytes();

            while (running) {

                /////
                //   Goes through the network and finds all IPs to send the requests to
                //

                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {

                    NetworkInterface networkInterface = interfaces.nextElement();

                    if (!networkInterface.isLoopback() && networkInterface.isUp() && !networkInterface.isVirtual()) {

                        /////
                        //   Goes through each IP in the network interface
                        //

                        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                        while(addresses.hasMoreElements()) {
                            InetAddress inetAddress = addresses.nextElement();
                            if (inetAddress != null) {

                                /////
                                //   Checks to see if the address if an IPv4 since that is what we are working with
                                //  Then checks to see if the address has already been added to the list of found IPs
                                //  As to not send a request to an address that has already responded
                                //

                                if (inetAddress instanceof Inet4Address) {
                                    if (!foundIPs.contains(inetAddress.getHostAddress())) {
                                        try {
                                            DatagramPacket sendPacket = new DatagramPacket(send, send.length,
                                                    inetAddress, udpPort);
                                            socket.send(sendPacket);
                                            System.out.println("Sent packet to " + inetAddress.getHostAddress());
                                            System.out.println("               " + networkInterface.getDisplayName());
                                        } catch (Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                /////
                //   Waits for a response from the address that a request was sent to
                //     NOTE: Will time out after a while as to send requests again, it will also find
                //    any new addresses on the network
                //

                DatagramPacket receive = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                try {
                    socket.receive(receive);
                } catch (SocketTimeoutException e) {
                    System.out.println(e.getMessage());
                    continue;
                }

                /////
                //   Now that a response was received we check if valid then add the address to the list
                //  of found IPs so long as it does not already exist within the list (just encase two or
                //  more requests got sent before one response was sent back)
                //

                System.out.println("Received packet from " + receive.getAddress().getHostAddress());

                String message = new String(receive.getData()).trim();
                if (message.equals(UDPDiscoveryHandle.getDiscoverResponseString())) {
                    String ip = receive.getAddress().toString().substring(1);
                    if (!foundIPs.contains(ip)) {
                        foundIPs.add(ip);
                        owner.updateList(foundIPs);
                    }
                } else {
                    System.out.println("Invalid response received");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Terminated");
    }

}
