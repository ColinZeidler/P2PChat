package dcm3203.data;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 * Created by Colin on 2014-10-29.
 *
 * Stores data related to a single user: Name, Socket, and OutputStream
 *
 * handles the sending of messages to the User this object represents.
 */
public class User {
    private String name;
    private Socket connection;
    private DataOutputStream sendStream;

    public User(String name, Socket connection) throws IOException {
        this.name = name;
        this.connection = connection;
        
        if (connection != null)
            sendStream = new DataOutputStream(connection.getOutputStream());
    }

    public Socket getConnection() {
        return connection;
    }

    public String getName() {
        return name;
    }

    public void sendText(String message) throws IOException {
        sendStream.writeInt(Model.textCode);
        sendStream.writeBytes(message);
    }

    public void sendConnect(String address) throws IOException {
        sendStream.writeInt(Model.connectCode);
        sendStream.writeBytes(address + '\n');
    }

    public void sendFileAd(String filename) throws IOException {
        sendStream.writeInt(Model.fileAdCode);
        sendStream.writeBytes(filename + '\n');
    }

    public void sendFileRequest() throws IOException {
        sendStream.writeInt(Model.fileReqCode);
    }

    public void sendFile() throws IOException {
        //send filesize
        //send file by reading in X bytes at once.
    }

    @Override
    public String toString() {
        return name;
    }
}
