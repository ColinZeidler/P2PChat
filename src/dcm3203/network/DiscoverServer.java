package dcm3203.network;

/**
 * Created by Colin on 2014-10-31.
 *
 * Handles incoming UDP packets from users trying to discover the network
 */
public class DiscoverServer {
    private int port;

    public DiscoverServer(int port) {
        this.port = port;
    }

    /**
     * waits for incoming UDP packet
     * checks for correct message
     * if correct reply with response message. (and user count)
     */
    public void run() {

    }
}
