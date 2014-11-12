package dcm3203.data;

import java.util.Vector;
import java.util.HashMap;

/**
 * Created by Michael on 12/11/2014.
 *
 *
 */
public class FileList {
    private HashMap<User, Vector<FileData>> fileList;
    private Vector<FileData> myFiles;

    FileList() {
        fileList = new HashMap<User, Vector<FileData>>();
        myFiles = new Vector<FileData>();
    }

    public synchronized void addToMyList(FileData fileData) {
        myFiles.add(fileData);
    }

    public synchronized void clearMyList() {
        myFiles.clear();
    }

    public synchronized boolean removeFromMyList(FileData fileData) {
        if (myFiles.contains(fileData)) {
            myFiles.remove(fileData);
            return (true);
        }
        return (false);
    }

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

    public synchronized boolean remove(User user){
        if (fileList.containsKey(user)) {
            fileList.remove(user);
            return (true);
        }
        return (false);
    }
}
