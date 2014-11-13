package dcm3203.data;

import java.util.ArrayDeque;
import java.util.Vector;

/**
 * Created by Colin on 2014-10-28.
 */
public class Model {
    private final ArrayDeque<String> messageHistory;
    private int maxMessages = 750;
    private final Vector<User> userList;
    private static Model instance;
    private String myName;
    private FileList filesAvailable;

    public static final int textCode = 0;
    public static final int connectCode = 3;
    public static final int fileAdCode = 1;
    public static final int fileReqCode = 2;

    public Model() {
        messageHistory = new ArrayDeque<String>();
        userList = new Vector<User>();
        filesAvailable = new FileList();
    }

    public static Model getInstance() {
        if (instance == null)
            instance = new Model();
        return instance;
    }

    public ArrayDeque<String> getMessageHistory() {
        synchronized (messageHistory) {
            return messageHistory;
        }
    }

    /**
     * add new message to the list, and remove the oldest if message count is greater than the max
     * @param message the message to add to the list
     */
    public void addMessage(String message) {
        synchronized (messageHistory) {
            messageHistory.add(message);
            if (messageHistory.size() > maxMessages)
                messageHistory.remove();
        }
    }

    public Vector<User> getUserList() {
        synchronized (userList) {
            return userList;
        }
    }

    public void addUser(User user) {
        synchronized (userList) {
            this.userList.add(user);
        }
    }

    public void removeUser(User user) {
        synchronized (userList) {
            removeUserFiles(user);
            this.userList.remove(user);
        }
    }

    public boolean addFile(String fileDataString) {
        return (addFile(new FileData(fileDataString)));
    }

    public boolean addFile(FileData fileData) {
        if (fileData.isHave()) {
            this.filesAvailable.addToMyList(fileData);
        } else {
            synchronized (userList) {
                for (User user : userList) {
                    if (user.getName().equals(fileData.getFileLocation())) {
                        this.filesAvailable.add(user, fileData);
                        return (true);
                    }
                }
            }
        }
        return (false);
    }

    public boolean removeFile(String fileDataString) {
        return (removeFile(new FileData(fileDataString)));
    }

    public boolean removeFile(FileData fileData) {
        if (fileData.isHave()) {
            return(this.filesAvailable.removeFromMyList(fileData));
        } else {
            return(this.filesAvailable.remove(fileData));
        }
    }

    public boolean removeUserFiles(User user) {
        return(this.filesAvailable.remove(user));
    }

    public String printFiles() {
        return (filesAvailable.toString());
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }
}
