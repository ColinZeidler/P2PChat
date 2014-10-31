package dcm3203;

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
    private JButton             searchLocalIP;
    private JButton             joinListedIP;
    private JButton             joinEnteredIP;
    private JButton             cancelButton;
    private JList<String>       listIP;
    private JScrollPane         listPane;
    private JTextField          enterIP;

    /////
    //   The handlers
    //
    private ActionListener      cancelAction;       //  The handler for quitting the app
    private ActionListener      listIPAction;       //  The handler for an IP found by discovery
    private ActionListener      enterIPAction;      //  The handler for an IP entered by user
    private ComponentAdapter    resizeAdapter;      //  TODO get this working
    private MouseAdapter        listIPAdapter;      //  The handle for clicking on the list

    /////
    //   The constant values
    //
    static private final boolean        CDIALOG_RESIZABLE = false; // 'true' doesn't work properly

    static private final int            CDIALOG_WIDTH = 600;                //  Window width
    static private final int            CDIALOG_HEIGHT = 400;               //  Window height
    static private final int            CDIALOG_PAD = 5;                    //  Padding between components

    static private final int            CDIALOG_BUTTON_WIDTH = 150;         //  Width of each button
    static private final int            CDIALOG_BUTTON_HEIGHT = 25;         //  Height of each button (and textbox)

    static private final String         NO_PEERS = "No peers found";        //  Empty list const string

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

        listIPAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listIPCheck(listIP.getSelectedValue());
            }
        };

        enterIPAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enterIPCheck(enterIP.getText());
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

        listIP = new JList<String>();
        //listIP.addMouseListener(listListener);
        listPane = new JScrollPane(listIP,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listPane.setLocation(CDIALOG_PAD, CDIALOG_PAD);
        listIP.addMouseListener(listIPAdapter);
        this.add(listPane);

        enterIP = new JTextField("[Enter an IP Address]");
        this.add(enterIP);

        /////
        //   Sets up the buttons
        //

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
        listPane.setSize(CDIALOG_WIDTH - CDIALOG_BUTTON_WIDTH - CDIALOG_PAD*4,
                CDIALOG_HEIGHT - (CDIALOG_PAD*5 + CDIALOG_BUTTON_HEIGHT*3));

        enterIP.setLocation(CDIALOG_PAD,
                CDIALOG_HEIGHT - (CDIALOG_PAD + CDIALOG_BUTTON_HEIGHT)*3);
        enterIP.setSize(listPane.getWidth(),
                CDIALOG_BUTTON_HEIGHT);

        joinListedIP.setLocation(CDIALOG_WIDTH - CDIALOG_BUTTON_WIDTH - CDIALOG_PAD*2,
                CDIALOG_PAD + CDIALOG_BUTTON_HEIGHT);
        joinListedIP.setSize(CDIALOG_BUTTON_WIDTH,
                CDIALOG_BUTTON_HEIGHT);

        joinEnteredIP.setLocation(joinListedIP.getLocation().x,
                CDIALOG_HEIGHT - (CDIALOG_PAD + CDIALOG_BUTTON_HEIGHT)*3);
        joinEnteredIP.setSize(CDIALOG_BUTTON_WIDTH,
                CDIALOG_BUTTON_HEIGHT);

        cancelButton.setLocation(joinListedIP.getLocation().x,
                CDIALOG_HEIGHT - (CDIALOG_PAD + CDIALOG_BUTTON_HEIGHT)*2);
        cancelButton.setSize(CDIALOG_BUTTON_WIDTH,
                CDIALOG_BUTTON_HEIGHT);
    }

    private void initList() {
        String[] listData = new String[0];

        // TODO get list data, empty means no IPs found

        // Test data
/*
        listData = new String[5];
        listData[0] = "128.128.128.128";
        listData[1] = "128.128.1542.128";
        listData[2] = "128.abc.128.128";
        listData[3] = "NULL";
        listData[4] = "128.128";
*/
        if (listData.length == 0) {
            listData = new String[1];
            listData[0] = NO_PEERS;
        }

        listIP.setListData(listData);
    }

    private void cancelButtonPressed() {
        if (getOwner() != null) getOwner().dispose();
        dispose();
    }

    private void listClicked() {
        listIP.setSelectedIndex(listIP.getSelectedIndex());
    }

    private void enterIPCheck(String ip) {
        if(isValidIP(ip)) {
            selectIP(ip);
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
        // TODO send out to connect to IP
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

    /*
// Testing stuff
    static public void main(String[] args){
        ConnectDialog dlog = new ConnectDialog(new View(new Model(), new Controller()),"Connect",true);
        dlog.setVisible(true);
    }
    */
}
