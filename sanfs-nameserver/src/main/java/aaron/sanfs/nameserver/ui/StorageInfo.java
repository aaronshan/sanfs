package aaron.sanfs.nameserver.ui;

import aaron.sanfs.nameserver.status.Storage;

import javax.swing.*;
import java.awt.*;

/**
 * Storage information panel.
 * <p/>
 * This panel is pretty ugly.
 *
 * @author lishunyang
 */
public class StorageInfo {
    /**
     * Panel instance.
     */
    private JPanel panel;

    /**
     * Storage id.
     */
    private JTextArea id;

    /**
     * Storage saving load.
     */
    private JProgressBar load;

    /**
     * Storage sum of running task.
     */
    private JTextArea taskSum;

    /**
     * Storage icon.
     */
    private static final ImageIcon serverIcon = new ImageIcon("ico/server.png");

    /**
     * Construction method.
     */
    public StorageInfo() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        id = new JTextArea();
        load = new JProgressBar();
        load.setMinimum(0);
        load.setMaximum(100);

        taskSum = new JTextArea();

        JLabel icon = new JLabel();
        icon.setIcon(serverIcon);
        icon.setHorizontalAlignment(JLabel.CENTER);
        panel.add(icon, BorderLayout.CENTER);

        JPanel subPanel = new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
        subPanel.add(load);
        subPanel.add(taskSum);
        panel.add(id, BorderLayout.NORTH);
        panel.add(subPanel, BorderLayout.SOUTH);
    }

    /**
     * Update storage panel inforation.
     *
     * @param storage
     */
    public void update(Storage storage) {
        this.id.setText(storage.getId());
        this.load.setValue(storage.getStorageLoad());
        this.taskSum.setText("Running task: " + storage.getTaskSum());
    }

    /**
     * Get panel instance.
     *
     * @return
     */
    public JPanel getPanel() {
        return panel;
    }
}
