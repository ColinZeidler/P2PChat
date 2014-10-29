package dcm3203;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Colin on 2014-10-29.
 */
public class User {
    private String name;
    private Socket connection;

    public User(String name, Socket connection) {
        this.name = name;
        this.connection = connection;
    }

    public Socket getConnection() {
        return connection;
    }

    public String getName() {
        return name;
    }

    public void sendMessage(String message) throws IOException {
        PrintWriter send = new PrintWriter(connection.getOutputStream(), true);
        send.print(message);
        send.close();
    }
}
