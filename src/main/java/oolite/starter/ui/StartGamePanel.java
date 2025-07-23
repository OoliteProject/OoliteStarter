/*
 */
package oolite.starter.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Files;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;
import oolite.starter.Oolite;
import oolite.starter.Oolite2;
import oolite.starter.model.Installation;
import oolite.starter.model.ProcessData;
import oolite.starter.model.SaveGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class StartGamePanel extends javax.swing.JPanel implements Oolite.OoliteListener, Oolite2.OoliteListener {
    private static final Logger log = LogManager.getLogger();

    private static final String STARTGAMEPANEL_COULD_NOT_RUN_GAME = "Could not run game";
    private static final String STARTGAMEPANEL_COULD_NOT_RELOAD = "Could not reload";
    
    private transient Oolite ooliteDriver;
    private transient Oolite2 oolite2Driver;
    private SaveGameTableModel model;
    private SaveGamePanel sgp;

    /**
     * Invoked whenever the Oolite2 status changes.
     * From Oolite2.OoliteListener
     * 
     * @param status the new status
     */
    @Override
    public void statusChanged(Oolite2.Status status) {
        log.debug("statusChanged({})", status);
        if (status == Oolite2.Status.INITIALIZED) {
            update();
        }
    }

    /**
     * From Oolite2.OoliteListener.
     * @param message
     */
    @Override
    public void problemDetected(String message) {
        // message will be served by MainFrame
    }
    
    private enum RunState {
        IDLE, RUNNING
    }

    private int previousWindowState;
    private DimAroundCenteredPanel glassPanel;
    private WaitPanel waitPanel;
    private RunState runState = RunState.IDLE;

    /**
     * Creates new form StartGamePanel.
     */
    public StartGamePanel() {
        initComponents();
        setName("Start Game");
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
    public void setOolite(Oolite oolite, Oolite2 oolite2) {
        if (this.ooliteDriver != null) {
            this.ooliteDriver.removeOoliteListener(this);
        }
        this.ooliteDriver = oolite;
        update();
        oolite.addOoliteListener(this);
        
        jTable1.getSelectionModel().addListSelectionListener(lse -> {
            log.trace("valueChanged({})", lse);
            if (!lse.getValueIsAdjusting()) {
                // we have a final value - let's render it
                int rowIndex = jTable1.getSelectedRow();
                if (rowIndex >=0 ) {
                    rowIndex = jTable1.convertRowIndexToModel(rowIndex);
                    SaveGame row = model.getRow(rowIndex);
                    sgp.setData(row);
                }

                btResume.setEnabled(jTable1.getSelectedRow() != -1);
                btDelete.setEnabled(jTable1.getSelectedRow() != -1);
            }
        });
        
        sgp = new SaveGamePanel();
        sgp.setOolite(oolite2);
        jSplitPane1.setRightComponent(sgp);
        
        if (oolite2Driver != null) {
            oolite2Driver.removeOoliteListener(this);
        }
        this.oolite2Driver = oolite2;
        oolite2Driver.addOoliteListener(this);
        
        update();
    }
    
    private void update() {
        try {
            model = new SaveGameTableModel(ooliteDriver.getSaveGames());
            jTable1.clearSelection();
            jTable1.setAutoCreateColumnsFromModel(true);
            jTable1.setDefaultRenderer(Date.class, new DateCellRenderer(DateFormat.getDateTimeInstance()));
            jTable1.setModel(model);
            Util.setColumnWidths(jTable1);
            
            TableRowSorter<SaveGameTableModel> trw = new TableRowSorter<>(model);
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            trw.setSortKeys(sortKeys);
            jTable1.setRowSorter(trw);
            
            if (sgp != null) {
                sgp.setData(null);
            }
            
            jpStatus.removeAll();
            jpStatus.add(new Badge("Save Games", String.valueOf(model.getRowCount()), Color.BLACK));
            
            if (oolite.starter.util.Util.isMac()) {
                btResume.setToolTipText("Not available on MacOS");
            } else {
                btResume.setEnabled(jTable1.getSelectedRow() != -1);
            }
            btDelete.setEnabled(jTable1.getSelectedRow() != -1);
        } catch (Exception e) {
            log.warn("Could not update", e);
        }
    }
    
    private String constructMessage(String m, Throwable t) {
        StringBuilder sb = new StringBuilder(m);
        while (t != null) {
            sb.append("\n").append(t.getClass().getName()).append(": ").append(t.getMessage());
            t = t.getCause();
        }
        
        sb.append("\n\nYou better check the logfiles in $HOME/.Oolite/Logs now.");
        return sb.toString();
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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btReload = new javax.swing.JButton();
        btNew = new javax.swing.JButton();
        btResume = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(30, 0), new java.awt.Dimension(32767, 0));
        btDelete = new javax.swing.JButton();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jpStatus = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());
        add(jPanel1, java.awt.BorderLayout.LINE_END);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        btReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/refresh_FILL0_wght400_GRAD0_opsz48.png"))); // NOI18N
        btReload.setText("Reload");
        btReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btReloadActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        jPanel2.add(btReload, gridBagConstraints);

        btNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/play_arrow_FILL0_wght400_GRAD0_opsz48.png"))); // NOI18N
        btNew.setText("New");
        btNew.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btNewActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 5, 2);
        jPanel2.add(btNew, gridBagConstraints);

        btResume.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/resume_FILL0_wght400_GRAD0_opsz48.png"))); // NOI18N
        btResume.setText("Resume");
        btResume.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btResume.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btResumeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 5, 5);
        jPanel2.add(btResume, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel2.add(filler1, gridBagConstraints);

        btDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/delete_forever_FILL0_wght400_GRAD0_opsz48.png"))); // NOI18N
        btDelete.setText("Delete");
        btDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDeleteActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel2.add(btDelete, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setDividerSize(7);
        jSplitPane1.setOneTouchExpandable(true);

        jScrollPane1.setMinimumSize(new java.awt.Dimension(300, 16));

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"No save games found"},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Save Games"
            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(jTable1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        jpStatus.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        add(jpStatus, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void btNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btNewActionPerformed
        log.debug("btNewActionPerformed({})", evt);

        showWaitPanel();
        
        try {
            new Thread() {
                @Override
                public void run() {
                    try {
                        ooliteDriver.run();
                    } catch (InterruptedException e) {
                        log.error("Interrupted", e);
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        log.error(STARTGAMEPANEL_COULD_NOT_RUN_GAME, e);
                        JOptionPane.showMessageDialog(StartGamePanel.this, constructMessage(STARTGAMEPANEL_COULD_NOT_RUN_GAME, e), "Error on Oolite", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        hideWaitPanel();
                    }
                }
            }.start();
        } catch (Exception e) {
            log.error(STARTGAMEPANEL_COULD_NOT_RUN_GAME, e);
            hideWaitPanel();
            JOptionPane.showMessageDialog(this, constructMessage(STARTGAMEPANEL_COULD_NOT_RUN_GAME, e));
        }
    }//GEN-LAST:event_btNewActionPerformed

    private void btResumeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btResumeActionPerformed
        log.debug("btResumeActionPerformed({})", evt);

        try {
            int rowIndex = jTable1.getSelectedRow();
            if (rowIndex == -1) {
                throw new IllegalStateException("Which savegame you want to start?");
            }
            
            rowIndex = jTable1.convertRowIndexToModel(rowIndex);
            SaveGame row = model.getRow(rowIndex);
            
            if (row.hasMissingExpansions() || row.hasTooManyExpansions()) {
                if (JOptionPane.showConfirmDialog(this, "The installed expansions do not match the savegame. Do you want to continue?", "Discrepancy detected...", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            showWaitPanel();
        
            new Thread() {
                @Override
                public void run() {
                    try {
                        ooliteDriver.run(row);
                    } catch (InterruptedException e) {
                        log.error("Interrupted", e);
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        log.error(STARTGAMEPANEL_COULD_NOT_RUN_GAME, e);
                        JOptionPane.showMessageDialog(StartGamePanel.this, constructMessage(STARTGAMEPANEL_COULD_NOT_RUN_GAME, e), "Error on Oolite", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        hideWaitPanel();
                    }
                }
            }.start();
        } catch (Exception e) {
            log.error(STARTGAMEPANEL_COULD_NOT_RUN_GAME, e);
            hideWaitPanel();
            JOptionPane.showMessageDialog(null, constructMessage(STARTGAMEPANEL_COULD_NOT_RUN_GAME, e));
        }
    }//GEN-LAST:event_btResumeActionPerformed

    private void btReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btReloadActionPerformed
        log.debug("btReloadActionPerformed({})", evt);

        try {
            update();
        } catch (Exception e) {
            log.error(STARTGAMEPANEL_COULD_NOT_RELOAD, e);
            JOptionPane.showMessageDialog(null, STARTGAMEPANEL_COULD_NOT_RELOAD);
        }
    }//GEN-LAST:event_btReloadActionPerformed

    private void btDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDeleteActionPerformed
        log.debug("btDeleteActionPerformed({})", evt);

        try {
            int rowIndex = jTable1.getSelectedRow();
            if (rowIndex == -1) {
                throw new IllegalStateException("Which savegame you want to delete?");
            }
            
            rowIndex = jTable1.convertRowIndexToModel(rowIndex);
            SaveGame row = model.getRow(rowIndex);
            
            if (JOptionPane.showConfirmDialog(btDelete, "Too much clutter causes confusion, kid. Its a good choice to cut down the clobber - but do you really want to chuck " + row.getName() + "?", "Delete...", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            Files.delete(row.getFile().toPath());
            update();
        } catch (Exception e) {
            log.error("Could not delete", e);
            JOptionPane.showMessageDialog(null, constructMessage("Could not delete", e));
        }

    }//GEN-LAST:event_btDeleteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btDelete;
    private javax.swing.JButton btNew;
    private javax.swing.JButton btReload;
    private javax.swing.JButton btResume;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel jpStatus;
    // End of variables declaration//GEN-END:variables

    private void showWaitPanel() {

        JFrame f = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (glassPanel == null) {
            waitPanel = new WaitPanel(ooliteDriver);
            glassPanel = new DimAroundCenteredPanel(waitPanel);

            final JPanel glasspane = (JPanel)f.getGlassPane();
            if (!(glasspane.getLayout() instanceof GridBagLayout)) {
                glasspane.setLayout(new GridBagLayout());
            }
            glasspane.removeAll();
            glasspane.add(glassPanel, new GridBagConstraints(1, 1, 1, 1, 1.0d, 1.0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        }
        
        f.getGlassPane().setVisible(true);
    }
    
    private void hideWaitPanel() {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                JFrame d = (JFrame)SwingUtilities.getWindowAncestor(glassPanel);
                d.setState(previousWindowState);
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                log.debug("interrupted", ex);
                Thread.currentThread().interrupt();
            }
            SwingUtilities.invokeLater(() -> {
                if (glassPanel != null) {
                    JFrame d = (JFrame)SwingUtilities.getWindowAncestor(glassPanel);

                    final JPanel glasspane = (JPanel)d.getGlassPane();
                    glasspane.removeAll();
                    glasspane.setVisible(false);
                }
                
                waitPanel = null;
                glassPanel = null;
            });
        }).start();
    }
    
    @Override
    public void launched(ProcessData pd) {
        log.warn("launched({})", pd);

        runState = RunState.RUNNING;
        
        File logfile = new File(System.getProperty("user.home"), ".Oolite/Logs");

        StringBuilder sb = new StringBuilder("<html><p>Launched process <tt>");
        sb.append(pd.getPid());
        sb.append("</tt> with command line</p><p><tt>");
        sb.append(String.valueOf(pd.getCommand()));
        sb.append("</tt></p><p>in directory</p><p><tt>");
        sb.append(pd.getCwd().getAbsolutePath());
        sb.append("</tt></p><p>Currently we are waiting for this process to finish.</p>");
        sb.append("<p>If you do not see Oolite showing up, consider taking a look at the <a href=\"file://");
        sb.append(logfile).append("\">logfiles</a>. More help may be available at <a href=\"https://bb.oolite.space/\">the forum</a>.</p>");
        sb.append("</html>");
        
        final String text = sb.toString();
        
        
        new Thread(() -> {
            try {
                SwingUtilities.invokeAndWait(() -> waitPanel.setText(text) );
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                log.debug("interrupted", ex);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Could not set text", e);
            }
            SwingUtilities.invokeLater(() -> {
                if (runState == RunState.RUNNING) {
                    JFrame f = (JFrame)SwingUtilities.getRoot(this);
                    previousWindowState = f.getState();
                    f.setState(java.awt.Frame.ICONIFIED);
                }
            });
        }).start();
        
    }

    @Override
    public void terminated() {
        log.warn("terminated()");
        runState = RunState.IDLE;
        update();
        hideWaitPanel();
    }

    @Override
    public void activatedInstallation(Installation installation) {
        log.error("activatedInstallation({})", installation);
        
        try {
            update();
        } catch (Exception e) {
            log.error(STARTGAMEPANEL_COULD_NOT_RELOAD, e);
            JOptionPane.showMessageDialog(null, STARTGAMEPANEL_COULD_NOT_RELOAD);
        }
    }
}
