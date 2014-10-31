package dcm3203;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Colin on 2014-10-28.
 */
public class Controller {
    private View myView;
    private Model myModel;
    /**
     * Entry method
     * @param args command line args, ignored
     */
    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.run();
    }

    public Controller() {
        myModel = new Model();
        myView = new View(myModel, this);
    }

    public void run() {
        //TODO spin off new Thread for incoming connection handling
        //TODO spin off new Thread for UDP discovery handling
        for (int i = 0; i < 250; i++) {
            myModel.addMessage("new message: " + i);
            myView.update();
        }

        for (int i = 0; i < 40; i ++) {
            myModel.addUser(new User("User " + i, null));
            myView.update();
        }

        //TODO handle incoming messages from users
    }

    public ActionListener getSendListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myModel.addMessage("Send message!!!");
                myView.update();
            }
        };
    }
}
