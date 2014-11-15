package dcm3203;

import dcm3203.data.FileData;
import dcm3203.data.Model;
import dcm3203.data.Packet;
import dcm3203.data.User;
import dcm3203.network.ConnectionServer;
import dcm3203.network.UDPDiscoveryHandle;
import dcm3203.ui.ConnectDialog;
import dcm3203.ui.RemoveFileDialog;
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
    final String resetError = "connection reset";
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
                            System.out.println("I am in the ConnectCode");
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
                                myModel.addMessage(senderInfo + " File advertisement: " + newAdFile.getFileName());
                                myModel.addFile(newAdFile);
                            } else {
                                myModel.addMessage(senderInfo + " Failed to advertise file!");
                            }
                            System.out.println(myModel.printFiles());
                            myView.update();
                            break;
                        case Model.fileReqCode: break;
                        case Model.fileRemoveCode:
                            String remInfo = new String(data.getBytes());
                            String removerInfo = remInfo.substring(0, remInfo.indexOf(FileData.SPLIT_STR));
                            remInfo = remInfo.substring(remInfo.indexOf("\n") + 1, remInfo.length());
                            FileData removeFile = new FileData(remInfo);

                            if (removeFile.isValid()) {
                                if (myModel.removeFile(removeFile)) {
                                    myModel.addMessage(removerInfo + " File no longer advertised: " + removeFile.getFileName());
                                } else {
                                    myModel.addMessage(removerInfo + " Failed to remove advertisement on file!");
                                }
                            } else {
                                myModel.addMessage(removerInfo + " Failed to remove advertisement on file!");
                            }
                            myView.update();
                            break;
                        case Model.disconnectCode: break;
                    }
                } catch(SocketException e){
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            for (User newUser: newUsers) {
                myModel.addUser(newUser);
                myView.update();
            }

            long endTime = System.currentTimeMillis();
            long diff = endTime - startTime;
//            System.out.println(diff);
            if (diff <= loopPauseTime) {
                try {
                    Thread.sleep(loopPauseTime - diff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

                    if (fileData.isValid()) {
                        Model.getInstance().addFile(fileData);
                        System.out.println(myModel.printFiles());

                        // Sends the file advertisement

                        String message = myModel.getMyName();
                        message += new SimpleDateFormat(" [HH:mm:ss]: ").format(Calendar.getInstance().getTime());

                        myModel.addMessage(message + " Advertised: " + fileData.getFileName());

                        message += "\n" + fileData.getSendDataString();

                        Packet packet = new Packet(Model.fileAdCode, message);
                        for (User user: myModel.getUserList()) {
                            try {
                                user.writePacket(packet);
                            } catch (IOException err) {
                                err.printStackTrace();
                            }
                        }
                    } else {
                        // file is not valid?
                        System.out.println("File is not valid!");
                    }
                } else {
                    System.out.println("Cancelled open");
                }
            }
        };
    }

    public ActionListener getRemoveFileListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RemoveFileDialog removeFileDialog = new RemoveFileDialog(myView, "Remove advertisement",
                        true, myModel.getFilesAvailable().localFiles());


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
                    String message = myModel.getMyName();
                    message += new SimpleDateFormat(" [HH:mm:ss]: ").format(Calendar.getInstance().getTime());
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
                        handleConnectionReset(user);
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
    private void handleConnectionReset(User user) {
        myModel.addMessage(user.getName() + " has disconnected");
        myModel.removeUser(user);
        myView.update();
    }
}
