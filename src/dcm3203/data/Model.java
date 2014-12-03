package dcm3203.data;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Vector;

/**
 * Created by Colin on 2014-10-28.
 */
public class Model {
    private final ArrayDeque<String>    messageHistory;
    private int                         maxMessages = 750;
    private final Vector<User>          userList;
    private static Model                instance;
    private String                      myName;
    private FileList                    filesAvailable;

    public static final int textCode = 0;
    public static final int connectCode = 3;
    public static final int disconnectCode = 5;
    public static final int fileAdCode = 1;
    public static final int fileReqCode = 2;
    public static final int fileRemoveCode = 4;
    public static final int heartbeatCode = 6;

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

    public void clearMessages() {
        synchronized (messageHistory) {
            messageHistory.clear();
        }
    }

    public Vector<User> getUserList() {
        synchronized (userList) {
            return userList;
        }
    }

    public void addUser(User user) {
        addMessage(user.getName() + " has connected.");
        synchronized (userList) {
            this.userList.add(user);
        }
    }

    public void removeUser(User user) {
        synchronized (userList) {
            removeUserFiles(user);
            try {
                user.getConnection().close();
            } catch (IOException e) {
//                e.printStackTrace();
                System.out.println("unable to gracefully close socket");
            }
            this.userList.remove(user);
        }
    }

    public void removeAllUsers() {
        User user;
        synchronized (userList) {
            while (userList.size() > 0) {
                user = userList.get(userList.size() - 1);
                removeUser(user);
            }
        }
    }

    /////
    //   Returns the FileList used
    //
    public FileList getFilesAvailable() { return (filesAvailable); }

    /////
    //   Adds FileData to the appropriate list from a FileDataString
    //
    public boolean addFile(String fileDataString) {
        return (addFile(new FileData(fileDataString)));
    }

    /////
    //   Adds FileData to the appropriate list
    //
    public boolean addFile(FileData fileData) {
        if (fileData.isHave()) {
            if (!this.filesAvailable.isAdvertisedByMe(fileData)) {
                this.filesAvailable.addToMyList(fileData);
                return (true);
            }
        } else {
            synchronized (userList) {
                for (User user : userList) {
                    if (user.getName().equals(fileData.getFileLocation())) {
                        if (!this.filesAvailable.isAdvertisedByUser(user, fileData)) {
                            this.filesAvailable.add(user, fileData);
                            return (true);
                        }
                    }
                }
            }
        }
        return (false);
    }

    /////
    //   Removes the FileData created from the FileData String
    //
    public boolean removeFile(String fileDataString) {
        return (removeFile(new FileData(fileDataString)));
    }

    /////
    //   Removes the file given from the appropriate list
    //
    public boolean removeFile(FileData fileData) {
        if (fileData.isHave()) {
            return(this.filesAvailable.removeFromMyList(fileData));
        } else {
            return(this.filesAvailable.remove(fileData));
        }
    }

    /////
    //   Removes all the file advertisements associated with the given user
    //
    public boolean removeUserFiles(User user) {
        return(this.filesAvailable.remove(user));
    }

    /////
    //   Prints the file names stored in the FileList
    //
    public String printFiles() {
        return (filesAvailable.toString());
    }

    /////
    //   Returns the name of the user
    //
    public String getMyName() {
        return myName;
    }

    /////
    //   Sets the name of the user
    //
    public void setMyName(String myName) {
        this.myName = myName;
    }
}
