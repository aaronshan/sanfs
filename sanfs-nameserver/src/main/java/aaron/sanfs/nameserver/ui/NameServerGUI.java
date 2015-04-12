package aaron.sanfs.nameserver.ui;

import aaron.sanfs.nameserver.meta.Meta;
import aaron.sanfs.nameserver.status.StatusEvent;
import aaron.sanfs.nameserver.status.StatusEventListener;
import aaron.sanfs.nameserver.status.Storage;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Graphical User Interface of NameServer.
 * <p/>
 * This GUI is pretty ugly..
 *
 * @author lishunyang
 */
public class NameServerGUI implements StatusEventListener {
    /**
     * Single instance pattern.
     */
    private static NameServerGUI instance = new NameServerGUI();

    /**
     * Main frame.
     */
    private JFrame frame = new JFrame("Name Server");

    /**
     * Tab component.
     */
    private JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP,
            JTabbedPane.WRAP_TAB_LAYOUT);

    /**
     * Storage status information panel.
     */
    private JPanel storagePanel = new JPanel();

    /**
     * Meta data information panel.
     */
    private JPanel directoryPanel = new JPanel();

    /**
     * Status of storages, for storages can be added or removed dynamically.
     */
    private Map<Storage, StorageInfo> storages =
            new HashMap<Storage, StorageInfo>();

    /**
     * Construction method.
     */
    private NameServerGUI() {
    }

    /**
     * Get GUI instance.
     *
     * @return
     */
    public static NameServerGUI getInstance() {
        return instance;
    }

    /**
     * Initialize GUI.
     */
    public void init() {
        frame.setBounds(100, 100, 450, 300);
        frame.setLayout(new BorderLayout());

        storagePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));

        tab.addTab("Storages", storagePanel);
        tab.addTab("Directory", directoryPanel);
        tab.setSelectedIndex(0);
        frame.add(tab, BorderLayout.CENTER);

        final MetaInfo metaInfo = new MetaInfo(Meta.getInstance());
        directoryPanel.add(metaInfo.getPanel());

        frame.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void handle(StatusEvent event) {
        switch (event.getType()) {
            case STORAGE_DEAD:
                removeStoragePanel(event.getStorage());
                break;
            case STORAGE_REGISTERED:
                addStoragePanel(event.getStorage());
                break;
            case HEARTBEAT:
            case LOAD_CHANGED:
            case TASK_SUM_CHANGED:
                updateStoragePanel(event.getStorage());
                break;
        }
    }

    /**
     * Add a new storage panel.
     *
     * @param storage
     */
    private void addStoragePanel(Storage storage) {
        final StorageInfo info = new StorageInfo();

        info.update(storage);
        storages.put(storage, info);
        storagePanel.add(info.getPanel());

        frame.validate();
    }

    /**
     * Update storage panel information.
     *
     * @param storage
     */
    private void updateStoragePanel(Storage storage) {
        final StorageInfo info = storages.get(storage);
        if (null == info)
            return;
        info.update(storage);
    }

    /**
     * Remove a specified storage panel.
     *
     * @param storage
     */
    private void removeStoragePanel(Storage storage) {
        final StorageInfo panel = storages.remove(storage);

        frame.remove(panel.getPanel());

        frame.validate();
    }
}
