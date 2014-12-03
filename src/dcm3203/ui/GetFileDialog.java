package dcm3203.ui;

import dcm3203.data.FileData;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ListIterator;
import javax.swing.*;

/**
 * Created by Michael on 01/12/2014.
 *
 *  GetFileDialog
 *      This dialog is used to obtain a file from the list specified on construction. Used in
 *    two different cases in the program, once for removing file advertisements, and the other
 *    when requesting a file for transfer.
 *
 *      Use: Make new dialog, will do the work to get file from the list
 *          After call getFileSelected which returns the selected file
 *
 */

public class GetFileDialog extends JDialog {

    private FileData                fileSelected;                         //  The selected file

    private static final String[]   NO_FILES = {"No Files Advertised"};   //  If no files are advertised

    public GetFileDialog(Frame owner, String title, boolean modal, final List<FileData> fileList) {
        super(owner, title, modal);

        setSize(500,400);
        setLayout(null);
        setResizable(false);
        setLocation((int)(getToolkit().getScreenSize().getWidth()/2 - getWidth()/2),
                (int)(getToolkit().getScreenSize().getHeight()/2 - getHeight()/2));

        final JList<String> fileJList = new JList<String>();
        JScrollPane listPane = new JScrollPane(fileJList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listPane.setLocation(5,5);
        listPane.setSize(485, 300);
        this.add(listPane);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setLocation(340, listPane.getHeight() + 10);
        cancelButton.setSize(150, 25);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileSelected = null;
                setVisible(false);
            }
        });
        add(cancelButton);

        JButton selectFileButton = new JButton("Select");
        selectFileButton.setSize(cancelButton.getSize());
        selectFileButton.setLocation(cancelButton.getLocation().x - selectFileButton.getWidth() - 5,
                cancelButton.getLocation().y);
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileSelected = fileList.get(fileJList.getSelectedIndex());
                setVisible(false);
            }
        });
        add(selectFileButton);

        if (fileList.isEmpty()) {
            fileJList.setListData(NO_FILES);
        } else {
            String[] fileNames = new String[fileList.size()];
            ListIterator<FileData> it = fileList.listIterator();

            for (int i = 0; i < fileNames.length && it.hasNext(); ++i)
                fileNames[i] = it.next().getFileName();

            fileJList.setListData(fileNames);
        }

        this.setVisible(true);
    }

    /////
    //   Returns the selected file
    //
    public FileData getFileSelected() { return (fileSelected); }
}
