package dcm3203.data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Michael on 12/11/2014.
 */
public class FileList {
    private HashMap<User, ArrayList<FileData>> list;

    FileList() {
        list = new HashMap<User, ArrayList<FileData>>();
    }
}
