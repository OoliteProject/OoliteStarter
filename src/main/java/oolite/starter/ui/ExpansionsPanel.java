/*
 */
package oolite.starter.ui;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import oolite.starter.Oolite;
import oolite.starter.model.Expansion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ExpansionsPanel extends javax.swing.JPanel implements Oolite.OoliteListener {
    private static final Logger log = LogManager.getLogger();
    
    class MyRowStatusFilter extends RowFilter<ExpansionsTableModel, Integer> {
        
        private String filterMode;
        
        public MyRowStatusFilter(String filterMode, String filterText) {
            this.filterMode = filterMode;
        }

        @Override
        public boolean include(Entry<? extends ExpansionsTableModel, ? extends Integer> entry) {
            log.debug("include({})", entry);
            
            ExpansionsTableModel etm = entry.getModel();
            Expansion expansion = model.getRow(entry.getIdentifier());
            
            /*
updatable
            */
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
                default: // all
                    return true;
            }
        }
    }
    
    private Oolite oolite;
    private ExpansionsTableModel model;
    private TableRowSorter<ExpansionsTableModel> trw;
    private ExpansionPanel ep;

    /**
     * Creates new form ExpansionsPanel.
     */
    public ExpansionsPanel() {
        initComponents();
        setName("Expansions");
        
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                log.debug("valueChanged({})", lse);
                if (!lse.getValueIsAdjusting()) {
                    // we have a final value - let's render it
                    showDetailsOfSelection();
                }
            }
        });
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
        ep.setVisible(false);
        add(ep, BorderLayout.SOUTH);
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
    }
    
    private void showDetailsOfSelection() {
        int rowIndex = jTable1.getSelectedRow();
        if (rowIndex >=0) {
            rowIndex = jTable1.convertRowIndexToModel(rowIndex);
            Expansion row = model.getRow(rowIndex);
            ep.setData(row);
            ep.setVisible(true);
        } else {
            ep.setVisible(false);
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
    public void setOolite(Oolite oolite) throws MalformedURLException {
        if (this.oolite != null) {
            this.oolite.removeOoliteListener(this);
        }
        this.oolite = oolite;
        update();
        oolite.addOoliteListener(this);
    }
    
    private void update() {
        try {
            List<Expansion> expansions = oolite.getAllExpansions();

            model = new ExpansionsTableModel(expansions);
            jTable1.setRowSorter(null);
            jTable1.setModel(model);
            
            trw = new TableRowSorter<ExpansionsTableModel>(model);
            jTable1.setRowSorter(trw);
            applyFilter();
            
            showDetailsOfSelection();
        } catch (Exception e) {
            log.warn("Could not update", e);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbFilterMode = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        txtFilterText = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        btActivate = new javax.swing.JButton();
        btExport = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter"));

        jLabel1.setText("Status");

        cbFilterMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "all", "installed", "updatable", "not installed", "enabled", "disabled", "not online" }));
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
                .addComponent(txtFilterText, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
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

        btActivate.setText("Activate...");
        btActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btActivateActionPerformed(evt);
            }
        });

        btExport.setText("Export...");
        btExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btExportActionPerformed(evt);
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btActivate)
                    .addComponent(btExport)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void cbFilterModeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbFilterModeItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            log.debug("cbFilterModeItemStateChanged({})", evt);
            applyFilter();
        }
    }//GEN-LAST:event_cbFilterModeItemStateChanged

    private void btActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btActivateActionPerformed
        try {
            JFileChooser jfc = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("Oolite Expansion Set (*.oolite-es)", "oolite-es");
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.addChoosableFileFilter(filter);
            jfc.setFileFilter(filter);
            if (jfc.showDialog(this, "Activate") == JFileChooser.APPROVE_OPTION) {
                oolite.setEnabledExpansions(jfc.getSelectedFile());
            }
        } catch (Exception e) {
            log.error("Could not activate", e);
            JOptionPane.showMessageDialog(this, "Could not activate.\n" + e.getClass().getName() + ": " + e.getMessage());
        } finally {
            update();
        }
    }//GEN-LAST:event_btActivateActionPerformed

    private void btExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btExportActionPerformed
        try {
            JFileChooser jfc = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("Oolite Expansion Set (*.oolite-es)", "oolite-es");
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.addChoosableFileFilter(filter);
            jfc.setFileFilter(filter);
            if (jfc.showDialog(this, "Export") == JFileChooser.APPROVE_OPTION) {
                
                File f = jfc.getSelectedFile();
                
                // Java does not automatically add the extension
                if (jfc.getFileFilter() instanceof FileNameExtensionFilter fnef) {
                    if (!fnef.accept(f)) {
                        // attach extension
                        f = new File(f.getAbsolutePath() + "." + fnef.getExtensions()[0]);
                    }
                }
                
                if (f.exists()) {
                    if (JOptionPane.showConfirmDialog(this, String.format("File %s exists. Do you want to overwrite?", f.getAbsolutePath())) != JOptionPane.OK_OPTION) {
                        return;
                    }
                }
                
                oolite.exportEnabledExpansions(f);
            }
        } catch (Exception e) {
            log.error("Could not export", e);
            JOptionPane.showMessageDialog(this, "Could not export.");
        }
    }//GEN-LAST:event_btExportActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btActivate;
    private javax.swing.JButton btExport;
    private javax.swing.JComboBox<String> cbFilterMode;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField txtFilterText;
    // End of variables declaration//GEN-END:variables

    @Override
    public void launched() {
    }

    @Override
    public void terminated() {
        update();
    }
}
