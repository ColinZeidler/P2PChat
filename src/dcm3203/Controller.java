package dcm3203;

/**
 * Created by Colin on 2014-10-28.
 */
public class Controller {
    /**
     * Entry method
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Hello!");
        View myView = new View(new Model());
        //TODO add listners
        System.out.println("Goodbye");
    }

    //TODO add functions for view callbacks
}
