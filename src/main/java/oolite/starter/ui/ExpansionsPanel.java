/*
 */
package oolite.starter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import oolite.starter.Configuration;
import oolite.starter.Oolite;
import oolite.starter.model.Expansion;
import oolite.starter.model.ExpansionReference;
import oolite.starter.model.ProcessData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ExpansionsPanel extends javax.swing.JPanel implements Oolite.OoliteListener {
    private static final Logger log = LogManager.getLogger();
    
    private static final String EXPANSIONSPANEL_COULD_NOT_RELOAD = "Could not reload";

    class MyRowStatusFilter extends RowFilter<ExpansionsTableModel, Integer> {
        
        private String filterMode;
        
        public MyRowStatusFilter(String filterMode, String filterText) {
            log.debug("MyRowStatusFilter({}, {})", filterMode, filterText);
            this.filterMode = filterMode;
        }

        @Override
        public boolean include(Entry<? extends ExpansionsTableModel, ? extends Integer> entry) {
            log.debug("include({})", entry);
            
            Expansion expansion = model.getRow(entry.getIdentifier());
            
            switch(filterMode) {
                case "installed":
                    return expansion.isLocal();
                case "not installed":
                    return !expansion.isLocal();
                case "enabled":
                    return expansion.isEnabled();
                case "disabled":
                    return expansion.isLocal() && !expansion.isEnabled();
                case "not online":
                    return !expansion.isOnline();
                case "updatable":
                    return model.getSiblingCount(expansion)>1;
                case "problematic":
                    return expansion.isEnabled() && (expansion.getEMStatus().isConflicting() || expansion.getEMStatus().isMissingDeps());
                default: // all
                    return true;
            }
        }
    }
    
    private transient Oolite oolite;
    private ExpansionsTableModel model;
    private transient TableRowSorter<ExpansionsTableModel> trw;
    private transient List<Expansion> expansions;

    private ExpansionPanel ep;

    /**
     * Creates new form ExpansionsPanel.
     */
    public ExpansionsPanel() {
        initComponents();
        
        jTable1.getSelectionModel().addListSelectionListener(lse -> {
            log.debug("valueChanged({})", lse);
            if (!lse.getValueIsAdjusting()) {
                // we have a final value - let's render it
                showDetailsOfSelection();
            }
        });
        jTable1.setDefaultRenderer(Object.class, new AnnotationRenderer(jTable1.getDefaultRenderer(Object.class), Configuration.COLOR_ATTENTION));
        txtFilterText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                applyFilter();
            }
        });
        
        ep = new ExpansionPanel();
        jSplitPane1.setRightComponent(ep);
    }
    
    private void applyFilter() {
        log.debug("applyFilter");
        if (trw != null) {
            List<RowFilter<ExpansionsTableModel, Integer>> filters = new ArrayList<>();
            filters.add(new MyRowStatusFilter(String.valueOf(cbFilterMode.getSelectedItem()), txtFilterText.getText()));
            if (!"".equals(txtFilterText.getText())) {
                try {
                    filters.add(RowFilter.regexFilter(txtFilterText.getText()));
                } catch (Exception e) {
                    log.info("Cannot apply regexp filter", e);
                }
            }
            trw.setRowFilter(RowFilter.andFilter(filters));
            
        }
        jTable1.repaint();
        
        updateBadges();
    }
    
    private void showDetailsOfSelection() {
        int rowIndex = jTable1.getSelectedRow();
        if (rowIndex >=0) {
            rowIndex = jTable1.convertRowIndexToModel(rowIndex);
            Expansion row = model.getRow(rowIndex);
            ep.setData(row);
        } else {
            ep.setData(null);
        }
    }
    
    /**
     * Sets the Oolite instance to run the savegames from.
     * 
     * @param oolite the oolite instance
     * @throws IOException something went wrong
     * @throws SAXException something went wrong
     * @throws ParserConfigurationException something went wrong
     * @throws XPathExpressionException  something went wrong
     */
    public void setOolite(Oolite oolite) {
        if (this.oolite != null) {
            this.oolite.removeOoliteListener(this);
        }
        this.oolite = oolite;
        update();
        oolite.addOoliteListener(this);
    }
    
    void updateBadges() {
        log.debug("updateBadges()");
        pnStatus.removeAll();

        pnStatus.add(new Badge("Expansions", String.valueOf(model.getRowCount()), Color.BLACK));
        if (model.getRowCount() > trw.getViewRowCount()) {
            pnStatus.add(new Badge("Filtered", String.valueOf(trw.getViewRowCount()), Color.BLACK));
        }

        int y = model.getNumberOfExpansionsMissingDeps();
        if (y>0) {
            pnStatus.add(new Badge("MissingDeps", String.valueOf(y), Configuration.COLOR_ATTENTION));
        }

        int x = model.getNumberOfExpansionsConflicting();
        if (x > 0) {
            pnStatus.add(new Badge("Conflicts", String.valueOf(x), Configuration.COLOR_ATTENTION));
        }
        
        x = model.getNumberOfExpansionsIncompatible();
        if (x > 0) {
            pnStatus.add(new Badge("Incompatible with selected Oolite version", String.valueOf(x), Color.black));
        }
        
        pnStatus.validate();
        pnStatus.repaint();
    }
    
    /**
     * Updates the expansionspanel display.
     */
    public void update() {
        log.debug("update()");
        try {
            expansions = oolite.getAllExpansions();

            model = new ExpansionsTableModel(expansions);
            model.addTableModelListener(tme -> {
                updateBadges();
            });
            
            jTable1.setRowSorter(null);
            jTable1.setModel(model);
            
            trw = new TableRowSorter<>(model);
            jTable1.setRowSorter(trw);
            applyFilter();
            updateBadges();
        } catch (Exception e) {
            log.warn("Could not update", e);
        }
    }

    @Override
    public void activatedInstallation() {
        log.error("activatedInstallation()");
        try {
            update();
        } catch (Exception e) {
            log.error(EXPANSIONSPANEL_COULD_NOT_RELOAD, e);
            JOptionPane.showMessageDialog(null, EXPANSIONSPANEL_COULD_NOT_RELOAD);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpToolbar = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbFilterMode = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtFilterText = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        btActivate = new javax.swing.JButton();
        btExport = new javax.swing.JButton();
        btValidate = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        btReload = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new MyJTable();
        pnStatus = new javax.swing.JPanel();

        setName("OXPs/OXZs"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter"));

        jLabel1.setText("Status");

        cbFilterMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "all", "installed", "updatable", "not installed", "enabled", "disabled", "not online", "problematic" }));
        cbFilterMode.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbFilterModeItemStateChanged(evt);
            }
        });

        jLabel2.setText("and contains RE");

        txtFilterText.setText(".*");
        txtFilterText.setMinimumSize(new java.awt.Dimension(300, 24));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbFilterMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFilterText, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbFilterMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(txtFilterText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Expansion Set"));

        btActivate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/upload_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btActivate.setText("Activate...");
        btActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btActivateActionPerformed(evt);
            }
        });

        btExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/download_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btExport.setText("Export...");
        btExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btExportActionPerformed(evt);
            }
        });

        btValidate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/checklist_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btValidate.setText("Validate");
        btValidate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btValidateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btActivate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btExport)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btValidate)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btActivate)
                    .addComponent(btExport)
                    .addComponent(btValidate))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Misc"));

        btReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/refresh_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btReload.setText("Reload");
        btReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btReloadActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btReload)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btReload)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jpToolbarLayout = new javax.swing.GroupLayout(jpToolbar);
        jpToolbar.setLayout(jpToolbarLayout);
        jpToolbarLayout.setHorizontalGroup(
            jpToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpToolbarLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 374, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jpToolbarLayout.setVerticalGroup(
            jpToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpToolbarLayout.createSequentialGroup()
                .addGroup(jpToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        add(jpToolbar, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setOneTouchExpandable(true);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"No expansions found"},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Expansions"
            }
        ));
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jTable1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pnStatus.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        add(pnStatus, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void cbFilterModeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbFilterModeItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            log.debug("cbFilterModeItemStateChanged({})", evt);
            applyFilter();
        }
    }//GEN-LAST:event_cbFilterModeItemStateChanged

    private void btActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btActivateActionPerformed
        log.debug("btActivateActionPerformed({})", evt);
        try {
            JFileChooser jfc = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("Oolite Expansion Set (*.oolite-es)", "oolite-es");
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.addChoosableFileFilter(filter);
            jfc.setFileFilter(filter);
            if (jfc.showDialog(this, "Activate") == JFileChooser.APPROVE_OPTION) {
                update();
                new ActivationWorker(oolite, expansions, jfc.getSelectedFile(), this).execute();
            }
        } catch (Exception e) {
            log.error("Could not trigger activate", e);
            JOptionPane.showMessageDialog(this, "Could not trigger activate.\n" + e.getClass().getName() + ": " + e.getMessage());
        }
    }//GEN-LAST:event_btActivateActionPerformed

    private void btExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btExportActionPerformed
        log.debug("btExportActionPerformed({})", evt);
        try {
            JFileChooser jfc = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("Oolite Expansion Set (*.oolite-es)", "oolite-es");
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.addChoosableFileFilter(filter);
            jfc.setFileFilter(filter);
            if (jfc.showDialog(this, "Export") == JFileChooser.APPROVE_OPTION) {
                
                File f = jfc.getSelectedFile();
                
                // Java does not automatically add the extension
                if (jfc.getFileFilter() instanceof FileNameExtensionFilter fnef
                        && !fnef.accept(f)
                ) {
                    // attach extension
                    f = new File(f.getAbsolutePath() + "." + fnef.getExtensions()[0]);
                }
                
                if (f.exists()
                    && JOptionPane.showConfirmDialog(this, String.format("File %s exists. Do you want to overwrite?", f.getAbsolutePath())) != JOptionPane.OK_OPTION
                ) {
                    return;
                }
                
                oolite.exportEnabledExpansions(f);
            }
        } catch (Exception e) {
            log.error("Could not export", e);
            JOptionPane.showMessageDialog(this, "Could not export.");
        }
    }//GEN-LAST:event_btExportActionPerformed

    private void btValidateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btValidateActionPerformed
        log.debug("btValidateActionPerformed({})", evt);
        try {
            List<Expansion> es = new ArrayList<>();
            for (int i= 0; i< trw.getViewRowCount(); i++) {
                es.add(model.getRow(jTable1.convertRowIndexToModel(i)));
            }
            List<ExpansionReference> warnings = oolite.validateDependencies(es);
            
            if (warnings.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All dependencies resolved.");
            } else {
                DefaultListModel<ExpansionReference> dlm = new DefaultListModel<>();
                dlm.addAll(warnings);
                JList<ExpansionReference> list = new JList<>(dlm);
                list.setCellRenderer(new ExpansionReferenceCellRenderer());
                list.addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int rowIndex = list.locationToIndex(e.getPoint());
                        if (dlm != null && rowIndex >= 0) {
                            ExpansionReference er = dlm.getElementAt(rowIndex);
                            if (er.getReasons().isEmpty()) {
                                list.setToolTipText(null);
                            } else {
                                list.setToolTipText(String.valueOf(er.getReasons()));
                            }
                        }
                    }
                });
                
                JScrollPane sp = new JScrollPane(list);
                
                JPanel content = new JPanel();
                content.setLayout(new BorderLayout());
                
                content.add(new JLabel("<html>Here is a list of missing dependencies and conflicts.<br/><br/></html>"), BorderLayout.NORTH);
                content.add(sp, BorderLayout.CENTER);
                
                Dimension d = new Dimension(900, 600);
                sp.setMaximumSize(d);
                sp.setPreferredSize(d);
                JOptionPane.showMessageDialog(this, content, "Validation Result", JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception e) {
            log.error("Could not validate", e);
            JOptionPane.showMessageDialog(this, "Could not validate.");
        }
    }//GEN-LAST:event_btValidateActionPerformed

    private void btReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btReloadActionPerformed
        log.debug("btReloadActionPerformed({})", evt);
        try {
            update();
        } catch (Exception e) {
            log.error(EXPANSIONSPANEL_COULD_NOT_RELOAD, e);
            JOptionPane.showMessageDialog(null, EXPANSIONSPANEL_COULD_NOT_RELOAD);
        }
    }//GEN-LAST:event_btReloadActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btActivate;
    private javax.swing.JButton btExport;
    private javax.swing.JButton btReload;
    private javax.swing.JButton btValidate;
    private javax.swing.JComboBox<String> cbFilterMode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel jpToolbar;
    private javax.swing.JPanel pnStatus;
    private javax.swing.JTextField txtFilterText;
    // End of variables declaration//GEN-END:variables

    @Override
    public void launched(ProcessData pd) {
        // we are not yet interested in this event
    }

    @Override
    public void terminated() {
        SwingUtilities.invokeLater(this::update);
    }
}
