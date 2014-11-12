package dcm3203.network;

import dcm3203.data.Model;
import dcm3203.data.Packet;
import dcm3203.data.User;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Colin on 2014-10-31.
 *
 * Handles incoming connections, from users that wish to join the room
 */
public class ConnectionServer implements Runnable{
    private int port;
    private Model myModel;
    private ServerSocket socket;

    public ConnectionServer(int port) {
        this.port = port;
        myModel = Model.getInstance();
    }

    /**
     * constantly loops over actions
     * waits for an incoming connection,
     * accepts the connection,
     * tells all other users to connect to the new one.
     * adds the new user to the user list
     */
    @Override
    public void run() {
        System.out.println("temp out, nothing actually done");
        System.out.println("Port is: " + this.port);

        try {
            socket = new ServerSocket(port);

            while (true) {
                Socket newSocket = socket.accept();
                //get name from newSocket

                BufferedReader incoming = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
                String da = incoming.readLine();
                System.out.println(da);
                int newToTheRoom = Integer.parseInt(da); //get the status int, from person connecting
                System.out.println(newToTheRoom);
                String name = incoming.readLine();
                DataOutputStream send = new DataOutputStream(newSocket.getOutputStream());
                send.writeBytes(myModel.getMyName() + '\n');

                if (newToTheRoom == 1) {
//                    send.writeInt(myModel.getUserList().size());
                    for (User user : myModel.getUserList()) {
                        System.out.println("telling " + user.getName() + "to connect to new guy");
                        Packet data = new Packet(Model.connectCode,
                                newSocket.getInetAddress().getHostAddress().getBytes());
                        user.writePacket(data);
                    }
                }

                //add user to user list
                myModel.addUser(new User(name.trim(), newSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
