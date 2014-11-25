package dcm3203.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Vector;

import dcm3203.Controller;
import dcm3203.data.Model;
import dcm3203.network.UDPRequester;

/**
 * Created by Michael on 30/10/2014.
 * ConnectDialog class
 *
 * NOTES:
 *  - IP assumed to be in strings
 *  TODO check if can connect
 *  TODO get local IPs to list
 *
 */
public class ConnectDialog extends JDialog{

    /////
    //   Controller
    //     NOTE : Added this to call a function from the controller that uses the dialog instead of doing
    //           any connecting within the dialog class since this class should really only have functions
    //           and such that has to do with the dialog itself
    //
    private Controller          control;     //  Used to send the IP to a function to connect to the address

    /////
    //   UDPRequester and other related
    //      Another thread is run to find IPs in background
    //
    private boolean             runningUDP = false;
    private final String        START_UDP_SEARCH = "Search For Peers";
    private final String        STOP_UDP_SEARCH = "Stop Search";
    private Thread              requesterThread;
    private UDPRequester        requester;

    /////
    //   All the components
    //
    private JButton             cancelButton;           //  For quitting the app
    private JButton             createRoomButton;       //  For creating a room for others to connect to
    private JButton             infoOnNameButton;       //  For getting the info on name format
    private JButton             joinListedIP;           //  For joining an IP from the list of local IPs
    private JButton             joinEnteredIP;          //  For joining an IP entered by the user
    private JButton             searchLocalIP;          //  For getting the list of local IPs
    private JLabel              enterNameLabel;
    private JList<String>       listIP;                 //  The list of local IPs
    private JScrollPane         listPane;
    private JTextField          enterIP;                //  The field for the user to enter an IP
    private JTextField          enterNameField;         //  The field for the user to enter a username

    /////
    //   The handlers
    //
    private ActionListener      cancelAction;       //  The handler for quitting the app
    private ActionListener      createRoomAction;   //  The handler for creating a room for others to connect to
    private ActionListener      enterIPAction;      //  The handler for an IP entered by user
    private ActionListener      infoOnNameAction;   //  The handler for getting info on the format of the name
    private ActionListener      listIPAction;       //  The handler for an IP found by discovery
    private ActionListener      searchAction;       //  The handler to search for peers
    private ComponentAdapter    resizeAdapter;      //  TODO get this working, probably won't happen since not needed
    private MouseAdapter        listIPAdapter;      //  The handle for clicking on the list

    /////
    //   The constant values
    //
    ///////
      //   To do with the sizing of the dialog
      //
    static private final boolean        CDIALOG_RESIZABLE = false;                  // 'true' doesn't work properly

    static private final Dimension      CDIALOG     = new Dimension(600, 500);      //  Window dimensions
    static private final int            CDIALOG_PAD = 5;                            //  Padding between components

    static private final Dimension      CDIALOG_BUTTON = new Dimension(150, 25);    //  The dimensions of a button
    static private final int            CDIALOG_BUTTON_PAD = CDIALOG_PAD + (int)CDIALOG_BUTTON.getHeight();

    static private final int            CDIALOG_NAME_LABEL_WIDTH = 100;             //  The width of the name label

      /////
      //   For the list of IPs
      //
    static private final String         NO_PEERS_FOUND = "No peers found";          //  Empty list const string

      /////
      //   For the check of a valid name
      //
    static private final String         IS_NAME_VALID_REGEX      = "[a-zA-Z]([a-zA-Z0-9[ |_|'|-]]*([a-zA-Z0-9]))?";    //  The check for valid name
    static private final String         IS_NAME_CHAR_VALID_REGEX = "[a-zA-Z0-9 _'-]";
    static private final int            NAME_MAX_LEN             = 64;

    public ConnectDialog(Frame owner, String title, boolean modal, Controller control) {
        super(owner, title, modal);

        this.control = control;

        initHandlers();
        initVisual();
        updatePositions();
        initList();
    }

    private void cancelButtonPressed() {
        if (getOwner() != null)
            getOwner().dispatchEvent(new WindowEvent(getOwner(), WindowEvent.WINDOW_CLOSING)); //properly kills the program
        if (requester != null)
            killSearch();
        dispose();
    }

    private void enterIPCheck(String ip) {
        if(isValidIP(ip)) {
            if(nameCheck(enterNameField.getText())) {
                setMyName(enterNameField.getText());
                selectIP(ip);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid IP entered!", "Warning!", JOptionPane.WARNING_MESSAGE);
        }
    }

    static public String getInvalidCharacters(String name) {
        String rString = "";

        for (int i = 0; i < name.length(); i++) {
            String invalidChar = String.valueOf(name.charAt(i));

            if (!invalidChar.matches(IS_NAME_CHAR_VALID_REGEX))
                if(!rString.contains(invalidChar))
                    rString += invalidChar + ", ";
        }

        if (rString.length() > 0)                                   //  Ensures no errors will happen then
            rString = rString.substring(0, rString.length() - 2);   //  Removes the last comma since no character after it

        return (rString);
    }

    /////
    //   This function deals with setting up the handlers of the dialog
    //
    private void initHandlers() {

        cancelAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButtonPressed();
            }
        };

        createRoomAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(nameCheck(enterNameField.getText())) {
                    setMyName(enterNameField.getText());
                    if (requester != null)
                        killSearch();
                    dispose();
                }
            }
        };

        enterIPAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enterIPCheck(enterIP.getText());
            }
        };

        infoOnNameAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog((Component)e.getSource(),
                        "Name format must be as follows: \n" +
                                " - Can contain letters, numbers, spaces, underscores, hyphens, apostrophes \n" +
                                " - Must start with a letter \n" +
                                " - Must end in a letter or number \n" +
                                " - Maximum length of " + NAME_MAX_LEN + " characters",
                        "Name Format", JOptionPane.INFORMATION_MESSAGE);
            }
        };

        listIPAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listIPCheck(listIP.getSelectedValue());
            }
        };

        searchAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!runningUDP) {
                    startSearch();
                    searchLocalIP.setText(STOP_UDP_SEARCH);
                } else {
                    killSearch();
                    searchLocalIP.setText(START_UDP_SEARCH);
                }
                runningUDP = !runningUDP;
            }
        };

        resizeAdapter = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updatePositions();
            }
        };

        listIPAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                listClicked();
            }
        };

    }

    private void initList() {
        updateList(new Vector<String>());
    }

    /////
    //   This function deals with setting up the visuals of the dialog
    //
    private void initVisual() {
        setResizable(CDIALOG_RESIZABLE);

        if (isResizable()) {
            setSize((int)CDIALOG.getWidth() + CDIALOG_PAD * 2, (int)CDIALOG.getHeight() + CDIALOG_PAD * 2);
        } else {
            setSize((int)CDIALOG.getWidth(), (int)CDIALOG.getHeight());
        }

        addComponentListener(resizeAdapter);

        setLocation((int)(getToolkit().getScreenSize().getWidth()/2 - getWidth()/2),
                (int)(getToolkit().getScreenSize().getHeight()/2 - getHeight()/2));
        setLayout(null);

        enterNameLabel = new JLabel("Enter User Name:");
        this.add(enterNameLabel);

        enterNameField = new JTextField();
        this.add(enterNameField);

        listIP = new JList<String>();
        listPane = new JScrollPane(listIP,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listIP.addMouseListener(listIPAdapter);
        this.add(listPane);

        enterIP = new JTextField("[Enter an IP Address]");
        this.add(enterIP);

        /////
        //   Sets up the buttons
        //
        cancelButton = new JButton("Exit");
        cancelButton.addActionListener(cancelAction);
        add(cancelButton);

        createRoomButton = new JButton("Create Room");
        createRoomButton.addActionListener(createRoomAction);
        add(createRoomButton);

        infoOnNameButton = new JButton("Info On Name...");
        infoOnNameButton.addActionListener(infoOnNameAction);
        add(infoOnNameButton);

        joinEnteredIP = new JButton("Join Entered IP");
        joinEnteredIP.addActionListener(enterIPAction);
        add(joinEnteredIP);

        joinListedIP = new JButton("Join Listed IP");
        joinListedIP.addActionListener(listIPAction);
        add(joinListedIP);

        searchLocalIP = new JButton(START_UDP_SEARCH);
        searchLocalIP.addActionListener(searchAction);
        add(searchLocalIP);
    }

    /////
    //   This function checks if the IP is valid (format)
    //      * Note not heavily tested, based on something I read
    //
    static public boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) return (false);

        String[] bytes = ip.split("\\.");
        if (bytes.length != 4) return (false);

        try {
            for (int i = 0; i < 4; i++) {
                int temp = Integer.parseInt(bytes[i]);
                if (temp < 0 || temp > 255) return (false);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid IP : " + e.getMessage()); // just if issues pop up
            return (false);
        }

        return(true);
    }

    static public boolean isValidName(String name) { return (name.length() > 0
            && name.matches(IS_NAME_VALID_REGEX)
            && name.length() <= NAME_MAX_LEN); }

    private void killSearch() {
        if(requesterThread != null) {
            requester.terminate();
            try {
                requesterThread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void listClicked() {
        listIP.setSelectedIndex(listIP.getSelectedIndex());
    }

    private void listIPCheck(String ip) {
        if(ip == null) {
            JOptionPane.showMessageDialog(this, "No IP selected from list!", "Warning!", JOptionPane.WARNING_MESSAGE);
        } else if (ip.equals(NO_PEERS_FOUND)) {
            JOptionPane.showMessageDialog(this, "No IPs in the list!", "Warning!", JOptionPane.WARNING_MESSAGE);
        } else {
            enterIPCheck(ip);
        }
    }

    private boolean nameCheck(String name) {
        if(!isValidName(name)) {
            String errMes;

            if (name.length() <= 0) {
                errMes = "No name entered!";
            } else if (name.length() > NAME_MAX_LEN) {
                errMes = "Name too long! Maximum of " + NAME_MAX_LEN + " characters!";
            } else {
                String invalidChars = getInvalidCharacters(name);
                errMes = "Name contains invalid character: " + invalidChars + "!";
            }

            JOptionPane.showMessageDialog(this, errMes, "Warning!", JOptionPane.WARNING_MESSAGE);

            return (false);
        }
        return (true);
    }

    /////
    //   Sends the ip to the controller to connect
    //      - is done after the ip has been verified as a valid format
    private void selectIP(String ip) {
        try {
            if(!control.setupConnection(ip)) {
                JOptionPane.showMessageDialog(this, "Error: Connection was not successful!", "Something went wrong!", JOptionPane.ERROR_MESSAGE);
            } else {
                if (requester != null)
                    killSearch();
                this.dispose();
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Connection was not successful!", "Something went wrong!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /////
    //   Sets the name of the user, sets the variable in the model
    //
    private void setMyName(String name) { Model.getInstance().setMyName(name); }

    private void startSearch() {
        requester = new UDPRequester(control.getUDPPort(), this);
        requesterThread = new Thread(requester);
        requesterThread.start();
    }

    public void updateList(Vector<String> listData) {
        if (listData.isEmpty())
            listData.add(NO_PEERS_FOUND);

        listIP.setListData(listData);
    }

    /////
    //   This function sets up the positions and width/heights of the components
    //
    private void updatePositions() {

        enterNameLabel.setLocation(CDIALOG_PAD, CDIALOG_PAD);
        enterNameLabel.setSize(CDIALOG_NAME_LABEL_WIDTH, (int)CDIALOG_BUTTON.getHeight());

        enterNameField.setLocation(enterNameLabel.getLocation().x + CDIALOG_NAME_LABEL_WIDTH + CDIALOG_PAD,
                enterNameLabel.getLocation().y);
        enterNameField.setSize(getWidth() - enterNameField.getLocation().x - CDIALOG_PAD*3 - (int)CDIALOG_BUTTON.getWidth(),
                (int)CDIALOG_BUTTON.getHeight());

        listPane.setLocation(CDIALOG_PAD, CDIALOG_PAD*3 + (int)CDIALOG_BUTTON.getHeight()*2);
        listPane.setSize((int)CDIALOG.getWidth() - (int)CDIALOG_BUTTON.getWidth() - CDIALOG_PAD*4,
                (int)CDIALOG.getHeight() - (CDIALOG_PAD*7 + (int)CDIALOG_BUTTON.getHeight()*5));

        enterIP.setLocation(CDIALOG_PAD,
                (int)CDIALOG.getHeight() - (CDIALOG_BUTTON_PAD)*3);
        enterIP.setSize(listPane.getWidth(),
                (int)CDIALOG_BUTTON.getHeight());

        /////
        //   Sets up the locations and sizes of each button
        //
        infoOnNameButton.setLocation((int)CDIALOG.getWidth() - (int)CDIALOG_BUTTON.getWidth() - CDIALOG_PAD*2,
                CDIALOG_PAD);
        infoOnNameButton.setSize(CDIALOG_BUTTON);

        createRoomButton.setLocation(infoOnNameButton.getLocation().x,
                CDIALOG_BUTTON_PAD + CDIALOG_PAD);
        createRoomButton.setSize(CDIALOG_BUTTON);

        searchLocalIP.setLocation(infoOnNameButton.getLocation().x,
                (CDIALOG_BUTTON_PAD)*3);
        searchLocalIP.setSize(CDIALOG_BUTTON);

        joinListedIP.setLocation(infoOnNameButton.getLocation().x,
                (CDIALOG_BUTTON_PAD)*4);
        joinListedIP.setSize((int)CDIALOG_BUTTON.getWidth(),
                (int)CDIALOG_BUTTON.getHeight());

        joinEnteredIP.setLocation(infoOnNameButton.getLocation().x,
                (int)CDIALOG.getHeight() - (CDIALOG_BUTTON_PAD)*3);
        joinEnteredIP.setSize(CDIALOG_BUTTON);

        cancelButton.setLocation(infoOnNameButton.getLocation().x,
                (int)CDIALOG.getHeight() - (CDIALOG_BUTTON_PAD)*2);
        cancelButton.setSize(CDIALOG_BUTTON);
    }
}
