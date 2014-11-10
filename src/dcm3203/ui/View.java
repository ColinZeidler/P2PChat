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
    private JMenuBar menuBar;
    private JMenu menu;
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
        //TODO add padding between all elements

        // add application menu
        menuBar = new JMenuBar();
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("File Menu");
        menuBar.add(menu);
        this.setJMenuBar(menuBar);

        // add chat log
        chatLog = new JTextArea(30, 40);
        chatLog.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatLog);
        c.fill = GridBagConstraints.HORIZONTAL;
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

        //TODO fix shit when resizing
        setVisible(true);

    }

    public String getMessage(){
        return messageBar.getText();
    }
    public void update() {
        chatLog.setText(""); //clear the chat log
        for (String message: myModel.getMessageHistory())
            chatLog.append(message + System.lineSeparator());

        userJList.setListData(myModel.getUserList());
    }
}
