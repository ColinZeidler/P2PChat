package dcm3203.network;

import dcm3203.data.Model;
import dcm3203.data.Packet;
import dcm3203.data.User;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Colin on 2014-12-14.
 *
 */
public class SendFileThread implements Runnable {

    private FileInputStream fi;
    private User dest;
    private String fileName;

    public SendFileThread(String fileName, String fileLocation, User dest) throws FileNotFoundException {
        this.fileName = fileName;
        fi = new FileInputStream(fileLocation);
        this.dest = dest;
    }

    @Override
    public void run() {
        byte[] bytes = new byte[4096];
        byte[] fileName = this.fileName.getBytes();
        Packet fileData;
        fileData = new Packet(Model.fileStartCode, fileName);
        try {
            dest.writePacket(fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int size;
        try {
            while ((size = fi.read(bytes)) != -1) {

                //create the sendbytes array with filename +  + file data
                byte[] sendBytes = new byte[size+fileName.length+1];
                System.arraycopy(fileName, 0, sendBytes, 0, fileName.length);
                System.arraycopy(bytes, 0, sendBytes, fileName.length+1, size);

                fileData = new Packet(Model.fileCode, sendBytes);
                dest.writePacket(fileData);
            }
        } catch(IOException e) {
            System.out.println("unable to read file");
        }

        fileData = new Packet(Model.fileEndCode, fileName);
        try {
            dest.writePacket(fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Transfer Complete");
    }
}
