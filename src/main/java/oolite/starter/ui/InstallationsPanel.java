/*
 */
package oolite.starter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import oolite.starter.Configuration;
import oolite.starter.Oolite;
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
    private static final String INSTALLATIONSPANEL_COULD_NOT_ACTIVATE = "Could not select";
    
    private static final ImageIcon icon_star = new ImageIcon(InstallationsPanel.class.getResource("/icons/star_FILL1_wght400_GRAD0_opsz24.png"));
    
    private InstallationForm installationDetails;
    private InstallationTableModel model;
    private transient Configuration configuration;
    
    /**
     * Flag to indicate whether the configuration was saved after modification.
     * TODO: This flag should go into the configuration class.
     */
    private boolean configDirty;
    private SecureRandom random;

    /**
     * Creates new form InstallationsPanel.
     */
    public InstallationsPanel() {
        random = new SecureRandom();
        
        initComponents();
        
        installationDetails = new InstallationForm();
        installationDetails.setEnabled(false);
        jSplitPane1.setRightComponent(installationDetails);
        
        configDirty = false;
        
        setButtonColors();
        
        jTable1.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer(){
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
                if (c instanceof JLabel l) {
                    l.setText("");
                    l.setHorizontalAlignment(CENTER);
                    if (Boolean.TRUE.equals(value)) {
                        l.setIcon(icon_star);
                    } else {
                        l.setIcon(null);
                    }
                }
                return c;
            }
            
        });
        
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JTable table = (JTable)e.getSource();
                int row = table.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && row != -1) {
                    activateRow(row);
                }
            }
            
        });
    }
    
    /**
     * Sets the configuration to be used for managing installations.
     * 
     * @param configuration the configuration
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;

        model = new InstallationTableModel(configuration);
        model.addTableModelListener(tme -> {
            log.debug("tableChanged({})", tme);
            if (tme.getSource() == model) {
                log.trace("it is our tablemodel!");
                setButtonColors();
            }
        });
        
        jTable1.setModel(model);
        Util.setColumnWidths(jTable1);
        
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
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        trw.setSortKeys(sortKeys);
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
        log.debug("setButtonColors()");
        UIDefaults uidefaults = UIManager.getLookAndFeelDefaults();
        Color defaultBackground = uidefaults.getColor("Button.background");

        if (model == null || model.getRowCount() == 0) {
            btAdd.setBackground(Configuration.COLOR_ATTENTION);
            btScan.setBackground(Configuration.COLOR_ATTENTION);
            btActivate.setBackground(defaultBackground);
        } else {                
            btAdd.setBackground(defaultBackground);
            btScan.setBackground(defaultBackground);
                
            if (configuration != null && configuration.getActiveInstallation() == null) {
                btActivate.setBackground(Configuration.COLOR_ATTENTION);
            } else {
                btActivate.setBackground(defaultBackground);
            }
        }
        
        if (configuration != null && configuration.isDirty()) {
            btSave.setBackground(Configuration.COLOR_ATTENTION);
        } else {
            btSave.setBackground(defaultBackground);
        }
    }
    
    private void setConfigDirty(boolean configDirty) {
        this.configDirty = configDirty;
        if (configDirty) {
            configuration.setDirty(true);
        }
        setButtonColors();
    }

    void activateRow(int rowIndex) {
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
        configuration.setDirty(true);
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

        btAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/note_add_24dp_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btAdd.setText("Add...");
        btAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddActionPerformed(evt);
            }
        });

        btEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btEdit.setText("Edit...");
        btEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEditActionPerformed(evt);
            }
        });

        btRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/scan_delete_24dp_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btRemove.setText("Remove");
        btRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoveActionPerformed(evt);
            }
        });

        btScan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/search_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btScan.setText("Scan...");
        btScan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btScanActionPerformed(evt);
            }
        });

        btSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/download_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btSave.setText("Save");
        btSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSaveActionPerformed(evt);
            }
        });

        btActivate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/star_FILL1_wght400_GRAD0_opsz24.png"))); // NOI18N
        btActivate.setText("Select");
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
                .addContainerGap()
                .addComponent(btScan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btAdd)
                .addGap(6, 6, 6)
                .addComponent(btEdit)
                .addGap(6, 6, 6)
                .addComponent(btRemove)
                .addGap(88, 88, 88)
                .addComponent(btActivate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btSave)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btAdd)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btEdit)
                        .addComponent(btScan))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btRemove)
                        .addComponent(btSave)
                        .addComponent(btActivate)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setDividerSize(7);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setOneTouchExpandable(true);

        jScrollPane1.setViewportView(jTable1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btScanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btScanActionPerformed
        log.debug("btScanActionPerformed({})", evt);

        JFrame f2 = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog ipd = new JDialog(f2, "Select Oolite Home Directory", true);
        InstallationPicker ip = new InstallationPicker();
        ipd.add(ip);
        ipd.pack();
        ipd.setLocationRelativeTo(f2);

        ScanOolitesSwingWorker worker = new ScanOolitesSwingWorker(ip);

        ip.addCancelListener(ae -> {
            try {
                ipd.setVisible(false);
                worker.cancel(true);
            } catch (Exception e) {
                log.error("Could not cleanup after cancel", e);
            }
        });
        ip.addOkListener(ae -> {
            try {
                ipd.setVisible(false);
                worker.cancel(true);

                log.info("something was selected - we want this value {}", ip.getSelectedInstallation());

                File homeDir = new File(ip.getSelectedInstallation());
                Installation i = Oolite.populateFromHomeDir(homeDir);

                log.info("offering for edit {}", i);
                InstallationForm installationForm = new InstallationForm();
                installationForm.setData(i);
                if (JOptionPane.showOptionDialog(InstallationsPanel.this, installationForm, "Add Oolite version...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
                    log.info("adding installation...");
                    int rowIndex = model.addRow(installationForm.getData());
                    rowIndex = jTable1.convertRowIndexToView(rowIndex);
                    jTable1.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
                    setConfigDirty(true);
                }
            } catch (Exception e) {
                log.error("Could not act after ok", e);
            }
        });
        
        ipd.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                worker.cancel(true);
            }

        });
        worker.execute();
        ipd.setVisible(true); // this one blocks until the dialog gets hidden
        
    }//GEN-LAST:event_btScanActionPerformed

    private void btAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddActionPerformed
        log.debug("btAddActionPerformed({})", evt);
        
        try {
            InstallationForm installationForm = new InstallationForm();
            JOptionPane pane = new JOptionPane(installationForm, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
            JDialog dialog = pane.createDialog(this, "Add Oolite version...");
            dialog.setResizable(true);
            
            dialog.setVisible(true);
            if (((Integer)pane.getValue()).intValue() == JOptionPane.OK_OPTION) {
                int rowIndex = model.addRow(installationForm.getData());
                rowIndex = jTable1.convertRowIndexToView(rowIndex);
                jTable1.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
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
                JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_SELECT_ROW);
                return;
            }
            
            rowIndex = jTable1.convertRowIndexToModel(rowIndex);
            Installation i = model.getRow(rowIndex);
            
            InstallationForm installationForm = new InstallationForm();
            installationForm.setData(i);
            
            JOptionPane pane = new JOptionPane(installationForm, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);
            JDialog dialog = pane.createDialog(this, "Edit Oolite version...");
            dialog.setResizable(true);
            
            dialog.setVisible(true);
            if (((Integer)pane.getValue()).intValue() == JOptionPane.OK_OPTION) {
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
        
        if (JOptionPane.showConfirmDialog(btRemove, "Kiddo! This will remove all our knowledge about this installation on disk.\nAre you sure you want to do this?", "Remove...", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                int rowIndex = jTable1.getSelectedRow();
                if (rowIndex == -1) {
                    JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_SELECT_ROW);
                    return;
                }

                rowIndex = jTable1.convertRowIndexToModel(rowIndex);
                model.removeRow(rowIndex);

                setConfigDirty(true);
            } catch (Exception e) {
                log.error(INSTALLATIONSPANEL_ERROR, e);
                JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_ERROR);
            }        
        }
    }//GEN-LAST:event_btRemoveActionPerformed

    private void btSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSaveActionPerformed
        log.debug("btSaveActionPerformed({})", evt);
        
        try {
            File f = configuration.getDefaultConfigFile();
            configuration.saveConfiguration(f);
            
            StringBuilder sb = new StringBuilder("<html>");
            if (configuration.getActiveInstallation() == null) {
                sb.append("<p>Nice try, kiddo!</p><p>Your configuration was stored in ").append(f.getAbsolutePath()).append(".</p>");
                sb.append("<p>But... you still ain't got any Oolite version selected. Work on that, or I can't let you go.</p>");
            } else {
                switch(random.nextInt(3)) {
                    case 0:
                        sb.append("<p>Smart move, kiddo!</p><p>Your configuration was stored in ").append(f.getAbsolutePath()).append(".</p>");
                        sb.append("<p>Next time we won't have to fasten these screws again.</p>");
                        break;
                    case 1:
                        sb.append("<p>Your configuration has been stashed away in ").append(f.getAbsolutePath()).append(".</p>");
                        sb.append("<p>When you are ready to get out of here, kick off by pushing the &quot;Start Game&quot; button up top! But don't forget to visit the F8/8 marketplace first.</p>");
                        break;
                    default:
                        sb.append("<p>All right there!\n" +
                                  "We've stored your configuration in ").append(f.getAbsolutePath()).append(".</p>");
                        sb.append("<p>If you are all finished, you can get yourself out of here and get your ship ready to undock.</p>");
                        break;
                }
            }
            sb.append("</html>");
            MrGimlet.showMessage(this, sb.toString());
            
            setConfigDirty(false);
        } catch (Exception e) {
            log.error(INSTALLATIONSPANEL_COULD_NOT_SAVE, e);
            JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_COULD_NOT_SAVE);
        }        
    }//GEN-LAST:event_btSaveActionPerformed

    /**
     * Activates the selected installation.
     * 
     * @param evt 
     */
    private void btActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btActivateActionPerformed
        log.debug("btActivateActionPerformed({})", evt);
        
        try {
            int rowIndex = jTable1.getSelectedRow();
            if (rowIndex == -1) {
                JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_SELECT_ROW);
                return;
            }
            
            activateRow(rowIndex);
        } catch (Exception e) {
            log.error(INSTALLATIONSPANEL_COULD_NOT_ACTIVATE, e);
            JOptionPane.showMessageDialog(this, INSTALLATIONSPANEL_COULD_NOT_ACTIVATE);
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
