package dcm3203.network;

/**
 * Created by Colin on 2014-10-31.
 *
 * Handles incoming connections, from users that wish to join the room
 */
public class ConnectionServer {
    private int port;

    public ConnectionServer(int port) {
        this.port = port;
    }

    /**
     * constantly loops over actions
     * waits for an incoming connection,
     * accepts the connection,
     * tells all other users to connect to the new one.
     * adds the new user to the user list
     */
    public void run() {

    }
}
