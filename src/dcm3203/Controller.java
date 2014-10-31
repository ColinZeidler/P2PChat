package dcm3203;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        View myView = new View(new Model(), new Controller());
        //TODO add listeners
        System.out.println("Goodbye");
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
