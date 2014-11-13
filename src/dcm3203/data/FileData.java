package dcm3203.data;

import java.io.File;

/**
 * Created by Michael on 09/11/2014.
 *
 * FileData class to store info on the advertised file
 *
 *      Can be used to store the location of the file, on disk if the user that has it, name of user that does
 *     if current user does not have it. Tells where to go to get file.
 *
 *     Each user can have a list of files and there locations
 *
 *     Only create a FileData object if you have the file OR you have a FileData string
 *
 *  Use:
 *      Check validity before use
 *          Before use check if valid with - boolean isValid()
 *
 *      To get the FileData String for sending
 *          If sending the file information to another peer use - String getSendDataString()
 *          If storing locally use - String getDataString()
 *
 *      To create a FileData object with a FileData String:
 *          Constructor : FileData(String) where the string is in FileData format
 *          Function : boolean setFromDataString(String), returns false if not valid FileData
 *
 *      FileData stores 4 pieces of info on the file:
 *          name - the name of the file (e.g. info.txt)
 *          location - the directory path (if local) or the peer name (if not local)
 *          fileSize - the size of the file
 *          have - if true the file is local
 */
public class FileData {

    private String  name;       //  The name of the file
    private String  location;   //  directory if have file, name if other user has file
    private long    fileSize;   //  The size of the file
    private boolean have;       //  If the current user has the file, can match if name is the same somehow

    private boolean valid;      //  Makes sure that the data is not incomplete or invalid

    static public final String SPLIT_STR = "\n";

    /////
    //   Sets up the class from a FileData in String format
    //
    public FileData(String dataString) { setFromDataString(dataString); }

    /////
    //   Constructor to set up each variable
    //
    public FileData(String name, String location) {
        try {
            File temp = new File(location);
            if (!temp.exists()) throw new Exception("Not a valid path name");

            this.name = name;
            this.location = location;
            this.fileSize = new File(location).length();
            this.have = true;
            valid = true;
        } catch (Exception e) {
            valid = false;
            System.err.println(e.getMessage());
        }
    }

    /////
    //   Makes a FileData String without changing values for the string
    //
    public String getDataString() {
        return (name + SPLIT_STR + location + SPLIT_STR + fileSize + SPLIT_STR + have);
    }

    /////
    //   Basic getter functions
    //
    public String getFileLocation() { return (location); }
    public String getFileName()     { return (name); }
    public long   getFileSize()     { return (fileSize); }

    /////
    //   Used to get the string that would be sent to other users on the network
    // for their knowledge of where the file is. Makes a FileData string for other
    // users in the network (used if have file)
    //
    public String getSendDataString() {
        if (have) {
            return (name + SPLIT_STR + Model.getInstance().getMyName() + SPLIT_STR + fileSize + SPLIT_STR + String.valueOf(false));
        } else {
            return (getDataString());
        }
    }

    /////
    //   Returns a boolean of whether the current users has the file or not
    //
    public boolean isHave() { return (have); }

    /////
    //   Is used to see if the data is correct or valid, properly set up (prevent sending faulty info
    //
    public boolean isValid() { return (valid); }

    /////
    //   Gets data from a FileData string, returns if the object is valid
    //
    public boolean setFromDataString(String dataString) {
        String[] split = dataString.split(SPLIT_STR);

        try {
            if(split.length != 4) throw new Exception("Improper FileData string given");

            name = split[0];
            location = split[1];
            fileSize = Long.valueOf(split[2]);
            have = Boolean.valueOf(split[3]);

            valid = true;
        } catch (Exception e) {
            valid = false;
            System.err.println(e.getMessage());
        }
        return (valid);
    }

}
