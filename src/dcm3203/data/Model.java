package dcm3203.data;

import java.util.ArrayDeque;
import java.util.Vector;

/**
 * Created by Colin on 2014-10-28.
 */
public class Model {
    private ArrayDeque<String> messageHistory;
    private int maxMessages = 750;
    private Vector<User> userList;

    public Model() {
        messageHistory = new ArrayDeque<String>();
        userList = new Vector<User>();
    }

    public ArrayDeque<String> getMessageHistory() {
        return messageHistory;
    }

    public void setMessageHistory(ArrayDeque<String> messageHistory) {
        this.messageHistory = messageHistory;
    }

    /**
     * add new message to the list, and remove the oldest if message count is greater than the max
     * @param message the message to add to the list
     */
    public void addMessage(String message) {
        messageHistory.add(message);
        if (messageHistory.size() > maxMessages)
            messageHistory.remove();
    }

    public Vector<User> getUserList() {
        return userList;
    }

    public void setUserList(Vector<User> userList) {
        this.userList = userList;
    }

    public void addUser(User user) {
        this.userList.add(user);
    }

    public void removeUser(User user) {
        this.userList.remove(user);
    }
}
