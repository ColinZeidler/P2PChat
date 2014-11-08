package dcm3203;

import dcm3203.data.Model;
import dcm3203.data.User;
import dcm3203.network.ConnectionServer;
import dcm3203.network.UDPDiscoveryHandle;
import dcm3203.ui.ConnectDialog;
import dcm3203.ui.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 * Created by Colin on 2014-10-28.
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
        myConnect = new ConnectDialog(myView, "Connect", true);
        myConnect.setVisible(true);
    }

    public void run() {

        //TODO spin off new Thread for incoming connection handling (just a shell right now)
        Thread inConnect = new Thread(new ConnectionServer(connectionPort));
        inConnect.start();

        // spin off new Thread for UDP discovery handling
        Thread discoverHandle = new Thread(new UDPDiscoveryHandle(udpPort));
        discoverHandle.start();

        //TODO handle incoming messages from users
        while (true) {
            long startTime = System.currentTimeMillis();

            Vector<User> newUsers = new Vector<User>(1);
            for (User user: myModel.getUserList()) {
                BufferedReader fromUser = null;
                try {
                     fromUser = new BufferedReader(new InputStreamReader(user.getConnection().getInputStream()));
                } catch (NullPointerException e) {
                    continue;
                }catch (IOException e) {
                    e.printStackTrace();
                }

                if (fromUser != null) {
                    try {
                        switch (fromUser.read()) {
                            case Model.textCode:
                                myModel.addMessage(fromUser.readLine());
                                myView.update();
                                break;
                            case Model.connectCode:
                                User temp = incomingConnect(fromUser);
                                if (temp != null)
                                    newUsers.add(temp);
                                break;
                            case Model.fileAdCode: break;
                            case Model.fileReqCode: break;
                        }
                    } catch (SocketTimeoutException e) { //Socket will timeout if there is no data to receive
                        continue;
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (User newUser: newUsers) {
                myModel.addUser(newUser);
            }
            long endTime = System.currentTimeMillis();
            long diff = endTime - startTime;
            System.out.println(diff);
            if (diff <= loopPauseTime) {
                try {
                    Thread.sleep(loopPauseTime - diff);
                } catch (InterruptedException e) {
                    //do nothing
                }
            }
        }
    }

    private User incomingConnect(BufferedReader fromUser) throws IOException{
        User newUser = null;
        String host = fromUser.readLine();
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

    public ActionListener getSendListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myModel.addMessage("Send message!!!");
                myView.update();
            }
        };
    }
}
