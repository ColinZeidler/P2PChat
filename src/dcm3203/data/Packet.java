package dcm3203.data;

import java.nio.ByteBuffer;
/**
 * Created by Daniel on 30/10/2014.
 */

/**
 * Easier to build a packet of data with an object
 And then just encode that object into a stream
 than it is to create packets individually by passing raw data.
 Something like:
 Packet packet = new Packet(packet_id, data);
 client.writePacket(packet);

 Same way with reading Packets as well:
 Packet packet = client.readPacket();
 switch(packet.getId()) {
 case PACKET_ID0:
 //Handle packet 0 (Handle a string)
 case PACKET_ID1:
 //Handle packet 1(Handle a file type)
 break;

 default:
 //Invalid packet
 break;
 }

 client.readPacket():
 Packet readPacket() {
 //read a byte from stream [id]
 //read a short from stream [packet size]
 //read data into a byte array
 return new Packet(id, data);
 }
 */
public class Packet {
    private int ID;
    private byte[] data;

    public Packet(int id, byte[] data){
        ID = id;
        this.data = data;
    }

    public Packet(int id, String data) {
        this(id, data.getBytes());
    }

    public int getID(){return ID;}
    public byte[] getBytes() {
        return data;
    }
    public int getDataLength() {
        return data.length;
    }
}
