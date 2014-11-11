package dcm3203;

import dcm3203.data.FileData;
import dcm3203.data.Model;
import dcm3203.data.Packet;
import dcm3203.data.User;
import dcm3203.network.ConnectionServer;
import dcm3203.network.UDPDiscoveryHandle;
import dcm3203.ui.ConnectDialog;
import dcm3203.ui.View;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
                try {
                    Packet data = user.readPacket();
                    if (data == null)
                        continue;
                    switch (data.getID()) {
                        case Model.textCode:
                            String message = new String(data.getBytes());
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
                } catch (IOException e) {
                    e.printStackTrace();
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
        } catch (SocketTimeoutException e) {
            return (false);
        }

        DataOutputStream send = new DataOutputStream(newSocket.getOutputStream());
        send.writeInt(1);
        send.writeBytes(myModel.getMyName() + '\n');
        BufferedReader incoming = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));

        String name = incoming.readLine();
        newSocket.setSoTimeout(1);
        Model.getInstance().addUser(new User(name.trim(), newSocket));
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
        //receive from the new guy
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String name = reader.readLine();
        newUser = new User(name, socket);
        return newUser;
    }

    public ActionListener getConnectDialogListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myConnect.setVisible(true);
            }
        };
    }

    public ActionListener getExitListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // TODO needs to clean up, not proper exit
            }
        };
    }

    public ActionListener getFileListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFileChooser fc = new JFileChooser();

                int rVal = fc.showOpenDialog(myView);

                if (rVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    FileData fileData = new FileData(file.getName(), file.getPath());

                    System.out.println("My String: " + fileData.getDataString());
                    System.out.println("Send String: " + fileData.getSendDataString());
                } else {
                    System.out.println("Cancelled open");
                }
            }
        };
    }

    public ActionListener getSendListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String messageContents = myView.getMessage();

                if(!messageContents.equals("")) {   // To prevent blank messages from being sent
                    String message = myModel.getMyName();
                    message += new SimpleDateFormat(" [HH:mm:ss]: ").format(Calendar.getInstance().getTime());
                    message += messageContents;
                    Packet packet = new Packet(Model.textCode, message.getBytes());
                    for (User user: myModel.getUserList()) {
                        try {
                            user.writePacket(packet);
                        } catch (IOException error) {}
                    }
                    myModel.addMessage(message);
                    myView.update();
                }
            }
        };
    }
}
