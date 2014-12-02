package dcm3203.ui;

import dcm3203.data.FileData;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.*;

/**
 * Created by Michael on 01/12/2014.
 *
 */

public class GetFileDialog extends JDialog {

    private final List<FileData> fileList;
    private final JList<String> fileJList;
    private final JScrollPane listPane;

    private static final String[] NO_FILES = {"You have advertised no files"};

    public GetFileDialog(Frame owner, String title, boolean modal, List<FileData> fileList) {
        super(owner, title, modal);
        this.fileList = fileList;

        setSize(500,400);
        setLayout(null);
        setResizable(false);
        setLocation((int)(getToolkit().getScreenSize().getWidth()/2 - getWidth()/2),
                (int)(getToolkit().getScreenSize().getHeight()/2 - getHeight()/2));

        fileJList = new JList<String>();
        listPane = new JScrollPane(fileJList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listPane.setLocation(5,5);
        listPane.setSize(485,300);
        this.add(listPane);

        if (fileList.isEmpty()) {
            fileJList.setListData(NO_FILES);
        } else {
            String[] fileNames = new String[fileList.size()];
            ListIterator<FileData> it = fileList.listIterator();

            for (int i = 0; i < fileNames.length && it.hasNext(); ++i) {
                fileNames[i] = it.next().getFileName();
            }

            fileJList.setListData(fileNames);
        }

        this.setVisible(true);
    }

}
