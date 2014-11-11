package dcm3203.data;

import java.io.DataInputStream;
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
    private DataInputStream receiveStream;

    public User(String name, Socket connection) throws IOException {
        this.name = name;
        this.connection = connection;
        
        if (connection != null) {
            sendStream = new DataOutputStream(connection.getOutputStream());
            receiveStream = new DataInputStream(connection.getInputStream());
        }
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

    public void writePacket(Packet packet) throws IOException{
        //TEMP
        sendStream.writeInt(packet.getID());
        sendStream.write(packet.getData().get());
    }

    public Packet readPacket() throws IOException{
            if(receiveStream.available() > 3) {
                int sread = receiveStream.read();
                if(sread == -1) {
                    //End of file. PANIC!
                }
                byte id = (byte) sread;
                int size = (receiveStream.read() << 8) | receiveStream.read();
                byte[] data = new byte[size];

            int off = 0;
            while(off != size) {
                int read = receiveStream.read(data, off, size - off);
                off += read;
            }

            return new Packet(id, data);
        }
        return null;

    }

    public void sendText(String message) throws IOException {
        String timestamp = User.getCurrentTimeString();
        message = Model.getInstance().getMyName() + " " + timestamp + ": " + message + '\n';
        Packet packet = new Packet(0, message.getBytes());
        writePacket(packet);
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
