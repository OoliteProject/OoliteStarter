/*
 */
package oolite.starter.ui2;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import oolite.starter.Oolite2;
import oolite.starter.generic.FilteredListModel;
import oolite.starter.generic.ListAction;
import oolite.starter.generic.SortedListModel;
import oolite.starter.model.Expansion;
import oolite.starter.model.Installation;
import oolite.starter.model.ProcessData;
import oolite.starter.util.FilterAndSearchUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This panel allows users to move expansions between two lists.
 * 
 * @author hiran
 */
public class ExpansionsPanel2 extends javax.swing.JPanel implements Oolite2.OoliteListener {
    private static final Logger log = LogManager.getLogger();

    public static interface SelectionListener {
        
        /**
         * Will be invoked when the selection changed.
         * 
         * @param selection the newly selected expansion
         */
        public void selectionChanged(Expansion selection);
        
    }

    private transient Oolite2 ooliteDriver;
    private transient Action installAction;
    private transient Action removeAction;
    private transient Action downloadAction;
    private transient Action deleteAction;
    
    /**
     * Field required to hold a reference to a homogenously triggererable action on list.
     */
    private transient ListAction<Expansion> installListAction;

    /**
     * Field required to hold a reference to a homogenously triggererable action on list.
     */
    private transient ListAction<Expansion> removeListAction;
    private Oolite2.OoliteExpansionListModel elmAvailable;
    private Oolite2.OoliteExpansionListModel elmInstalled;
    
    private transient SwingWorker sw;
    
    private transient List<SelectionListener> listeners = new ArrayList<>();
    
    private String availableSearchString = "";
    private FilterAndSearchUtil.FilterMode availableFilterMode = FilterAndSearchUtil.FilterMode.NONE;
    private FilterAndSearchUtil.SortMode availableSortMode = FilterAndSearchUtil.SortMode.BY_TITLE;
    private String installedSearchString = "";
    private FilterAndSearchUtil.FilterMode installedFilterMode = FilterAndSearchUtil.FilterMode.NONE;
    private FilterAndSearchUtil.SortMode installedSortMode = FilterAndSearchUtil.SortMode.BY_TITLE;
    
    private transient FilteredListModel.Filter<Expansion> enabledFilter =  new FilteredListModel.Filter<Expansion>() {
        private static final Logger log = LogManager.getLogger();
        
        @Override
        public boolean willShow(Expansion t) {
            log.debug("willShow({})", t);
            
            return t.isEnabled();
        }

        @Override
        public String toString() {
            return "Filter(enabled)";
        }

    };
    
    private transient FilteredListModel.Filter<Expansion> notEnabledFilter =  new FilteredListModel.Filter<Expansion>() {
        private static final Logger log = LogManager.getLogger();
        
        @Override
        public boolean willShow(Expansion t) {
            log.debug("willShow({})", t);

            return !t.isEnabled() && !t.getEMStatus().isIncompatible();
        }

        @Override
        public String toString() {
            return "Filter(!enabled)";
        }

    };
    
    /**
     * Creates new form ExpansionsPanel2.
     */
    public ExpansionsPanel2(Oolite2 oolite) {
        log.debug("ExpansionsPanel2()");
        
        installAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.debug("installAction actionPerformed()");
                
                if (sw != null && sw.getState() != SwingWorker.StateValue.DONE) {
                    return;
                }
                
                int rowIndex = jlAvailable.getSelectedIndex();
                
                if (rowIndex >= 0) {
                    Expansion e = jlAvailable.getModel().getElementAt(rowIndex);

                    sw = new InstallSwingWorker(ExpansionsPanel2.this, e, oolite);
                    sw.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent pce) {
                            log.debug("propertyChange({})", pce);
                            updateActions(e);
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

                if (sw != null && sw.getState() != SwingWorker.StateValue.DONE) {
                    return;
                }
                
                int rowIndex = jlInstalled.getSelectedIndex();
                
                if (rowIndex >= 0) {
                    Expansion e = jlInstalled.getModel().getElementAt(rowIndex);

                    sw = new RemoveSwingWorker(ExpansionsPanel2.this, e, oolite);
                    sw.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent pce) {
                            log.debug("propertyChange({})", pce);
                            updateActions(e);
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

                if (sw != null && sw.getState() != SwingWorker.StateValue.DONE) {
                    return;
                }

                String input = JOptionPane.showInputDialog(
                        ExpansionsPanel2.this, 
                        "Please enter download URL.\nYou can find download URLs at https://wiki.alioth.net/index.php/Guide_to_Unlisted_OXPs",
                        "Download OXP...",
                        JOptionPane.QUESTION_MESSAGE
                );
                log.info("Download URL: {}", input);
                Path addonDir = Paths.get(ooliteDriver.getActiveInstallation().getAddonDir());
                
                if (input != null) {
                    sw = new DownloadSwingWorker(ExpansionsPanel2.this, input, addonDir, oolite);
                    sw.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent pce) {
                            log.debug("propertyChange({})", pce);
                            updateActions(null);
                        }
                    });
                    sw.execute();
                }
                
            }
        };
        
        deleteAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.debug("deleteAction actionPerformed()");

                if (sw != null && sw.getState() != SwingWorker.StateValue.DONE) {
                    return;
                }
                
                int rowIndex = jlInstalled.getSelectedIndex();
                
                if (rowIndex >= 0) {
                    Expansion e = jlInstalled.getModel().getElementAt(rowIndex);

                    sw = new DeleteSwingWorker(ExpansionsPanel2.this, e, oolite);
                    sw.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent pce) {
                            log.debug("propertyChange({})", pce);
                            updateActions(e);
                        }
                    });
                    sw.execute();
                }
            }
        };

        initComponents();
        jProgressBar1.setVisible(false);
        
        ExpansionCellRenderer ecr = new ExpansionCellRenderer();
        jlAvailable.setCellRenderer(ecr);
        jlInstalled.setCellRenderer(ecr);
                
//        btDownload.addActionListener((ae) -> downloadAction.actionPerformed(ae));
//        btInstall.addActionListener((ae) -> installAction.actionPerformed(ae));
//        btRemove.addActionListener((ae) -> removeAction.actionPerformed(ae));
//        btDelete.addActionListener((ae) -> deleteAction.actionPerformed(ae));
        
        installListAction = new ListAction<>(jlAvailable, installAction);
        removeListAction = new ListAction<>(jlInstalled, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int rowIndex = jlInstalled.getSelectedIndex();
                Expansion e = jlInstalled.getModel().getElementAt(rowIndex);
                
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
                        
                        updateActions(e);
                        fireSelectionEvent(e);
                    }
                }
                
            }
        };
                
        jlAvailable.addListSelectionListener(lsl);
        jlInstalled.addListSelectionListener(lsl);
        
        FocusListener fl = new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
                log.debug("focusGained({})", fe);

                JList<Expansion> jlist = (JList<Expansion>)fe.getSource();
                Expansion e = jlist.getSelectedValue();

                updateActions(e);
                fireSelectionEvent(e);
            }

            @Override
            public void focusLost(FocusEvent fe) {
                log.debug("focusLost({})", fe);
            }
        };
        
        jlAvailable.addFocusListener(fl);
        jlInstalled.addFocusListener(fl);
        
        setOolite(oolite);
    }
    
    /**
     * Enables/disables the actions depending on what is going on.
     * @param e If something about a specific expansion is going on, this is the one
     */
    private void updateActions(Expansion e) {
        boolean working = sw != null && sw.getState() != SwingWorker.StateValue.DONE;
        jProgressBar1.setVisible(working);
        log.debug("working: {}", working);

        if (e != null) {
            if (working) {
                jProgressBar1.setString(sw.getClass().getName() + e.getTitle());
            }

            installAction.setEnabled(!e.isEnabled() && !working);
            removeAction.setEnabled(e.isEnabled() && e.isManaged() && !working);
            deleteAction.setEnabled(
                e.isEnabled() && !e.isManaged() && !"Oolite Debug OXP".equals(e.getTitle()) && !working
                );
        } else {
            if (sw != null) {
                jProgressBar1.setString(sw.getClass().getName());
            } else {
                jProgressBar1.setString("");
            }
        }
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
    public void setOolite(Oolite2 oolite) {
        if (this.ooliteDriver != null) {
            this.ooliteDriver.removeOoliteListener(this);
        }
        this.ooliteDriver = oolite;
        update();
        oolite.addOoliteListener(this);
    }
    
    private void update() {
        log.debug("update()");
        // todo: align this with the new situation
        try {
//            List<Expansion> expansions = ooliteDriver.getAllExpansions();
            //elmAvailable = new ExpansionListModel(expansions, e -> !e.isEnabled() );
            elmAvailable = ooliteDriver.getExpansionListModel();
            setModel(jlAvailable, elmAvailable, availableFilterMode, availableSearchString, availableSortMode, notEnabledFilter);

            //elmInstalled = new ExpansionListModel(expansions, e -> e.isEnabled());
            elmInstalled = ooliteDriver.getExpansionListModel();
            setModel(jlInstalled, elmInstalled, installedFilterMode, installedSearchString, installedSortMode, enabledFilter);
            
//            FilteredListModel<Expansion> lm = new FilteredListModel<Expansion>(elmInstalled, new FilteredListModel.Filter<Expansion>() {
//                @Override
//                public boolean willShow(Expansion t) {
//                    return t.isEnabled();
//                }
//            });
//            elmSortedInstalled = new SortedListModel<>(lm, SortOrder.ASCENDING, new Comparator<Expansion>() {
//                @Override
//                public int compare(Expansion t1, Expansion t2) {
//                    return t1.getTitle().compareTo(t2.getTitle());
//                }
//            });
//            jlInstalled.setModel(elmSortedInstalled);
            
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

        spAvailable = new javax.swing.JScrollPane();
        jlAvailable = new javax.swing.JList<>();
        spInstalled = new javax.swing.JScrollPane();
        jlInstalled = new javax.swing.JList<>();
        btInstall = new javax.swing.JButton();
        btRemove = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btDownload = new javax.swing.JButton();
        btDelete = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        btFlterAvailable = new javax.swing.JButton();
        btFilterInstalled = new javax.swing.JButton();

        setName("OXPs/OXZs"); // NOI18N

        spAvailable.setPreferredSize(new java.awt.Dimension(250, 130));

        jlAvailable.setName("AvailableJList"); // NOI18N
        spAvailable.setViewportView(jlAvailable);

        spInstalled.setPreferredSize(new java.awt.Dimension(250, 130));

        jlInstalled.setName("InstalledJList"); // NOI18N
        spInstalled.setViewportView(jlInstalled);

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

        btFlterAvailable.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/filter_list_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btFlterAvailable.setToolTipText("Sort & Filter...");
        btFlterAvailable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFlterAvailableActionPerformed(evt);
            }
        });

        btFilterInstalled.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/filter_list_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btFilterInstalled.setToolTipText("Sort & Filter...");
        btFilterInstalled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFilterInstalledActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btFlterAvailable))
                            .addComponent(spAvailable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btInstall)
                            .addComponent(btRemove)
                            .addComponent(btDownload)
                            .addComponent(btDelete))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btFilterInstalled))
                            .addComponent(spInstalled, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btFlterAvailable)
                            .addComponent(btFilterInstalled))
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
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
                        .addGap(0, 56, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spInstalled, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spAvailable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setModel(JList list, Oolite2.OoliteExpansionListModel elm, FilterAndSearchUtil.FilterMode fm, String searchString, FilterAndSearchUtil.SortMode sortMode, FilteredListModel.Filter<Expansion> baseFilter) {
        FilteredListModel.Filter<Expansion> user = FilterAndSearchUtil.getExpansionFilter(fm, searchString);
        FilteredListModel.Filter<Expansion> filter = new FilteredListModel.AndFilter<Expansion>(baseFilter, user);
        FilteredListModel<Expansion> lm = new FilteredListModel<Expansion>(elm, filter);
        
        Comparator<Expansion> comparator = FilterAndSearchUtil.getExpansionComparator(sortMode);
        SortedListModel<Expansion> elmSorted = new SortedListModel<>(lm, SortOrder.ASCENDING, comparator);
        
        log.trace("setting filter on {}: {}", list.getName(), filter);
        log.trace("setting comparator on {}: {}", list.getName(), comparator);
        
        list.setModel(elmSorted);
    }
    
    private void btFlterAvailableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFlterAvailableActionPerformed
        FilterAndSearch fas = new FilterAndSearch();
        fas.setFilterMode(availableFilterMode);
        fas.setSearchString(availableSearchString);
        fas.setSortMode(availableSortMode);
        if (JOptionPane.showConfirmDialog(spAvailable, fas, "Available Filter and Sort", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            log.debug("set new filter for available");
            availableFilterMode = fas.getFilterMode();
            availableSortMode = fas.getSortMode();
            availableSearchString = fas.getSearchString();
            
            setModel(jlAvailable, elmAvailable, availableFilterMode, availableSearchString, availableSortMode, notEnabledFilter);
        }
    }//GEN-LAST:event_btFlterAvailableActionPerformed

    private void btFilterInstalledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFilterInstalledActionPerformed
        FilterAndSearch fas = new FilterAndSearch();
        fas.setFilterMode(installedFilterMode);
        fas.setSearchString(installedSearchString);
        fas.setSortMode(installedSortMode);
        if (JOptionPane.showConfirmDialog(spInstalled, fas, "Installed Filter and Sort", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            log.debug("set new filter for installed");
            installedFilterMode = fas.getFilterMode();
            installedSortMode = fas.getSortMode();
            installedSearchString = fas.getSearchString();
            
            setModel(jlInstalled, elmInstalled, installedFilterMode, installedSearchString, installedSortMode, enabledFilter);
        }
    }//GEN-LAST:event_btFilterInstalledActionPerformed
    
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

    /**
     * From Oolite2.OoliteListener.
     * @param status 
     */
    @Override
    public void statusChanged(Oolite2.Status status) {
        log.debug("statusChanged({})", status);
    }

    /**
     * From Oolite2.OoliteListener.
     * @param message
     */
    @Override
    public void problemDetected(String message) {
        // message will be served by MainFrame
    }

    /**
     * From Oolite2.OoliteListener.
     * @param status 
     */
    @Override
    public void launched(ProcessData pd) {
        log.debug("launcher({})", pd);
    }

    /**
     * From Oolite2.OoliteListener.
     * @param status 
     */
    @Override
    public void terminated() {
        log.debug("terminated()");
    }

    /**
     * From Oolite2.OoliteListener.
     * @param status 
     */
    @Override
    public void activatedInstallation(Installation installation) {
        log.debug("activatedInstallation({})", installation);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btDelete;
    private javax.swing.JButton btDownload;
    private javax.swing.JButton btFilterInstalled;
    private javax.swing.JButton btFlterAvailable;
    private javax.swing.JButton btInstall;
    private javax.swing.JButton btRemove;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JList<Expansion> jlAvailable;
    private javax.swing.JList<Expansion> jlInstalled;
    private javax.swing.JScrollPane spAvailable;
    private javax.swing.JScrollPane spInstalled;
    // End of variables declaration//GEN-END:variables
}
