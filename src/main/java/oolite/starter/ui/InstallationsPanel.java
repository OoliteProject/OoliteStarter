/*
 */
package oolite.starter.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.TableRowSorter;
import oolite.starter.Configuration;
import oolite.starter.model.Installation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class InstallationsPanel extends javax.swing.JPanel {
    private static final Logger log = LogManager.getLogger();

    private static final String INSTALLATIONSPANEL_ERROR = "Error";
    private static final String INSTALLATIONSPANEL_SELECT_ROW = "Please select row";
    private static final String INSTALLATIONSPANEL_COULD_NOT_SAVE = "Could not save";
    
    private InstallationForm installationDetails;
    private InstallationTableModel model;
    private transient Configuration configuration;
    
    private boolean configDirty;
    
    /**
     * Creates new form InstallationsPanel.
     */
    public InstallationsPanel() {
        initComponents();
        
        installationDetails = new InstallationForm();
        installationDetails.setEnabled(false);
        jSplitPane1.setRightComponent(installationDetails);
        
        configDirty = false;
        
        setButtonColors();
    }
    
    /**
     * Sets the configuration to be used for managing installations.
     * 
     * @param configuration the configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;

        model = new InstallationTableModel(configuration);
        jTable1.setModel(model);
        jTable1.getSelectionModel().addListSelectionListener(lse -> {
            log.debug("valueChanged({})", lse);
            if (!lse.getValueIsAdjusting()) {
                // we have a final value - let's render it
                showDetailsOfSelection();
            }
        });
        jTable1.getSelectionModel().addListSelectionListener(lse -> {
            log.debug("valueChanged({})", lse);
            if (!lse.getValueIsAdjusting()) {
                // we have a final value - let's render it
                showDetailsOfSelection();
            }
        });
        TableRowSorter<InstallationTableModel> trw = new TableRowSorter<>(model);
        jTable1.setRowSorter(trw);
        
        setButtonColors();
    }
    
    private void showDetailsOfSelection() {
        int rowIndex = jTable1.getSelectedRow();
        if (rowIndex >=0) {
            rowIndex = jTable1.convertRowIndexToModel(rowIndex);
            Installation row = model.getRow(rowIndex);
            installationDetails.setData(row);
        } else {
            installationDetails.setData(null);
        }
    }
    
    private void ensureDirectoryExists(String dirName, String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory() && JOptionPane.showOptionDialog(
                this, 
                "Directory \n" + dirPath + "\ndoes not exist. Would you like it created?",
                dirName + " missing",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null
        ) == JOptionPane.OK_OPTION) {
            dir.mkdirs();
        }
    }
    
    private void setButtonColors() {
        UIDefaults uidefaults = UIManager.getLookAndFeelDefaults();
        Color defaultBackground = uidefaults.getColor("Button.background");

        if (model != null && model.getRowCount()==0) {
            btAdd.setBackground(Color.red);
            btActivate.setBackground(defaultBackground);
        } else {
            btAdd.setBackground(defaultBackground);

            if (configuration != null && configuration.getActiveInstallation() == null) {
                btActivate.setBackground(Color.red);
            } else {
                btActivate.setBackground(defaultBackground);
            }
        }
        
        
        if (configDirty) {
            btSave.setBackground(Color.red);
        } else {
            
//            for (Enumeration enumm = uidefaults.keys(); enumm.hasMoreElements(); ) {
//                Object key = enumm.nextElement();
//                Object value = uidefaults.get(key);
//                
//                if (String.valueOf(key).startsWith("Button.background")) {
//                    log.warn("have key '{}'={}", key, value);
//                }
//            }
            
            btSave.setBackground(defaultBackground);
        }
    }
    
    private void setConfigDirty(boolean configDirty) {
        this.configDirty = configDirty;
        
        setButtonColors();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btAdd = new javax.swing.JButton();
        btEdit = new javax.swing.JButton();
        btRemove = new javax.swing.JButton();
        btScan = new javax.swing.JButton();
        btSave = new javax.swing.JButton();
        btActivate = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setName("Oolite Versions"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        btAdd.setText("Add...");
        btAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddActionPerformed(evt);
            }
        });

        btEdit.setText("Edit...");
        btEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEditActionPerformed(evt);
            }
        });

        btRemove.setText("Remove");
        btRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoveActionPerformed(evt);
            }
        });

        btScan.setText("Scan...");
        btScan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScanActionPerformed(evt);
            }
        });

        btSave.setText("Save");
        btSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSaveActionPerformed(evt);
            }
        });

        btActivate.setText("Activate");
        btActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btActivateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(btAdd)
                .addGap(6, 6, 6)
                .addComponent(btEdit)
                .addGap(6, 6, 6)
                .addComponent(btRemove)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btScan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btActivate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                .addComponent(btSave)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btAdd)
                    .addComponent(btEdit)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btRemove)
                        .addComponent(btScan)
                        .addComponent(btSave)
                        .addComponent(btActivate)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setOneTouchExpandable(true);

        jScrollPane1.setViewportView(jTable1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScanActionPerformed
        log.debug("btScanActionPerformed({})", evt);

        JFrame f2 = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog ipd = new JDialog(f2, "Select Oolite installation", true);
        InstallationPicker ip = new InstallationPicker();
        ipd.add(ip);
        ipd.pack();
        ipd.setLocationRelativeTo(f2);
        
        SwingWorker<List<String>, String> worker = new SwingWorker<List<String>, String>() {
            
            private List<Pattern> skipPatterns = new ArrayList<>();
            private List<Pattern> goodPatterns = new ArrayList<>();
            private List<String> result;
            private HashSet<String> scannedFiles;
            private int totalFiles;
            
            private void scan(File f) throws IOException {
                log.trace("scan({})", f);
                log.trace("already scanned {}/{} files", scannedFiles.size(), totalFiles);

                publish (f.getAbsolutePath());
                
                if (scannedFiles.contains(f.getCanonicalPath())) {
                    return;
                }
                scannedFiles.add(f.getCanonicalPath());

                for (Pattern p: skipPatterns) {
                    if (p.matcher(f.getAbsolutePath()).matches()) {
                        return;
                    }
                }
                
                for (Pattern p: goodPatterns) {
                    Matcher m = p.matcher(f.getAbsolutePath());
                    if (m.matches()) {
                        //String s = f.getAbsolutePath();
                        String s = m.group(1);
                        result.add(s);
                        
                        // add to installations panel
                        ip.addInstallation(s);
                        publish(s);
                    }
                }
                        
                if (f.isDirectory()) {
                    File[] entries = f.listFiles();
                    if (entries != null) {
                        totalFiles += entries.length;
                        for (File entry: entries ) {
                            scan(entry);
                            
                            if (isCancelled() ) {
                            //if (isCancelled() || monitor.isCanceled()) {
                                return;
                            }
                        }
                    }
                }
            }
            
            /**
             * Entry point for this SwingWorker.
             * Scans the filesystem, then returns the collected results.
             */
            @Override
            protected List<String> doInBackground() throws Exception {
                log.debug("doInBackground()");
                ip.startScan();
                
                scannedFiles = new HashSet<>();

                skipPatterns.add(Pattern.compile("^/proc/.*"));
                skipPatterns.add(Pattern.compile("^/sys/.*"));
                skipPatterns.add(Pattern.compile(".*/proc/self/.*"));
                skipPatterns.add(Pattern.compile(".*/proc/thread-self/.*"));
                skipPatterns.add(Pattern.compile(".*/proc/\\d+/.*"));
                skipPatterns.add(Pattern.compile(".*/cwd/proc/.*/cwd/proc/.*"));
                skipPatterns.add(Pattern.compile(".*/cwd/sys/class/.*"));
                skipPatterns.add(Pattern.compile(".*/cwd/sys/devices/.*"));
                skipPatterns.add(Pattern.compile(".*/cwd/sys/dev/.*"));
                skipPatterns.add(Pattern.compile(".*/sys/class/.*"));
                skipPatterns.add(Pattern.compile(".*/sys/devices/.*"));
                skipPatterns.add(Pattern.compile(".*/sys/dev/.*"));
                skipPatterns.add(Pattern.compile(".*/sys/bus/.*"));
                skipPatterns.add(Pattern.compile(".*/sys/block/.*"));
                skipPatterns.add(Pattern.compile(".*/sys/module/.*"));
                
                // Linux version
                goodPatterns.add(Pattern.compile("(.*/oolite.app)/oolite-wrapper"));
                // Mac OS version
                goodPatterns.add(Pattern.compile("(.*\\.app)/Contents/MacOS/Oolite"));
                // Windows version
                goodPatterns.add(Pattern.compile("(.*\\\\oolite.app)\\\\oolite.exe"));
                
                try {
                    result = new ArrayList<>();

                    totalFiles += File.listRoots().length + 1;
                    
                    scan(new File(System.getProperty("user.home")));
                    
                    for(File f: File.listRoots()) {
                        scan(f);
                    }

                    return result;
                } catch (Exception e) {
                    log.error("could not scan", e);
                    throw new Exception("could not scan", e);
                }
            }

            @Override
            protected void process(List<String> chunks) {
                log.trace("process({})", chunks);
                
                // can we read something from the amount of chunks?
                
                if (!chunks.isEmpty()) {
                    ip.setNote(chunks.get(0));
                }
            }

            @Override
            protected void done() {
                //log.debug("done()");
                //monitor.close();
                ip.stopScan();
                ip.setNote("Scanning finished.");
                btScan.setEnabled(true);
                
                log.debug("Found {} installations {}", result.size(), result);
            }
            
        };

        ip.addCancelListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ipd.setVisible(false);
                worker.cancel(true);
            }
        });
        ip.addOkListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ipd.setVisible(false);
                worker.cancel(true);
                
                log.info("something was selected - we want this value {}", ip.getSelectedInstallation());
                
            }
        });

        worker.execute();
        ipd.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                worker.cancel(true);
            }

        });
        ipd.setVisible(true);
        
    }//GEN-LAST:event_btScanActionPerformed

    private void btAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddActionPerformed
        log.debug("btAddActionPerformed({})", evt);
        
        try {
            InstallationForm installationForm = new InstallationForm();
            if (JOptionPane.showOptionDialog(this, installationForm, "Add Oolite version...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
                model.addRow(installationForm.getData());
                setConfigDirty(true);
            }
        } catch (Exception e) {
            log.error(INSTALLATIONSPANEL_ERROR, e);
            JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_ERROR);
        }
    }//GEN-LAST:event_btAddActionPerformed

    private void btEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEditActionPerformed
        log.debug("btEditActionPerformed({})", evt);
        
        try {
            int rowIndex = jTable1.getSelectedRow();
            if (rowIndex == -1) {
                JOptionPane.showConfirmDialog(this, INSTALLATIONSPANEL_SELECT_ROW);
                return;
            }
            
            rowIndex = jTable1.convertRowIndexToModel(rowIndex);
            Installation i = model.getRow(rowIndex);
            
            InstallationForm installationForm = new InstallationForm();
            installationForm.setData(i);
            
            if (JOptionPane.showOptionDialog(this, installationForm, "Add Installation...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
                Installation data = installationForm.getData();
                model.updateRow(data);
                if (model.getRow(jTable1.getSelectedRow()) == data) {
                    this.installationDetails.setData(data);
                }
                setConfigDirty(true);
            }
        } catch (Exception e) {
            log.error(INSTALLATIONSPANEL_ERROR, e);
            JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_ERROR);
        }        
    }//GEN-LAST:event_btEditActionPerformed

    private void btRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveActionPerformed
        log.debug("btRemoveActionPerformed({})", evt);
        
        try {
            int rowIndex = jTable1.getSelectedRow();
            if (rowIndex == -1) {
                JOptionPane.showConfirmDialog(this, INSTALLATIONSPANEL_SELECT_ROW);
                return;
            }
            
            rowIndex = jTable1.convertRowIndexToModel(rowIndex);
            model.removeRow(rowIndex);

            setConfigDirty(true);
        } catch (Exception e) {
            log.error(INSTALLATIONSPANEL_ERROR, e);
            JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_ERROR);
        }        
    }//GEN-LAST:event_btRemoveActionPerformed

    private void btSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSaveActionPerformed
        log.debug("btSaveActionPerformed({})", evt);
        
        try {
            File f = new File(System.getProperty("user.home") + File.separator + ".oolite-starter.conf");
            configuration.saveConfiguration(f);
            
            //JOptionPane.showMessageDialog(this, "Stored configuration in " + f.getAbsolutePath(), "Save", JOptionPane.INFORMATION_MESSAGE);
            
            StringBuilder sb = new StringBuilder("<html>");
            if (configuration.getActiveInstallation() == null) {
                sb.append("<p>Nice try, kiddo!</p><p>Your configuration was stored in ").append(f.getAbsolutePath()).append(".</p>");
                sb.append("<p>But... you still ain't got an active Oolite version. Expect trouble to follow your pants.</p>");
            } else {
                sb.append("<p>Smart move, kiddo!</p><p>Your configuration was stored in ").append(f.getAbsolutePath()).append(".</p>");
                sb.append("<p>Next time we won't have to fasten these screws again.</p>");
            }
            sb.append("</html>");
            MrGimlet.showMessage(this, sb.toString());
            
            setConfigDirty(false);
        } catch (Exception e) {
            log.error(INSTALLATIONSPANEL_COULD_NOT_SAVE, e);
            JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_COULD_NOT_SAVE);
        }        
    }//GEN-LAST:event_btSaveActionPerformed

    private void btActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btActivateActionPerformed
        log.debug("btActivateActionPerformed({})", evt);
        
        try {
            int rowIndex = jTable1.getSelectedRow();
            if (rowIndex == -1) {
                JOptionPane.showConfirmDialog(this, INSTALLATIONSPANEL_SELECT_ROW);
                return;
            }
            
            int modelIndex = jTable1.convertRowIndexToModel(rowIndex);
            Installation i = model.getRow(modelIndex);
            
            // validate installation
            ensureDirectoryExists("AddonDir", i.getAddonDir());
            ensureDirectoryExists("Deactivated AddonDir", i.getDeactivatedAddonDir());
            ensureDirectoryExists("Managed AddonDir", i.getManagedAddonDir());
            ensureDirectoryExists("Managed Deactivated AddonDir", i.getManagedDeactivatedAddonDir());
            
            configuration.activateInstallation(i);
            model.fireTableDataChanged();
            jTable1.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);

            setConfigDirty(true);
        } catch (Exception e) {
            log.error(INSTALLATIONSPANEL_COULD_NOT_SAVE, e);
            JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_COULD_NOT_SAVE);
        }        
    }//GEN-LAST:event_btActivateActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btActivate;
    private javax.swing.JButton btAdd;
    private javax.swing.JButton btEdit;
    private javax.swing.JButton btRemove;
    private javax.swing.JButton btSave;
    private javax.swing.JButton btScan;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
