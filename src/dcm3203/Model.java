package dcm3203;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Created by Colin on 2014-10-28.
 */
public class Model {
    private ArrayDeque<String> messageHistory;
    private int maxMessages = 750;
    private ArrayList<User> userList;

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

    public ArrayList<User> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<User> userList) {
        this.userList = userList;
    }

    public void addUser(User user) {
        this.userList.add(user);
    }

    public void removeUser(User user) {
        this.userList.remove(user);
    }
}
