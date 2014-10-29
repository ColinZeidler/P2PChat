package dcm3203;

import java.util.ArrayDeque;

/**
 * Created by Colin on 2014-10-28.
 */
public class Model {
    private ArrayDeque<String> messageHistory;

    public ArrayDeque<String> getMessageHistory() {
        return messageHistory;
    }

    public void setMessageHistory(ArrayDeque<String> messageHistory) {
        this.messageHistory = messageHistory;
    }

    public void addMessage(String message) {
        messageHistory.add(message);
    }

}
