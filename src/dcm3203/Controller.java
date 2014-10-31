package dcm3203;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Colin on 2014-10-28.
 */
public class Controller {
    private View myView;
    /**
     * Entry method
     * @param args command line args, ignored
     */
    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.run();
    }

    public Controller() {
        myView = new View(new Model(), this);
    }

    public void run() {

    }

    //TODO implement action listener
    public ActionListener getSendListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Send Button Test");
            }
        };
    }
}
