package aaron.sanfs.client;

import aaron.sanfs.common.event.TaskEvent;
import aaron.sanfs.common.event.TaskEventListener;
import aaron.sanfs.common.util.Configuration;
import aaron.sanfs.common.util.WrapLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * GUI of the client
 * accept user operation, call corresponding function in Client
 *
 * @author gengyufeng
 */
public class ClientGUI implements TaskEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ClientGUI.class);

    JFrame frame = new JFrame("DFS");
    JPanel bottomPanel = new JPanel();
    JPanel topPanel = new JPanel();
    JScrollPane sp;
    //main file panel
    public JPanel filePanel;
    //file item
    public JPanel fileItem;
    //goback button
    JButton backButton;
    //right click
    JPopupMenu rightClickPop = new JPopupMenu();
    JPopupMenu mainRightClickPop = new JPopupMenu();
    JMenuItem menuDelete = new JMenuItem("删除");
    JMenuItem menuCut = new JMenuItem("剪切");
    JMenuItem menuPaste = new JMenuItem("粘贴");
    JMenuItem menuPaste1 = new JMenuItem("粘贴");
    JMenuItem menuRefresh = new JMenuItem("刷新");

    //current directory
    private String currentDirectory = "/";
    private String selectedItem = null;
    private String cutDir = null, cutName = null;

    private Client client = Client.getInstance();

    /**
     * init essential parts of GUI, must be called
     */
    public void init() {
        frame.setTitle("DFS -- " + currentDirectory);
        frame.setLayout(new BorderLayout());
        filePanel = new JPanel();
        filePanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 15));

        menuRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDirectory(currentDirectory);
            }
        });
        menuPaste.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.addListener(ClientGUI.this);
                logger.debug("To move from:" + cutDir + cutName
                        + " to:" + currentDirectory + cutName);
                client.moveFileDirectASync(cutDir, cutName, currentDirectory, cutName);
                cutDir = null;
                cutName = null;
            }
        });
        mainRightClickPop.add(menuRefresh);
        mainRightClickPop.add(menuPaste);
        filePanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON3) {
                    return;
                }
                JPanel item = (JPanel) e.getSource();
                if (null == cutDir) {
                    menuPaste.setEnabled(false);
                } else {
                    menuPaste.setEnabled(true);
                }
                mainRightClickPop.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        sp = new JScrollPane(filePanel);
        frame.add(sp, BorderLayout.CENTER);

        showDirectory(currentDirectory);

        //right click popup menu
        menuDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.addListener(ClientGUI.this);
                logger.debug("To delete:" + currentDirectory + selectedItem);
                client.removeFileDirectASync(currentDirectory, selectedItem);
                selectedItem = null;
            }
        });
        menuCut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cutDir = currentDirectory;
                cutName = selectedItem;
            }
        });
        menuPaste1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.addListener(ClientGUI.this);
                logger.debug("To move from:" + cutDir + cutName
                        + " to:" + currentDirectory + cutName);
                client.moveFileDirectASync(cutDir, cutName, currentDirectory, cutName);
                cutDir = null;
                cutName = null;
            }
        });

        rightClickPop.add(menuDelete);
        rightClickPop.add(menuCut);
        rightClickPop.add(menuPaste1);

        //top
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 20));

        //create directory
        JButton createDirButton = new JButton("新建目录");
        createDirButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String subdir = JOptionPane.showInputDialog("请输入子目录名称");
                if (null == subdir || subdir.equals(""))
                    return;
                client.addListener(ClientGUI.this);
                client.createDirectoryASync(currentDirectory + subdir + "/");
            }
        });

        //upload file
        JButton addButton = new JButton("上传文件");
        addButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser fChooser = new JFileChooser(".");
                fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int ret = fChooser.showOpenDialog(frame);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fChooser.getSelectedFile();
                    //TODO feed file to task
                    client.addListener(ClientGUI.this);
                    client.addFileAsync(currentDirectory, file.getName(), file);
                }
            }
        });


        //goback
        backButton = new JButton("返回上层");
        backButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int lastx = currentDirectory
                        .substring(0, currentDirectory.length() - 1).lastIndexOf("/");
                currentDirectory = currentDirectory.substring(0, lastx + 1);
                if (currentDirectory.equals("/")) {
                    backButton.setEnabled(false);
                }
                showDirectory(currentDirectory);
                frame.setTitle("DFS -- " + currentDirectory);
            }
        });
        backButton.setEnabled(false);

        //add buttons
        topPanel.add(createDirButton);
        topPanel.add(addButton);
        topPanel.add(backButton);


        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(700, 433);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * show the contents of directory
     *
     * @param dir full path directory address
     */
    private void showDirectory(String dir) {
        filePanel.removeAll();
        java.util.List<String> itemList = client.getDirectorySync(dir);
        for (String filename : itemList) {
            fileItem = new JPanel();
            fileItem.setLayout(new BorderLayout(0, 1));
            JLabel icon = new JLabel();
            icon.setIcon(IconFactory.getIcon(filename));
            icon.setHorizontalAlignment(JLabel.CENTER);
            JLabel text = new JLabel(filename.replace("/", ""));
            text.setHorizontalAlignment(JLabel.CENTER);
            fileItem.add(icon, BorderLayout.CENTER);
            fileItem.add(text, BorderLayout.SOUTH);

            if (filename.lastIndexOf("/") != filename.length() - 1) {
                String pathString = Configuration.getInstance().getString("client_dir") + currentDirectory + filename;
                pathString = pathString.replaceAll("\\\\", "////");
                File file = new File(pathString);
                if (file.exists()) {
                    fileItem.setBackground(Color.cyan);
                }
            }

            fileItem.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    JPanel item = (JPanel) e.getSource();
                    //right click
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        selectedItem = ((JLabel) item.getComponent(1)).getText();
                        if (((ImageIcon) ((JLabel) item.getComponent(0))
                                .getIcon()).getDescription().equals("dir")) {
                            selectedItem += "/";
                        }
                        if (null == cutDir) {
                            menuPaste1.setEnabled(false);
                        } else {
                            menuPaste1.setEnabled(true);
                        }
                        rightClickPop.show(e.getComponent(), e.getX(), e.getY());
                        return;
                    }
                    if (e.getClickCount() == 2) {
                        //directory
                        if (((ImageIcon) ((JLabel) item.getComponent(0))
                                .getIcon()).getDescription().equals("dir")) {
                            String pathString = currentDirectory + ((JLabel) item.getComponent(1)).getText() + "/";
                            currentDirectory = pathString;
                            backButton.setEnabled(true);
                            showDirectory(pathString);
                            frame.setTitle("DFS -- " + currentDirectory);
                        } else {    //file
                            String localPath = Configuration.getInstance().getString("client_dir");
                            String fileString = localPath + currentDirectory + ((JLabel) item.getComponent(1)).getText();
                            fileString = fileString.replaceAll("\\\\", "////");
                            logger.debug(">>>" + fileString);
                            File file = new File(fileString);
                            if (!file.exists()) {
                                logger.info("Creating local dir");
                                file.mkdirs();
                            }
                            if (file.exists()) {
                                file.delete();
                                fileItem.setBackground(null);
                            }
                            client.getFileAsync(currentDirectory, ((JLabel) item.getComponent(1)).getText(), file);
                        }
                    }
                }
            });

            fileItem.setToolTipText(text.getText());
            fileItem.setPreferredSize(new Dimension(45, 55));
            filePanel.add(fileItem);
        }
        filePanel.updateUI();
    }

    /**
     * return ImageIcon according to file suffix
     *
     * @author geng yufeng
     */
    private static class IconFactory {
        private static final String imageDirPath = System.getProperty("user.dir");
        private static final ImageIcon dirIcon = new ImageIcon(imageDirPath + "/ico/folder.png");
        private static final ImageIcon txtIcon = new ImageIcon(imageDirPath + "/ico/txt.png");
        private static final ImageIcon videoIcon = new ImageIcon(imageDirPath + "/ico/avi.png");
        private static final ImageIcon imgIcon = new ImageIcon(imageDirPath + "/ico/pic.png");
        private static final ImageIcon otherIcon = new ImageIcon(imageDirPath + "/ico/pict.png");

        public static ImageIcon getIcon(String filename) {
            int lasts = filename.lastIndexOf("/");
            if (lasts == filename.length() - 1) {
                dirIcon.setDescription("dir");
                return dirIcon;
            }
            filename = filename.substring(lasts + 1, filename.length());
            String[] tmp = filename.split("\\.");
            String suffix = tmp[tmp.length - 1];
            if (suffix.equals("txt")) {
                return txtIcon;
            } else if (suffix.equals("avi") || suffix.equals("rmvb")) {
                return videoIcon;
            } else if (suffix.equals("txt")) {
                return txtIcon;
            } else if (suffix.equals("png") || suffix.equals("jpg")) {
                return imgIcon;
            }
            return otherIcon;
        }
    }

    @Override
    public void handle(TaskEvent event) {
        // TODO Auto-generated method stub
        showDirectory(currentDirectory);
    }
}
