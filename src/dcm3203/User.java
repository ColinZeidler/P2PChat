package dcm3203;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.text.SimpleDateFormat;

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

    private static String getCurrentTimeString() {
        StringBuilder bldr = new StringBuilder("[");

        bldr.append(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));

        return bldr.append("]").toString();
    }
    //This might be completely wrong and stupid
    //Not tested
    public void sendMessage(String message) throws IOException {
        PrintWriter send = new PrintWriter(connection.getOutputStream(), true);
        String timestamp = this.getCurrentTimeString();
        message = name + " " + timestamp + ": " + message;
        send.print(message);
        send.close();
    }

    @Override
    public String toString() {
        return name;
    }
}
