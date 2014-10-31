package dcm3203;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Michael on 30/10/2014.
 * ConnectDialog class
 *
 * NOTES:
 *  - IP assumed to be in strings
 *
 * TODO just a shell right now, need to add
 */
public class ConnectDialog extends JDialog{

    private JList<String>       listIP;
    private JTextField          enterIP;

    private ActionListener      cancelAction;       //  The handler for quitting the app
    private ActionListener      listIPAction;       //  The handler for an IP found by discovery
    private ActionListener      enterIPAction;      //  The handler for an IP entered by user

    static final String         NO_PEERS = "No peers found";        //  Empty list const string

    public ConnectDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);

        initHandlers();
        initVisual();
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

    }

    /////
    //   This function deals with setting up the visuals of the dialog
    //
    private void initVisual() {
        setSize(500, 390);
        setResizable(false);
        setLayout(null);

        listIP = new JList();
        //listIP.addMouseListener(listListener);
        JScrollPane scrollPane = new JScrollPane(listIP,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setLocation(5,5);
        scrollPane.setSize(330,280);
        this.add(scrollPane);

        enterIP = new JTextField("[Enter an IP Address]");
        enterIP.setLocation(5,300);
        enterIP.setSize(330,25);
        this.add(enterIP);

        /////
        //   Sets up the buttons
        //

        JButton joinListedIP = new JButton("Join Listed IP");
        joinListedIP.setLocation(340,30);
        joinListedIP.setSize(150, 25);
        joinListedIP.addActionListener(listIPAction);
        add(joinListedIP);

        JButton joinEnteredIP = new JButton("Join Entered IP");
        joinEnteredIP.setLocation(340,300);
        joinEnteredIP.setSize(150,25);
        joinEnteredIP.addActionListener(enterIPAction);
        add(joinEnteredIP);

        JButton cancelButton = new JButton("Exit");
        cancelButton.setLocation(340,330);
        cancelButton.setSize(150, 25);
        cancelButton.addActionListener(cancelAction);
        add(cancelButton);
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
        if (getOwner() != null) ((View) getOwner()).dispose();
        dispose();
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
            selectIP(ip);
        }
    }

    private void selectIP(String ip) {
        //if (getOwner() != null) ((View)getOwner()).ipFunction(ip);
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
                System.out.println(i);
            }
        } catch (Exception e) {
            return (false);     // TODO Not a good way to handle this, need to fix
        }

        return(true);
    }

/* // Testing stuff
    static public void main(String[] args){
        ConnectDialog dlog = new ConnectDialog(new View(new Model(), new Controller()),"Connect",true);
        dlog.setVisible(true);
    }
*/
}
