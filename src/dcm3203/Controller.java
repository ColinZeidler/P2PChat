package dcm3203;

import dcm3203.data.Model;
import dcm3203.data.Packet;
import dcm3203.data.User;
import dcm3203.network.ConnectionServer;
import dcm3203.network.UDPDiscoveryHandle;
import dcm3203.ui.ConnectDialog;
import dcm3203.ui.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 * Created by Colin on 2014-10-28.
 *
 * Main Control class
 * handles the start of the program and the overall control flow,
 *
 * contains actionHandlers for the View class
 *
 * Updates the data Model, and then tells the View class to update
 */
public class Controller {
    private View myView;
    private Model myModel;
    private ConnectDialog myConnect;
    private final int connectionPort = 60023, udpPort = 60022;
    private final long loopPauseTime = 25;  //number of milliseconds to sleep for in each loop
    /**
     * Entry method
     * @param args command line args, ignored
     */
    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.run();
    }

    public Controller() {
        myModel = Model.getInstance();
        myView = new View(this);
        myConnect = new ConnectDialog(myView, "Connect", true, this);
    }

    public void run() {

        // spin off new Thread for UDP discovery handling
        Thread discoverHandle = new Thread(new UDPDiscoveryHandle(udpPort));
        discoverHandle.start();

        myConnect.setVisible(true);

        System.out.println("TEST BY MOOP"); // TODO get rid jsut to show

        // spin off new Thread for incoming connection handling (just a shell right now)
        Thread inConnect = new Thread(new ConnectionServer(connectionPort));
        inConnect.start();

        // handle incoming messages from users
        while (true) {
            long startTime = System.currentTimeMillis();

            Vector<User> newUsers = new Vector<User>(1);
            for (User user: myModel.getUserList()) {
//                BufferedReader fromUser = null;
                DataInputStream fromUser = null;
                try {
//                     fromUser = new BufferedReader(new InputStreamReader(user.getConnection().getInputStream()));
                    fromUser = new DataInputStream(user.getConnection().getInputStream());
                } catch (NullPointerException e) {
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (fromUser != null) {
                    try {
                        Packet data = readPacket(fromUser);
                        switch (data.getID()) {
                            case Model.textCode:
                                String message = data.getData().toString();
                                myModel.addMessage(message);
                                myView.update();
                                break;
                            case Model.connectCode:
                                String host = data.getData().toString();
                                User temp = incomingConnect(host);
                                if (temp != null)
                                    newUsers.add(temp);
                                for (User newUser: newUsers) {
                                    myModel.addUser(newUser);
                                }
                                myView.update();
                                break;
                            case Model.fileAdCode: break;
                            case Model.fileReqCode: break;
                        }
                    } catch (SocketTimeoutException e) { //Socket will timeout if there is no data to receive
                        continue;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            long diff = endTime - startTime;
//            System.out.println(diff);
            if (diff <= loopPauseTime) {
                try {
                    Thread.sleep(loopPauseTime - diff);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
        }
    }

    /**
     * Connect to a remote room,
     * Is called when the user enters or selects an IP then clicks Join
     * @param ip the ip of the room to connect to
     * @return success value, true if connection worked, false if failed.
     * @throws IOException
     */
    public boolean setupConnection(String ip) throws IOException{
        Socket newSocket;

        try {
            newSocket = new Socket(ip, connectionPort);
        } catch (UnknownHostException e) {
            return (false);
        }

        DataOutputStream send = new DataOutputStream(newSocket.getOutputStream());
        send.writeInt(1);
        send.close();
        BufferedReader incoming = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));

        String name = incoming.readLine();
        incoming.close();
        Model.getInstance().addUser(new User(name, newSocket));

        return(true);
    }

    /**
     * Handles incoming message that is flagged as a request to connect to a new user
     * receives an IP address from a remote client,
     * and then connects to it, gets the new users name and creates a User object
     *
     * @param host the input stream from the User sending the message
     * @return The User that we just connected to
     * @throws IOException
     */
    private User incomingConnect(String host) throws IOException{
        User newUser = null;
        Socket socket;
        try {
            socket = new Socket(host, connectionPort);
        } catch (UnknownHostException e) {
            return null;
        }
        DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
        //Send data to the new guy
        sender.writeInt(0);     //Tell the client not to have everyone else connect to me
        sender.writeBytes(myModel.getMyName() + '\n');
        sender.close();
        //receive from the new guy
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String name = reader.readLine();
        reader.close();
        newUser = new User(name, socket);
        return newUser;
    }

    /**
     * Creates a Packet object from the incoming stream of data
     * @param fromUser the data stream to receive from.
     * @return Packet object containing all of the Data
     * @throws IOException
     */
    private Packet readPacket(DataInputStream fromUser) throws IOException {
        Packet packet = null;

        int type = fromUser.readInt();
        int bufferSize = fromUser.readInt();

        byte[] bytes = new byte[bufferSize];

        int result = fromUser.read(bytes);

        packet = new Packet(type, bytes);
        return packet;
    }

    public ActionListener getSendListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = myView.getMessage();
                myModel.addMessage(message);
                myView.update(); 
            }
        };
    }
}
