package dcm3203;

import dcm3203.data.FileData;
import dcm3203.data.Model;
import dcm3203.data.Packet;
import dcm3203.data.User;
import dcm3203.network.ConnectionServer;
import dcm3203.network.UDPDiscoveryHandle;
import dcm3203.ui.ConnectDialog;
import dcm3203.ui.GetFileDialog;
import dcm3203.ui.View;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
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

    private boolean         connected;                  //  Used for determining if a connection is being kept
    private boolean         running;                    //  Used for determining if the program should be running

    private final int       connectionPort  = 60023;
    private final int       udpPort         = 60022;

    private final long      loopPauseTime   = 25;       //number of milliseconds to sleep for in each loop
    private final long      hbMax           = 10000;
    private long            heartbeatTime   = hbMax;

    private View            myView;
    private Model           myModel;
    private ConnectDialog   myConnect;

    private final String    resetError      = "connection reset";

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
    }

    public void run() {
        running = true;

        while (running) {
            myConnect = new ConnectDialog(myView, "Connect", true, this);
            myConnect.setVisible(true);
            connected = true;

            // spin off new Thread for UDP discovery handling
            UDPDiscoveryHandle udpDiscoveryHandle = new UDPDiscoveryHandle(udpPort);
            Thread discoverHandle = new Thread(udpDiscoveryHandle);
            discoverHandle.start();

            // spin off new Thread for incoming connection handling (just a shell right now)
            ConnectionServer connectionServer = new ConnectionServer(connectionPort, myView);
            Thread inConnect = new Thread(connectionServer);
            inConnect.start();

            // handle incoming messages from users
            while (connected) {
                long startTime = System.currentTimeMillis();

                HashSet<User> deadUsers = new HashSet<User>();

                Vector<User> newUsers = new Vector<User>(1);
                for (User user : myModel.getUserList()) {
                    try {
                        Packet data = user.readPacket();
                        if (data == null)
                            continue;
                        switch (data.getID()) {
                            case Model.textCode:
                                String message = new String(data.getBytes());
                                myModel.addMessage(message);
                                myView.update();
//                              Toolkit.getDefaultToolkit().beep();
                                break;
                            case Model.connectCode:
                                String host = new String(data.getBytes()).trim();
                                User temp = incomingConnect(host);
                                if (temp != null)
                                    newUsers.add(temp);
                                myView.update();
                                break;
                            case Model.fileAdCode:
                                String fileInfo = new String(data.getBytes());
                                String senderInfo = fileInfo.substring(0, fileInfo.indexOf(FileData.SPLIT_STR));
                                fileInfo = fileInfo.substring(fileInfo.indexOf("\n") + 1, fileInfo.length());
                                FileData newAdFile = new FileData(fileInfo);

                                if (newAdFile.isValid()) {
                                    myModel.addMessage(senderInfo + "File advertisement: " + newAdFile.getFileName());
                                    myModel.addFile(newAdFile);
                                } else {
                                    myModel.addMessage(senderInfo + "Failed to advertise file! Invalid file!");
                                }
                                System.out.println(myModel.printFiles());
                                myView.update();
                                break;
                            case Model.fileReqCode:
                                break;
                            case Model.fileRemoveCode:
                                String remInfo = new String(data.getBytes());
                                String removerInfo = remInfo.substring(0, remInfo.indexOf(FileData.SPLIT_STR));
                                remInfo = remInfo.substring(remInfo.indexOf("\n") + 1, remInfo.length());
                                FileData removeFile = new FileData(remInfo);

                                if (removeFile.isValid()) {
                                    if (myModel.removeFile(removeFile)) {
                                        myModel.addMessage(removerInfo + "File no longer advertised: " + removeFile.getFileName());
                                    } else {
                                        myModel.addMessage(removerInfo + "Failed to remove advertisement on file! Not Removed!");
                                    }
                                } else {
                                    myModel.addMessage(removerInfo + "Failed to remove advertisement on file! Invalid file!");
                                }
                                myView.update();
                                break;
                            case Model.disconnectCode:
                                deadUsers.add(user);
                                break;
                            case Model.heartbeatCode:
                                System.out.println(new String(data.getBytes()));
                                break;
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                for (User newUser : newUsers) {
                    myModel.addUser(newUser);
                    myView.update();
                }

                long endTime = System.currentTimeMillis();
                long diff = endTime - startTime;

                //checking for any dead users by attempting to send a heartbeat message
                if ((heartbeatTime -= Math.max(diff, loopPauseTime)) < 0) {
                    Packet heartBeat = new Packet(Model.heartbeatCode, "heartbeat");
                    for (User user : myModel.getUserList()) {
                        try {
                            user.writePacket(heartBeat);
                        } catch (SocketException error) {
                            System.out.println(error.getMessage());
                            if (error.getMessage().toLowerCase().contains(resetError))
                                deadUsers.add(user);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    heartbeatTime = hbMax;
                }

                for (User user : deadUsers) {
                    handleUserDisconnect(user);
                }

//              System.out.println(diff);
                if (diff <= loopPauseTime) {
                    try {
                        Thread.sleep(loopPauseTime - diff);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                udpDiscoveryHandle.terminate();
                connectionServer.terminate();
            } catch (NullPointerException e) {
                //   Believe this only happens when two instances are on the same computer

                System.out.println(e.getMessage());
                System.exit(1);
            }

            try {
                discoverHandle.join();
                inConnect.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

        }

        System.exit(0);
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
            e.printStackTrace();
            return (false);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return (false);
        }

        DataOutputStream send = new DataOutputStream(newSocket.getOutputStream());
        send.writeBytes("1\n");
        send.writeBytes(myModel.getMyName() + '\n');
        BufferedReader incoming = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));

        String name = incoming.readLine();
        myModel.addUser(new User(name.trim(), newSocket));
        myView.update();
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
        System.out.println(host);
        User newUser = null;
        Socket socket;
        try {
            socket = new Socket(host, connectionPort);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return null;
        }
        DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
        //Send data to the new guy
        System.out.println("Sending data to new guy");
        sender.writeBytes("0\n");     //Tell the client not to have everyone else connect to me
        System.out.println("Sending name to new guy");
        sender.writeBytes(myModel.getMyName() + '\n');
        //receive from the new guy
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String name = reader.readLine();
        newUser = new User(name, socket);
        return newUser;
    }

    public ActionListener getNewConnectListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myConnect.setVisible(true);
            }
        };
    }

    public ActionListener getDisconnectListener() {
        return (new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectSelf();
            }
        });
    }

    public ActionListener getExitListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectSelf();
                endProgram();
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

                    if (fileData.isValid()) {
                        if (Model.getInstance().addFile(fileData)) {
                            System.out.println(myModel.printFiles());

                            // Sends the file advertisement

                            String message = new SimpleDateFormat("[HH:mm:ss] ").format(Calendar.getInstance().getTime());
                            message += myModel.getMyName() + ": ";

                            myModel.addMessage(message + " Advertised: " + fileData.getFileName());
                            myView.update();

                            message += "\n" + fileData.getSendDataString();

                            Packet packet = new Packet(Model.fileAdCode, message);
                            for (User user : myModel.getUserList()) {
                                try {
                                    user.writePacket(packet);
                                } catch (IOException err) {
                                    err.printStackTrace();
                                }
                            }
                        } else {
                            System.out.println("File already advertised");
                        }
                    } else {
                        System.out.println("File is not valid!");
                    }
                } else {
                    System.out.println("Cancelled open");
                }
            }
        };
    }

    public ActionListener getFileTransferRequestListener() {
        return (new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GetFileDialog removeFileDialog = new GetFileDialog(myView, "Request file",
                        true, myModel.getFilesAvailable().getUserFiles());
                FileData fileData = removeFileDialog.getFileSelected();

                // TODO add functionality to send the transfer request
                //    You can get the file path from fileData.getLocation()
            }
        });
    }

    public ActionListener getRemoveFileListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GetFileDialog removeFileDialog = new GetFileDialog(myView, "Remove advertisement",
                        true, myModel.getFilesAvailable().getLocalFiles());
                FileData fileData = removeFileDialog.getFileSelected();

                if (Model.getInstance().removeFile(fileData)) {
                    System.out.println(myModel.printFiles());

                    // Sends a notification that the file has been removed

                    String message = new SimpleDateFormat("[HH:mm:ss] ").format(Calendar.getInstance().getTime());
                    message += myModel.getMyName() + ": ";

                    myModel.addMessage(message + " File Removed: " + fileData.getFileName());
                    myView.update();

                    message += "\n" + fileData.getSendDataString();

                    Packet packet = new Packet(Model.fileRemoveCode, message);
                    for (User user : myModel.getUserList()) {
                        try {
                            user.writePacket(packet);
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("File could not be removed");
                }
            }
        };
    }

    /**
     * action listener to handle the Send message function.
     * @return actionListener
     */
        public ActionListener getSendListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String messageContents = myView.getMessage();

                if(!messageContents.equals("")) {   // To prevent blank messages from being sent
                    String message = new SimpleDateFormat("[HH:mm:ss] ").format(Calendar.getInstance().getTime());
                    message += myModel.getMyName() + ": ";
                    message += messageContents;
                    Packet packet = new Packet(Model.textCode, message);

                    ArrayList<User> deadUsers = new ArrayList<User>();
                    for (User user: myModel.getUserList()) {
                        try {
                            user.writePacket(packet);
                        } catch(SocketException error) {
//                            error.printStackTrace();
                            System.out.println(error.getMessage());
                            if (error.getMessage().toLowerCase().contains(resetError))
                                deadUsers.add(user);
                        } catch (IOException error) {
                            error.printStackTrace();
                        }
                    }

                    for (User user: deadUsers) {
                        handleUserDisconnect(user);
                    }
                    myModel.addMessage(message);
                    myView.update();
                }
            }
        };
    }

    /**
     * Handle removing a user that has disconnected due to app crash.
     * @param user user to remove
     */
    private void handleUserDisconnect(User user) {
        myModel.addMessage(user.getName() + " has disconnected");
        myModel.removeUser(user);
        myView.update();
    }

    /////
    //   Gets the UDP port number used (for the UDPRequester class
    //
    public int getUDPPort() { return (udpPort); }

    private void disconnectSelf() {
        Packet dcPacket = new Packet(Model.disconnectCode, "disconnect");
        for (User user: myModel.getUserList()) {
            try {
                user.writePacket(dcPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        connected = false;
        myModel.removeAllUsers();
        myModel.clearMessages();
        myView.update();
    }

    private void endProgram() { running = false; }
}
