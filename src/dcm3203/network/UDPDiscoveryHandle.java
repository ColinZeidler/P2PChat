package dcm3203.network;

/**
 * Created by Michael on 31/10/2014.
 *
 * UDPDiscoveryHandle is used to handle discovery
 */
public class UDPDiscoveryHandle implements Runnable {
    private int port;

    public UDPDiscoveryHandle(int port) {
        this.port = port;
    }

    /**
     * constantly loops over actions
     * waits for a UDP packet
     * checks if the message is the discovery message
     * if yes, reply with reply message (and user count)
     */
    public void run(){
        // TODO add code to handle discovery
        System.out.println("temp out, nothing actually done");
        System.out.println("Port is: " + this.port);
    }

}
