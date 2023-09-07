/*
 */
package oolite.starter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import oolite.starter.Configuration;
import oolite.starter.ExpansionManager;
import oolite.starter.Oolite;
import oolite.starter.model.Command;
import oolite.starter.model.Expansion;
import oolite.starter.model.ExpansionReference;
import oolite.starter.model.Installation;
import oolite.starter.model.ProcessData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.NodeList;

/**
 *
 * @author hiran
 */
public class ExpansionsPanel extends javax.swing.JPanel implements Oolite.OoliteListener, ExpansionManager.ExpansionManagerListener {
    private static final Logger log = LogManager.getLogger();
    
    private static final String EXPANSIONSPANEL_COULD_NOT_RELOAD = "Could not reload";
    
    private ExpansionManagerPanel emp;
    private JDialog emd;

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
                setPopupMenu();
            }
        });
        jTable1.setDefaultRenderer(Object.class, new AnnotationRenderer(jTable1.getDefaultRenderer(Object.class), Configuration.COLOR_ATTENTION));

        DeferredDocumentChangeListener deferredListener = new DeferredDocumentChangeListener(300);
        deferredListener.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                applyFilter();
            }
        });
        
        lbFilterText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbFilterText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    //Desktop.getDesktop().browse(new URI("https://en.wikipedia.org/wiki/Regular_expression"));
                    //Desktop.getDesktop().browse(new URI("https://www.regular-expressions.info/quickstart.html"));
                    Desktop.getDesktop().browse(new URI("https://www.regular-expressions.info/tutorial.html"));
                } catch (Exception ex) {
                    log.info("Could not browse", ex);
                }
            }
        });
        txtFilterText.getDocument().addDocumentListener(deferredListener);

        ep = new ExpansionPanel();
        jSplitPane1.setRightComponent(ep);
        
        setPopupMenu();
        
        txtEMStatus.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (emp == null) {
                    emp = new ExpansionManagerPanel();
                    emp.setData(ExpansionManager.getInstance());
                    
                    if (emd != null) {
                        emd.dispose();
                        emd = null;
                    }
                    
                }
                
                if (emd == null) {
                    emd = new JDialog(SwingUtilities.windowForComponent(ExpansionsPanel.this));
                    emd.getContentPane().add(emp);
                    emd.pack();
                    emd.setLocationRelativeTo(SwingUtilities.windowForComponent(ExpansionsPanel.this));
                }
                
                emd.setVisible(true);
                emd.requestFocus();
            }

        });
    }
    
    private void applyFilter() {
        log.debug("applyFilter()");
        if (trw != null) {
            List<RowFilter<ExpansionsTableModel, Integer>> filters = new ArrayList<>();
            filters.add(new MyRowStatusFilter(String.valueOf(cbFilterMode.getSelectedItem()), txtFilterText.getText()));
            if (!"".equals(txtFilterText.getText())) {
                try {
                    String re = "(?i)" + txtFilterText.getText();
                    log.trace("re={}", re);
                    filters.add(RowFilter.regexFilter(re));
                } catch (Exception e) {
                    log.info("Cannot apply regexp filter", e);
                }
            }
            trw.setRowFilter(RowFilter.andFilter(filters));
            
        }
        jTable1.repaint();
        
        updateBadges();
    }
    
    /**
     * Sets the details panel data to the selected row.
     * Usually call this when the selected row changed.
     */
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
     * Sets the JTable popup menu.
     * Usually call this when the selected row changed.
     */
    private void setPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        int rowIndex = jTable1.getSelectedRow();
        if (rowIndex >=0) {
            rowIndex = jTable1.convertRowIndexToModel(rowIndex);
            final Expansion row = model.getRow(rowIndex);
            
            if (row.isOnline()) {
                popupMenu.add(new AbstractAction("Copy Download URL") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        String s = row.getDownloadUrl();

                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        if (s != null) {
                            StringSelection stringSelection = new StringSelection(s);
                            clipboard.setContents(stringSelection, null);
                            log.info("Download URL '{}' copied to clipboard", s);
                            
                            MrGimlet.showMessage(SwingUtilities.getRootPane(jTable1), "In your pocket!");
                        } else {
                            clipboard.setContents(new StringSelection(""), null);
                            MrGimlet.showMessage(jTable1, "There is no URL to copy, son.");
                        }
                    }
                });
            }
            if (!row.isLocal()) {
                popupMenu.add(new AbstractAction("Install") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        
                        Command command = new Command(Command.Action.install, row);
                        ExpansionManager.getInstance().addCommand(command);
                        
//                        // todo: Run in background thread
//                        try {
//                            row.install();
//                            MrGimlet.showMessage(btExport, "Installation triggered.");
//                            setPopupMenu();
//                        } catch (Exception e) {
//                            log.error("Could not install {}", row, e);
//                            MrGimlet.showMessage(btExport, "Could not install. Check logfile.", 0);
//                        }
                    }
                });
            } else {
                popupMenu.add(new AbstractAction("Show in FileSystem") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        try {
                            Desktop.getDesktop().browseFileDirectory(row.getLocalFile());
                            return;
                        } catch (UnsupportedOperationException e) {
                            log.warn("Could not open file", e);
                        }
                        try {
                            Desktop.getDesktop().open(row.getLocalFile());
                            return;
                        } catch (Exception e) {
                            log.warn("Could not open file", e);
                        }
                    }
                });
                if (!row.isNested()) {
                    if (row.isEnabled()) {
                        popupMenu.add(new AbstractAction("Disable") {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                // todo: Run in background thread
                                
                                Command command = new Command(Command.Action.disable, row);
                                ExpansionManager.getInstance().addCommand(command);
                        
//                                // todo: notify user about result
//                                try {
//                                    row.disable();
//                                    MrGimlet.showMessage(btExport, "Expansion disabled.");
//                                    setPopupMenu();
//                                } catch (Exception e) {
//                                    log.error("Could not disable {}", row, e);
//                                    MrGimlet.showMessage(btExport, "Could not disable. Check logfile.", 0);
//                                }
                            }
                        });
                    } else {
                        popupMenu.add(new AbstractAction("Enable") {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                
                                Command command = new Command(Command.Action.enable, row);
                                ExpansionManager.getInstance().addCommand(command);
                        
//                                try {
//                                    row.enable();
//                                    MrGimlet.showMessage(btExport, "Expansion enabled.");
//                                    setPopupMenu();
//                                } catch (Exception e) {
//                                    log.error("Could not enable {}", row, e);
//                                    MrGimlet.showMessage(btExport, "Could not enable. Check logfile.", 0);
//                                }
                            }
                        });
                    }
                }

                if (popupMenu.getComponentCount()>0) {
                    popupMenu.add(new JSeparator());
                }
                popupMenu.add(new AbstractAction("Delete") {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                                
                        Command command = new Command(Command.Action.delete, row);
                        ExpansionManager.getInstance().addCommand(command);
                        
//                        try {
//                            row.remove();
//                            MrGimlet.showMessage(btExport, "Expansion removed.");
//                            setPopupMenu();
//                        } catch (Exception e) {
//                            log.error("Could not remove {}", row, e);
//                            MrGimlet.showMessage(btExport, "Could not remove. Check logfile.", 0);
//                        }
                    }
                });
            }
        }
        jTable1.setComponentPopupMenu(popupMenu);
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
            jTable1.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                
                private DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value instanceof TemporalAccessor ld) {
                        value = dtf.format(ld);
                    }
                    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
                }
                
            });
            Util.setColumnWidths(jTable1);

            if (trw == null) {
                trw = new TableRowSorter<>(model);
                List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
                trw.setSortKeys(sortKeys);
            }
            jTable1.setRowSorter(trw);
            applyFilter();
            updateBadges();
        } catch (Exception e) {
            log.warn("Could not update", e);
        }
    }

    @Override
    public void activatedInstallation(Installation installation) {
        log.debug("activatedInstallation({})", installation);
        
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
        java.awt.GridBagConstraints gridBagConstraints;

        jpToolbar = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cbFilterMode = new javax.swing.JComboBox<>();
        lbFilterText = new javax.swing.JLabel();
        txtFilterText = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        btActivate = new javax.swing.JButton();
        btExport = new javax.swing.JButton();
        btValidate = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        btReload = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        pnStatus = new javax.swing.JPanel();
        txtEMStatus = new javax.swing.JLabel();

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

        lbFilterText.setText("<html>and contains <a href=\"https://en.wikipedia.org/wiki/Regular_expression\">RE</a></html>");

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
                .addComponent(lbFilterText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(lbFilterText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addGap(0, 0, Short.MAX_VALUE))
        );

        add(jpToolbar, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setDividerSize(7);
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

        jPanel1.setLayout(new java.awt.GridBagLayout());

        pnStatus.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(pnStatus, gridBagConstraints);

        txtEMStatus.setText("Ready");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel1.add(txtEMStatus, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
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
                log.warn("activating {}", jfc.getSelectedFile());

                // todo: create a plan and show it
                NodeList nl = oolite.parseExpansionSet(jfc.getSelectedFile());
                List<Command> commands = oolite.buildCommandList(expansions, nl);
                ExpansionManager.getInstance().addCommands(commands);

                // if approved, inject it for execution
//                update();
//                new ActivationWorker(oolite, expansions, jfc.getSelectedFile(), this).execute();
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel jpToolbar;
    private javax.swing.JLabel lbFilterText;
    private javax.swing.JPanel pnStatus;
    private javax.swing.JLabel txtEMStatus;
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

    @Override
    public void updateStatus(ExpansionManager.Status status, Queue<Command> queue) {
        log.warn("updateStatus(...)");
        String s = String.valueOf(status) + " (" + String.valueOf(queue.size()) + ")";
        txtEMStatus.setText(s);
    }
    
}
