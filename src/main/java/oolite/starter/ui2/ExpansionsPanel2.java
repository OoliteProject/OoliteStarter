/*
 */
package oolite.starter.ui2;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import oolite.starter.Oolite;
import oolite.starter.generic.ListAction;
import oolite.starter.generic.SortedListModel;
import oolite.starter.model.Expansion;
import oolite.starter.model.Installation;
import oolite.starter.model.ProcessData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This panel allows users to move expansions between two lists.
 * 
 * @author hiran
 */
public class ExpansionsPanel2 extends javax.swing.JPanel implements Oolite.OoliteListener {
    private static final Logger log = LogManager.getLogger();
    
    public static interface SelectionListener {
        
        /**
         * Will be invoked when the selection changed.
         * 
         * @param selection the newly selected expansion
         */
        public void selectionChanged(Expansion selection);
        
    }

    private transient Oolite ooliteDriver;
    private Action installAction;
    private Action removeAction;
    private Action downloadAction;
    private Action deleteAction;
    private ListAction installListAction;
    private ListAction removeListAction;
    private ExpansionListModel elmAvailable;
    private ExpansionListModel elmInstalled;
    private SortedListModel<Expansion> elmSortedAvailable;
    private SortedListModel<Expansion> elmSortedInstalled;
    
    private List<SelectionListener> listeners = new ArrayList<>();
    
    /**
     * Creates new form ExpansionsPanel2.
     */
    public ExpansionsPanel2() {
        log.debug("ExpansionsPanel2()");
        
        installAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.debug("installAction actionPerformed()");
                
                int rowIndex = jList1.getSelectedIndex();
                
                if (rowIndex >= 0) {
                    Expansion e = jList1.getModel().getElementAt(rowIndex);

                    SwingWorker sw = new InstallSwingWorker(ExpansionsPanel2.this, e, elmAvailable, elmInstalled);
                    sw.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent pce) {
                            log.debug("propertyChange({})", pce);

                            boolean started = pce.getNewValue() == SwingWorker.StateValue.STARTED;
                            log.trace("pending: {}", started);

                            installAction.setEnabled(!started);
                            removeAction.setEnabled(!started);
                            downloadAction.setEnabled(!started);
                            deleteAction.setEnabled(!started);
                            jProgressBar1.setVisible(started);
                        }
                    });
                    sw.execute();
                }
            }
        };
        
        removeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.debug("removeAction actionPerformed()");
                
                int rowIndex = jList2.getSelectedIndex();
                
                if (rowIndex >= 0) {
                    Expansion e = jList2.getModel().getElementAt(rowIndex);

                    SwingWorker sw = new RemoveSwingWorker(ExpansionsPanel2.this, e, elmInstalled, elmAvailable);
                    sw.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent pce) {
                            log.debug("propertyChange({})", pce);

                            boolean started = pce.getNewValue() == SwingWorker.StateValue.STARTED;
                            log.trace("pending: {}", started);

                            installAction.setEnabled(!started);
                            removeAction.setEnabled(!started);
                            downloadAction.setEnabled(!started);
                            deleteAction.setEnabled(!started);
                            jProgressBar1.setVisible(started);
                        }
                    });
                    sw.execute();
                }
            }
        };

        downloadAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.debug("downloadAction actionPerformed()");
                JOptionPane.showMessageDialog(
                        ExpansionsPanel2.this, 
                        "Use your browser to download some OXP, then drop it into " + ooliteDriver.getActiveInstallation().getAddonDir(), 
                        "Direct Download", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        };
        
        deleteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.debug("deleteAction actionPerformed()");

                int rowIndex = jList2.getSelectedIndex();
                
                if (rowIndex >= 0) {
                    Expansion e = jList2.getModel().getElementAt(rowIndex);

                    SwingWorker sw = new DeleteSwingWorker(ExpansionsPanel2.this, e, elmInstalled);
                    sw.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent pce) {
                            log.debug("propertyChange({})", pce);

                            boolean started = pce.getNewValue() == SwingWorker.StateValue.STARTED;
                            log.trace("pending: {}", started);

                            installAction.setEnabled(!started);
                            removeAction.setEnabled(!started);
                            downloadAction.setEnabled(!started);
                            deleteAction.setEnabled(!started);
                            jProgressBar1.setVisible(started);
                        }
                    });
                    sw.execute();
                }
            }
        };

        initComponents();
        jProgressBar1.setVisible(false);
        
        ExpansionCellRenderer ecr = new ExpansionCellRenderer();
        jList1.setCellRenderer(ecr);
        jList2.setCellRenderer(ecr);
                
//        btDownload.addActionListener((ae) -> downloadAction.actionPerformed(ae));
//        btInstall.addActionListener((ae) -> installAction.actionPerformed(ae));
//        btRemove.addActionListener((ae) -> removeAction.actionPerformed(ae));
//        btDelete.addActionListener((ae) -> deleteAction.actionPerformed(ae));
        
        installListAction = new ListAction(jList1, installAction);
        removeListAction = new ListAction(jList2, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int rowIndex = jList2.getSelectedIndex();
                Expansion e = jList2.getModel().getElementAt(rowIndex);
                
                if (e.isManaged()) {
                    removeAction.actionPerformed(ae);
                } else {
                    deleteAction.actionPerformed(ae);
                }
            }
        });
        
        ListSelectionListener lsl = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {
                    log.debug("valueChanged({})", lse);
                    
                    JList<Expansion> list = (JList<Expansion>)lse.getSource();
                    log.trace("JList: {}", list);
                    int rowIndex = list.getSelectedIndex();
                    log.trace("rowIndex: {}", rowIndex);
                    
                    if (rowIndex >= 0) {
                        Expansion e = list.getModel().getElementAt(rowIndex);

                        installAction.setEnabled(!e.isLocal());
                        removeAction.setEnabled(e.isLocal() && e.isManaged());
                        deleteAction.setEnabled(
                                e.isLocal() && !e.isManaged() && !"Oolite Debug OXP".equals(e.getTitle())
                                );

                        fireSelectionEvent(e);
                    }
                }
                
            }
        };
                
        jList1.addListSelectionListener(lsl);
        jList2.addListSelectionListener(lsl);
    }

    /**
     * Sets the Oolite instance to run the savegames from.
     * 
     * @param oolite the ooliteDriver instance
     * @throws IOException something went wrong
     * @throws SAXException something went wrong
     * @throws ParserConfigurationException something went wrong
     * @throws XPathExpressionException  something went wrong
     */
    public void setOolite(Oolite oolite) {
        if (this.ooliteDriver != null) {
            this.ooliteDriver.removeOoliteListener(this);
        }
        this.ooliteDriver = oolite;
        update();
        oolite.addOoliteListener(this);
    }
    
    private void update() {
        try {
            List<Expansion> expansions = ooliteDriver.getAllExpansions();
            elmAvailable = new ExpansionListModel(expansions, e -> !e.isEnabled() );
            elmSortedAvailable = new SortedListModel<>(elmAvailable, SortOrder.ASCENDING, new Comparator<Expansion>() {
                @Override
                public int compare(Expansion t1, Expansion t2) {
                    return t1.getTitle().compareTo(t2.getTitle());
                }
            });
            jList1.setModel(elmSortedAvailable);

            elmInstalled = new ExpansionListModel(expansions, e -> e.isEnabled());
            elmSortedInstalled = new SortedListModel<>(elmInstalled, SortOrder.ASCENDING, new Comparator<Expansion>() {
                @Override
                public int compare(Expansion t1, Expansion t2) {
                    return t1.getTitle().compareTo(t2.getTitle());
                }
            });
            jList2.setModel(elmSortedInstalled);
            
        } catch (Exception e) {
            log.error("Could not update", e);
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
        jList1 = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>();
        btInstall = new javax.swing.JButton();
        btRemove = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btDownload = new javax.swing.JButton();
        btDelete = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();

        jList1.setName("AvailableJList"); // NOI18N
        jScrollPane1.setViewportView(jList1);

        jList2.setName("InstalledJList"); // NOI18N
        jScrollPane2.setViewportView(jList2);

        btInstall.setAction(installAction);
        btInstall.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/line_end_arrow_FILL0_wght400_GRAD0_opsz48.png"))); // NOI18N
        btInstall.setToolTipText("Install");

        btRemove.setAction(removeAction);
        btRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/line_start_arrow_FILL0_wght400_GRAD0_opsz48.png"))); // NOI18N
        btRemove.setToolTipText("Remove");

        jLabel1.setText("Available");

        jLabel2.setText("Installed");

        btDownload.setAction(downloadAction);
        btDownload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/download_FILL0_wght400_GRAD0_opsz48.png"))); // NOI18N
        btDownload.setToolTipText("Download...");

        btDelete.setAction(deleteAction);
        btDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/delete_forever_FILL0_wght400_GRAD0_opsz48.png"))); // NOI18N
        btDelete.setToolTipText("Delete");

        jProgressBar1.setIndeterminate(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btInstall)
                                    .addComponent(btRemove)
                                    .addComponent(btDownload)
                                    .addComponent(btDelete))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(0, 104, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addContainerGap())))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addComponent(btDownload)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btDelete)
                        .addGap(34, 34, 34)
                        .addComponent(btInstall)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btRemove)
                        .addGap(0, 68, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void launched(ProcessData pd) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void terminated() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void activatedInstallation(Installation installation) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    /**
     * Registers a new SelectionListener to this component.
     * 
     * @param listener the listener to add
     */
    public void addSelectionListener(SelectionListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Unregisters a SelectionListener from this component.
     * 
     * @param listener the listener to be removed
     */
    public void removeSelectionListener(SelectionListener listener) {
        listeners.remove(listener);
    }
    
    protected void fireSelectionEvent(Expansion e) {
        for (SelectionListener l: listeners) {
            l.selectionChanged(e);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btDelete;
    private javax.swing.JButton btDownload;
    private javax.swing.JButton btInstall;
    private javax.swing.JButton btRemove;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList<Expansion> jList1;
    private javax.swing.JList<Expansion> jList2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
