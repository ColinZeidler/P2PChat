package dcm3203.data;

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
 */
public class FileData {

    private String  name;       //  The name of the file
    private String  location;   //  directory if have file, name if other user has file
    private boolean have;       //  If the current user has the file, can match if name is the same somehow

    static private final String SPLIT_STR = "\n";

    /////
    //   Sets up the class from a FileData in String format
    //
    public FileData(String dataString) { setFromDataString(dataString); }

    /////
    //   Assumed to have file if creating without FileData string
    //
    public FileData(String name, String location) { this(name, location, true); }

    /////
    //   Constructor to set up each variable
    //
    public FileData(String name, String location, boolean have) {
        this.name = name;
        this.location = location;
        this.have = have;
    }

    /////
    //   Used to get the string that would be sent to other users on the network
    // for their knowledge of where the file is. Makes a FileData string for other
    // users in the network (used if have file)
    //
    public String getSendDataString() {
        return (name + SPLIT_STR + Model.getInstance().getMyName() + SPLIT_STR + String.valueOf(false));
    }

    /////
    //   Makes a FileData String without changing values for the string
    //
    public String getDataString() {
        return (name + SPLIT_STR + location + SPLIT_STR + have);
    }

    /////
    //   Gets data from a FileData string
    //
    public void setFromDataString(String dataString) {
        String[] split = dataString.split(SPLIT_STR);

        try {
            if(split.length != 3) throw new Exception("Improper FileData string given");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        name = split[0];
        location = split[1];
        have = Boolean.valueOf(split[2]);
    }

}