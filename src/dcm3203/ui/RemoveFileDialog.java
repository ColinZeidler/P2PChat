package dcm3203.ui;

import dcm3203.data.FileData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * Created by Michael on 13/11/2014.
 *
 */
public class RemoveFileDialog extends JDialog{

    private final Vector<FileData> fileList;
    private final JList<String> fileJList;
    private final JScrollPane listPane;

    private static final String[] NO_FILES = {"You have advertised no files"};

    public RemoveFileDialog(Frame owner, String title, boolean modal, final Vector<FileData> fileList) {
        super(owner, title, modal);
        this.fileList = fileList;

        setSize(600,400);
        setLayout(null);
        setResizable(false);
        setLocation((int)(getToolkit().getScreenSize().getWidth()/2 - getWidth()/2),
                (int)(getToolkit().getScreenSize().getHeight()/2 - getHeight()/2));

        fileJList = new JList<String>();
        if (fileList.isEmpty()) {
            fileJList.setListData(NO_FILES);
        } else {
            String[] fileNames = new String[fileList.size()];

            for (int i=0;i<fileNames.length;++i) {
                fileNames[i] = fileList.get(i).getFileName();
            }

            fileJList.setListData(fileNames);
        }
        listPane = new JScrollPane(fileJList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fileJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileJList.setSelectedIndex(fileJList.getSelectedIndex());
                }
        });
        fileJList.setSize(500,300);
        fileJList.setLocation(5,5);
        this.add(listPane);
        //this.setVisible(true);
    }

    public FileData getRemoveFile() {
        this.setVisible(true);
        if (fileJList.getSelectedValue().equals(NO_FILES[0])) {
            return(null);
        }
        FileData toReturn = fileList.elementAt(fileJList.getSelectedIndex());
        this.dispose();
        return(toReturn);
    }
}
