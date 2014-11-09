package dcm3203.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Michael on 30/10/2014.
 * ConnectDialog class
 *
 * NOTES:
 *  - IP assumed to be in strings
 *  TODO call correct function to send specified IP to main part of program
 *  TODO make sure exit button works with main program
 *  TODO get local IPs to list
 *
 */
public class ConnectDialog extends JDialog{

    /////
    //   All the components
    //
    private JButton             cancelButton;
    private JButton             infoOnNameButton;
    private JButton             joinListedIP;
    private JButton             joinEnteredIP;
    private JButton             searchLocalIP;
    private JLabel              enterNameLabel;
    private JList<String>       listIP;
    private JScrollPane         listPane;
    private JTextField          enterIP;
    private JTextField          enterNameField;

    /////
    //   The handlers
    //
    private ActionListener      cancelAction;       //  The handler for quitting the app
    private ActionListener      enterIPAction;      //  The handler for an IP entered by user
    private ActionListener      infoOnNameAction;   //  The handler for getting info on the format of the name
    private ActionListener      listIPAction;       //  The handler for an IP found by discovery
    private ActionListener      searchAction;       //  The handler to search for peers
    private ComponentAdapter    resizeAdapter;      //  TODO get this working
    private MouseAdapter        listIPAdapter;      //  The handle for clicking on the list

    /////
    //   The constant values
    //
    ///////
      //   To do with the sizing of the dialog
      //
    static private final boolean        CDIALOG_RESIZABLE = false; // 'true' doesn't work properly

    static private final int            CDIALOG_WIDTH  = 600;                   //  Window width
    static private final int            CDIALOG_HEIGHT = 400;                   //  Window height
    static private final int            CDIALOG_PAD    = 5;                     //  Padding between components

    static private final int            CDIALOG_BUTTON_WIDTH  = 150;            //  Width of each button
    static private final int            CDIALOG_BUTTON_HEIGHT = 25;             //  Height of each button (and textbox)

    static private final int            CDIALOG_NAME_LABEL_WIDTH = 100;         //  The width of the name label

      /////
      //   For the list of IPs
      //
    static private final String         NO_PEERS = "No peers found";            //  Empty list const string

      /////
      //   For the check of a valid name
      //
    static private final String         IS_NAME_VALID_REGEX      = "[a-zA-Z]([a-zA-Z0-9[ |_|'|-]]*([a-zA-Z0-9]))?";    //  The check for valid name
    static private final String         IS_NAME_CHAR_VALID_REGEX = "[a-zA-Z0-9 _'-]";
    static private final int            NAME_MAX_LEN             = 64;

    public ConnectDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);

        initHandlers();
        initVisual();
        updatePositions();
        initList();
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
                // TODO call to get list, do search, whatever
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

    /////
    //   This function deals with setting up the visuals of the dialog
    //
    private void initVisual() {
        setResizable(CDIALOG_RESIZABLE);

        if (isResizable()) {
            setSize(CDIALOG_WIDTH + CDIALOG_PAD * 2, CDIALOG_HEIGHT + CDIALOG_PAD * 2);
        }  else {
            setSize(CDIALOG_WIDTH, CDIALOG_HEIGHT);
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
        //listIP.addMouseListener(listListener);
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
        infoOnNameButton = new JButton("Info On Name...");
        infoOnNameButton.addActionListener(infoOnNameAction);
        add(infoOnNameButton);

        searchLocalIP = new JButton("Search For Peers");
        searchLocalIP.addActionListener(searchAction);
        add(searchLocalIP);

        joinListedIP = new JButton("Join Listed IP");
        joinListedIP.addActionListener(listIPAction);
        add(joinListedIP);

        joinEnteredIP = new JButton("Join Entered IP");
        joinEnteredIP.addActionListener(enterIPAction);
        add(joinEnteredIP);

        cancelButton = new JButton("Exit");
        cancelButton.addActionListener(cancelAction);
        add(cancelButton);
    }

    /////
    //   This function sets up the positions and width/heights of the components
    //
    private void updatePositions() {

        enterNameLabel.setLocation(CDIALOG_PAD, CDIALOG_PAD);
        enterNameLabel.setSize(CDIALOG_NAME_LABEL_WIDTH, CDIALOG_BUTTON_HEIGHT);

        enterNameField.setLocation(enterNameLabel.getLocation().x + CDIALOG_NAME_LABEL_WIDTH + CDIALOG_PAD,
                enterNameLabel.getLocation().y);
        enterNameField.setSize(getWidth() - enterNameField.getLocation().x - CDIALOG_PAD*3 - CDIALOG_BUTTON_WIDTH,
                CDIALOG_BUTTON_HEIGHT);

        listPane.setLocation(CDIALOG_PAD, CDIALOG_PAD*2 + CDIALOG_BUTTON_HEIGHT);
        listPane.setSize(CDIALOG_WIDTH - CDIALOG_BUTTON_WIDTH - CDIALOG_PAD*4,
                CDIALOG_HEIGHT - (CDIALOG_PAD*6 + CDIALOG_BUTTON_HEIGHT*4));

        enterIP.setLocation(CDIALOG_PAD,
        CDIALOG_HEIGHT - (CDIALOG_PAD + CDIALOG_BUTTON_HEIGHT)*3);
        enterIP.setSize(listPane.getWidth(),
                CDIALOG_BUTTON_HEIGHT);

        joinListedIP.setLocation(CDIALOG_WIDTH - CDIALOG_BUTTON_WIDTH - CDIALOG_PAD*2,
                (CDIALOG_PAD + CDIALOG_BUTTON_HEIGHT)*3);
        joinListedIP.setSize(CDIALOG_BUTTON_WIDTH,
                CDIALOG_BUTTON_HEIGHT);

        infoOnNameButton.setLocation(joinListedIP.getLocation().x,
                CDIALOG_PAD);
        infoOnNameButton.setSize(joinListedIP.getSize());

        searchLocalIP.setLocation(joinListedIP.getLocation().x,
                (CDIALOG_PAD + CDIALOG_BUTTON_HEIGHT)*2);
        searchLocalIP.setSize(joinListedIP.getSize());

        joinEnteredIP.setLocation(joinListedIP.getLocation().x,
                CDIALOG_HEIGHT - (CDIALOG_PAD + CDIALOG_BUTTON_HEIGHT)*3);
        joinEnteredIP.setSize(joinListedIP.getSize());

        cancelButton.setLocation(joinListedIP.getLocation().x,
                CDIALOG_HEIGHT - (CDIALOG_PAD + CDIALOG_BUTTON_HEIGHT)*2);
        cancelButton.setSize(joinListedIP.getSize());
    }

    private void initList() {
        String[] listData = new String[0];

        // TODO get list data, empty means no IPs found

        if (listData.length == 0) {
            listData = new String[1];
            listData[0] = NO_PEERS;
        }

        listIP.setListData(listData);
    }

    private void cancelButtonPressed() {
        if (getOwner() != null)
            getOwner().dispatchEvent(new WindowEvent(getOwner(), WindowEvent.WINDOW_CLOSING)); //properly kills the program
        dispose();
    }

    private void listClicked() {
        listIP.setSelectedIndex(listIP.getSelectedIndex());
    }

    private void enterIPCheck(String ip) {
        if(isValidIP(ip)) {
            String name = enterNameField.getText();
            if(isValidName(name)) {
                selectIP(ip);
            } else {
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
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid IP entered!", "Warning!", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void listIPCheck(String ip) {
        if(ip == null) {
            JOptionPane.showMessageDialog(this, "No IP selected from list!", "Warning!", JOptionPane.WARNING_MESSAGE);
        } else if (ip.equals(NO_PEERS)) {
            JOptionPane.showMessageDialog(this, "No IPs in the list!", "Warning!", JOptionPane.WARNING_MESSAGE);
        } else {
            enterIPCheck(ip);
        }
    }

    private void selectIP(String ip) {
        // TODO send out to connect to IP, store name
        //if (getOwner() != null) ((View)getOwner()).ipFunction(ip); // not actual function name
        System.out.println(ip);
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
}
