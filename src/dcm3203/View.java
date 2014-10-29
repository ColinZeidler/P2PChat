package dcm3203;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Colin on 2014-10-28.
 */
public class View extends JFrame{
    private Model myModel;

    public View(Model model) {
        setTitle("Application name");
        setSize(500, 600);
        setLocation(100, 100);

        //rows, columns, vertical gap, horizontal gap
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        //TODO add chat log


        //TODO add user list


        //TODO add message bar


        //TODO add send button


        setVisible(true);
    }
}
