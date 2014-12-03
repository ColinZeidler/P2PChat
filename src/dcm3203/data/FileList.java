package dcm3203.data;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.ListIterator;

/**
 * Created by Michael on 12/11/2014.
 *
 *   The FileList class
 *      - Contains a list of files the local user is advertising
 *      - Contains a map of user keys and vector values
 *          - The user key is the user that offers the file
 *          - The vector values is the list of files that the user advertises
 *
 *   Use:
 *      To add a file:
 *          - If local (on the current computer) use - void addToMyList(FileData)
 *          - If from an advertisement (from other peer) use - void add(User, FileData)
 *
 *      To see if a list contains a file
 *          - If local use - boolean isAdvertisedByMe(FileData)
 *          - If another peer - boolean isAdvertisedByUser(User, FileData)
 *
 *      To get files:
 *          - Local - Entire list - List<FileData> getLocalFiles()
 *          - Users - Entire map - Map<User, List<FileData>> getUserFiles()
 *                  - User's list - List<FileData> getUserFiles(User)
 *
 *      To remove a file:
 *          - If local use - boolean removeFromMyList(FileData)
 *          - If from another peer use - boolean remove(FileData)
 *
 *      To remove all files:
 *          - Local files, use - void clearMyList()
 *          - Of a peer, use - boolean remove(User)
 *
 */
public class FileList {
    private Map<User, List<FileData>>   userFiles;      //  The list of files that the other users are advertising
    private List<FileData>              myFiles;        //  The files that the current user is advertising

    FileList() {
        userFiles = new HashMap<User, List<FileData>>();
        myFiles = new LinkedList<FileData>();
    }

    /////
    //   Adds a file to the list of files that the specified user is advertising
    //
    public synchronized void add(User user, FileData fileData) {
        List<FileData> fdv;

        if (!userFiles.containsKey(user)) {
            fdv = new LinkedList<FileData>();
            userFiles.put(user, fdv);
        } else {
            fdv = userFiles.get(user);
        }

        insertToList(fdv, fileData);
    }

    /////
    //   Adds a file to the list of files the local user is advertising
    //
    public synchronized void addToMyList(FileData fileData) { insertToList(myFiles, fileData); }

    /////
    //   Clears the list of files the local user is advertising
    //
    public synchronized void clearMyList() { myFiles.clear(); }

    /////
    //   Gives a list containing the local files
    //
    public synchronized List<FileData> getLocalFiles() { return (myFiles); }

    /////
    //   Gives a map containing the names of local files
    //
    public synchronized Map<User, List<FileData>> getUserFileMap() { return (userFiles); }

    /////
    //   Returns a list with all the file advertisements of peers
    //
    public synchronized List<FileData> getUserFiles() {
        List<FileData> returnList = new LinkedList<FileData>();

        for (User user : Model.getInstance().getUserList())
            if(userFiles.get(user) != null)
                insertToList(returnList, userFiles.get(user));

        return (returnList);
    }

    /////
    //   Gives a list of all files from a specified user
    //
    public synchronized List<FileData> getUserFiles(User user) { return (userFiles.get(user)); }

    /////
    //   This function inserts a FileData and in lexicographically to prevent having to sorting later
    //
    private synchronized void insertToList(List<FileData> list, FileData fileData) {
        if (!list.isEmpty()) {
            ListIterator<FileData> iterator = list.listIterator();

            while (iterator.hasNext()) {
                if (iterator.next().compareTo(fileData) > 0) {
                    iterator.previous();        //  Needs to go back one since when getting
                    iterator.add(fileData);     // the data the iterator goes forward

                    return;
                }
            }
        }
        list.add(fileData);
    }

    /////
    //   Used to insert entire lists into another list
    //
    private synchronized void insertToList(List<FileData> list, List<FileData> addList) {
        if (!addList.isEmpty())
            for (FileData fileData : addList)
                insertToList(list, fileData);
    }

    /////
    //   Checks to see if the file has been advertised by this instance
    //
    public synchronized boolean isAdvertisedByMe(FileData fileData) {
        for (FileData myFileData : myFiles)
            if (myFileData.equals(fileData))
                return (true);

        return (false);
    }

    /////
    //   Checks to see if the file has been advertised by a peer, if not knowing the user
    //
    public synchronized boolean isAdvertisedByUser(FileData fileData) {
        for (User user : Model.getInstance().getUserList())
            if (isAdvertisedByUser(user, fileData))
                return (true);

        return (false);
    }

    /////
    //   Checks to see if the file has been advertised by a peer, if knowing the user
    //
    public synchronized boolean isAdvertisedByUser(User user, FileData fileData) {
        if(userFiles.get(user) != null)
            for (FileData userFileData : userFiles.get(user))
                if (userFileData.equals(fileData))
                    return (true);

        return (false);
    }

    /////
    //   Removes the file from the list, returns false if file is not within the list
    //
    private synchronized boolean remove(List<FileData> list, FileData fileData) {
        ListIterator<FileData> iterator = list.listIterator();

        while (iterator.hasNext()) {
            if (iterator.next().equals(fileData)) {
                iterator.remove();
                return (true);
            }
        }
        return (false);
    }

    /////
    //   Removes a single file from the list (if add functionality to remove a file that a user does
    //  not want to advertise anymore)
    //
    public synchronized boolean remove(FileData fileData) {
        for (User user : userFiles.keySet()) {
            List<FileData> fdv = userFiles.get(user);

            if (remove(fdv, fileData))
                return (true);
        }
        return (false);
    }

    /////
    //   Removes all the files that are advertised by the user that is removed
    //
    public synchronized boolean remove(User user){
        if (userFiles.containsKey(user)) {
            userFiles.remove(user);
            return (true);
        }
        return (false);
    }

    /////
    //   Removes a file from the list of files the local user is advertising
    //
    public synchronized boolean removeFromMyList(FileData fileData) { return (remove(myFiles, fileData)); }

    /////
    //   Returns a text list of the files advertised, both that the user is advertising and that
    //  peers are advertising
    //
    public synchronized String toString() {
        String strList = "My Files: ";

        for (FileData fileData : myFiles) {
            strList += fileData.getFileName() + ", ";
        }
        strList += "\n";

        for (User user : userFiles.keySet()) {
            strList += user.getName() + ": ";
            for (FileData fileData : userFiles.get(user)) {
                strList += fileData.getFileName() + ", ";
            }
            strList += "\n";
        }

        return (strList);
    }
}