package dcm3203.ui;

import dcm3203.Controller;
import dcm3203.data.Model;
import dcm3203.data.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Colin on 2014-10-28.
 */
public class View extends JFrame{
    private Model myModel;
    private Controller myController;

    private JTextArea chatLog;
    private JList<User> userJList;
    private JTextField messageBar;

    private final int   VIEW_MIN_WIDTH = 750;
    private final int   VIEW_MIN_HEIGHT = 600;

    public View(Controller controller) {
        myModel = Model.getInstance();
        myController = controller;
        initDisplay();
    }

    private void initDisplay() {
        setTitle("Application name");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); //exit the program when the window is closed.
        setSize(VIEW_MIN_WIDTH, VIEW_MIN_HEIGHT);
        setLocation(100, 100);

        setMinimumSize(new Dimension(VIEW_MIN_WIDTH, VIEW_MIN_HEIGHT)); //  Helps with the sizing issue (maybe not the solution)

        //rows, columns, vertical gap, horizontal gap
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // add application menu
        JMenuBar menuBar;
        JMenu menu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("File Menu");
        menuBar.add(menu);

        menuItem = new JMenuItem("Connect to New Room",
                KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, InputEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Brings the connection dialog back up to connect to a new room");
        menuItem.addActionListener(myController.getNewConnectListener());
        menu.add(menuItem);

        menuItem = new JMenuItem("Disconnect from Room",
                KeyEvent.VK_D);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_D, InputEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Disconnects and brings the connection dialog back up to connect to a new room");
        menuItem.addActionListener(myController.getDisconnectListener());
        menu.add(menuItem);

        menu.addSeparator();
        menuItem = new JMenuItem("Advertise File",
                KeyEvent.VK_A);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, InputEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Advertises file to other users to download");
        menuItem.addActionListener(myController.getFileListener());
        menu.add(menuItem);

        menuItem = new JMenuItem("Remove Advertisement",
                KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, InputEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Remove advertisement on one of your files");
        menuItem.addActionListener(myController.getRemoveFileListener());
        menu.add(menuItem);

        menu.addSeparator();
        menuItem = new JMenuItem("Exit",
                KeyEvent.VK_E);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_F4, InputEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "Quit the application");
        menuItem.addActionListener(myController.getExitListener());
        menu.add(menuItem);

        // add chat log
        chatLog = new JTextArea(30, 40);
        chatLog.setEditable(false);
        chatLog.setLineWrap(true);
        chatLog.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatLog);
        c.fill = GridBagConstraints.HORIZONTAL;
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.9;
        this.add(chatScrollPane, c);


        // add user list
        userJList = new JList<User>();
        JScrollPane userScrollPane = new JScrollPane(userJList);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0;
        this.add(userScrollPane, c);


        // add message bar
        messageBar = new JTextField();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        this.add(messageBar, c);


        // add send button
        JButton sendButton = new JButton("Send");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        sendButton.addActionListener(myController.getSendListener()); //Don't know if this is how it should be done
        this.add(sendButton, c);

        this.getRootPane().setDefaultButton(sendButton);
        //TODO fix shit when resizing
        setVisible(true);

    }

    public String getMessage(){
        String message = messageBar.getText();
        messageBar.setText("");
        return message;
    }
    public void update() {
        chatLog.setText(""); //clear the chat log
        for (String message: myModel.getMessageHistory())
            chatLog.append(message + System.lineSeparator());
        chatLog.setCaretPosition(chatLog.getDocument().getLength());

        userJList.setListData(myModel.getUserList());
    }
}
