package dcm3203.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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

    /**
     * writes the data for a packet object onto the Sockets outgoing stream
     *
     * Use:
     * Packet packet = new Packet(Model.Code, message.getBytes());
     * writePacket(packet);
     *
     * @param packet the packet object to send.
     * @throws IOException
     */
    public void writePacket(Packet packet) throws IOException{
        //TEMP
        sendStream.writeInt(packet.getID());
        sendStream.writeInt(packet.getDataLength());
        sendStream.write(packet.getBytes());
    }

    /**
     * Creates a Packet object from the incoming stream of data
     * @return Packet object containing all of the Data
     * @throws IOException
     */
    public Packet readPacket() throws IOException{
        if(receiveStream.available() > 3) {
            int id = receiveStream.readInt();
//            if(id == -1) {
//                //End of file. PANIC!
//            }
            int size = receiveStream.readInt();
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

    @Override
    public String toString() {
        return name;
    }
}
