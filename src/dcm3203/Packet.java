package dcm3203;

/**
 * Created by Daniel on 30/10/2014.
 */

/**
 * Easier to build a packet of data with an object
 And then just encode that object into a stream
 than it is to create packets individually by passing raw data.
 Something like:
 Packet packet = new Packet(packet_id);
 packet.putData(...);

 client.writePacket(packet);
 is easier than like:
 byte[] data = new byte[some_size];
 data[0] = packet_id;
 data[1] = some_data0;
 data[2] = some_data1;
 ...

 client.writePacket(data)

 Same way with reading Packets as well:
 Packet packet = client.readPacket();
 switch(packet.getId()) {
 case PACKET_ID0:
 //Handle packet 0
 break;

 default:
 //Invalid packet
 break;
 }
 */
public class Packet {
    private int ID;

    public int getID(){return ID;}
}
