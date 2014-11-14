package dcm3203.data;

import java.util.Vector;
import java.util.HashMap;

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
    private HashMap<User, Vector<FileData>> fileList;   //  The list of files that the other users are advertising
    private Vector<FileData> myFiles;                   //  The files that the current user is advertising

    FileList() {
        fileList = new HashMap<User, Vector<FileData>>();
        myFiles = new Vector<FileData>();
    }

    /////
    //   Adds a file to the list of files that the specified user is advertising
    //
    public synchronized void add(User user, FileData fileData) {
        Vector<FileData> fdv;
        if (!fileList.containsKey(user)) {
            fdv = new Vector<FileData>();
            fdv.add(fileData);
            fileList.put(user, fdv);
        } else {
            fdv = fileList.get(user);
            fdv.add(fileData);
            fileList.put(user, fdv);
        }
    }

    /////
    //   Adds a file to the list of files the local user is advertising
    //
    public synchronized void addToMyList(FileData fileData) {
        myFiles.add(fileData);
    }

    /////
    //   Clears the list of files the local user is advertising
    //
    public synchronized void clearMyList() {
        myFiles.clear();
    }

    /////
    //   Removes a single file from the list (if add functionality to remove a file that a user does
    //  not want to advertise anymore)
    //
    public synchronized boolean remove(FileData fileData) {
        for (User user : fileList.keySet()) {
            Vector<FileData> fdv = fileList.get(user);

            if (fdv.contains(fileData)) {
                fdv.remove(fileData);
                if (fdv.isEmpty()) {
                    fileList.remove(user);
                } else {
                    fileList.put(user, fdv);
                }
                return (true);
            }
        }
        return (false);
    }

    /////
    //   Removes all the files that are advertised by the user that is removed
    //
    public synchronized boolean remove(User user){
        if (fileList.containsKey(user)) {
            fileList.remove(user);
            return (true);
        }
        return (false);
    }

    /////
    //   Removes a file from the list of files the local user is advertising
    //
    public synchronized boolean removeFromMyList(FileData fileData) {
        if (myFiles.contains(fileData)) {
            myFiles.remove(fileData);
            return (true);
        }
        return (false);
    }

    /////
    //   Gives an vector containing the names of local files
    //
    public synchronized Vector<FileData> localFiles() { return (myFiles); }

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

        for (User user : fileList.keySet()) {
            strList += user.getName() + ": ";
            for (FileData fileData : fileList.get(user)) {
                strList += fileData.getFileName() + ", ";
            }
            strList += "\n";
        }

        return (strList);
    }
}
