/*
 */
package oolite.starter.ui2;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import oolite.starter.Oolite;
import oolite.starter.generic.ListAction;
import oolite.starter.model.Installation;
import oolite.starter.model.ProcessData;
import oolite.starter.model.SaveGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * New UI as suggested by community.
 * See http://aegidian.org/bb/viewtopic.php?p=293920#p293920
 * 
 * @author arquebus
 * @author hiran
 * @deprecated Use oolite.starter.ui.StartGamePanel instead
 */
@Deprecated(since = "21FEB24", forRemoval = true)
public class StartGamePanel2 extends javax.swing.JPanel implements Oolite.OoliteListener {
    private static final Logger log = LogManager.getLogger();

    private transient Oolite ooliteDriver;
    private SaveGameListModel saveGameListModel;
    private SaveGame newGame;
    private ListAction listAction;
    
    /**
     * Creates new form StartGamePanel2.
     */
    public StartGamePanel2() {
        initComponents();
        setName("Start Game");
        jList1.setCellRenderer(new SaveGameCellRenderer());

        listAction = new ListAction(jList1, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.debug("actionPerformed(...)");
                int rowIndex = jList1.getSelectedIndex();
                SaveGame saveGame = saveGameListModel.getElementAt(rowIndex);
                runSaveGame(saveGame);
            }
        });
        
        newGame = new SaveGame();
        newGame.setName("New Game...");
    }
    
    private void runSaveGame(SaveGame saveGame) {
        try {
            if (saveGame.getFile() == null) {
                ooliteDriver.run();
            } else {
                ooliteDriver.run(saveGame);
            }
        } catch (InterruptedException e) {
            log.warn("runSaveGame was interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Could not run game", e);
            JOptionPane.showMessageDialog(this, "Could not run game: " + e.getMessage(), "Error", JOptionPane.PLAIN_MESSAGE);
        }
    }
    
    private void update() {
        log.debug("update()");
        try {
            List<SaveGame> saveGames = ooliteDriver.getSaveGames();
            saveGames.add(0, newGame);
            saveGameListModel = new SaveGameListModel(saveGames);
            
            jList1.clearSelection();
            jList1.setModel(saveGameListModel);

//            TableRowSorter<SaveGameTableModel> trw = new TableRowSorter<>(model);
//            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
//            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
//            trw.setSortKeys(sortKeys);
//            jTable1.setRowSorter(trw);
            
//            if (sgp != null) {
//                sgp.setData(null);
//            }
            
//            jpStatus.removeAll();
//            jpStatus.add(new Badge("Save Games", String.valueOf(model.getRowCount()), Color.BLACK));
            
//            if (oolite.starter.util.Util.isMac()) {
//                btResume.setToolTipText("Not available on MacOS");
//            } else {
//                btResume.setEnabled(jTable1.getSelectedRow() != -1);
//            }
//            btDelete.setEnabled(jTable1.getSelectedRow() != -1);
        } catch (Exception e) {
            log.warn("Could not update", e);
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
    public void setOolite(Oolite oolite) {
        if (this.ooliteDriver != null) {
            this.ooliteDriver.removeOoliteListener(this);
        }
        this.ooliteDriver = oolite;
        update();
        oolite.addOoliteListener(this);
        
        jList1.getSelectionModel().addListSelectionListener(lse -> {
            log.debug("valueChanged({})", lse);
            if (!lse.getValueIsAdjusting()) {
                // we have a final value - let's render it
//                int rowIndex = jTable1.getSelectedRow();
//                if (rowIndex >=0 ) {
//                    rowIndex = jTable1.convertRowIndexToModel(rowIndex);
//                    SaveGame row = model.getRow(rowIndex);
//                    sgp.setData(row);
//                }

                SaveGame saveGame = saveGameListModel.getElementAt(jList1.getSelectedIndex());
                //btResume.setEnabled(saveGame.getFile() != null);
                btDelete.setEnabled(saveGame.getFile() != null);
            }
        });
        
//        sgp = new SaveGamePanel();
//        jSplitPane1.setRightComponent(sgp);
        
        update();
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

        btPlay = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        btDelete = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        btPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/play_arrow_FILL0_wght400_GRAD0_opsz48.png"))); // NOI18N
        btPlay.setText("Play!");
        btPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPlayActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(btPlay, gridBagConstraints);

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Save Game"));
        jScrollPane4.setViewportView(jList1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane4, gridBagConstraints);

        btDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/delete_forever_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        btDelete.setText("Delete");
        btDelete.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(btDelete, gridBagConstraints);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/save_FILL0_wght400_GRAD0_opsz24.png"))); // NOI18N
        jButton7.setText("Backup");
        jButton7.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jButton7, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void btPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPlayActionPerformed
        log.debug("btPlayActionPerformed(...)");
        int rowIndex = jList1.getSelectedIndex();
        SaveGame saveGame = saveGameListModel.getElementAt(rowIndex);
        runSaveGame(saveGame);
    }//GEN-LAST:event_btPlayActionPerformed
    
    @Override
    public void launched(ProcessData pd) {
        log.warn("launched({})", pd);
//
//        runState = RunState.RUNNING;
//        
//        File logfile = new File(System.getProperty("user.home"), ".Oolite/Logs");
//
//        StringBuilder sb = new StringBuilder("<html><p>Launched process <tt>");
//        sb.append(pd.getPid());
//        sb.append("</tt> with command line</p><p><tt>");
//        sb.append(String.valueOf(pd.getCommand()));
//        sb.append("</tt></p><p>in directory</p><p><tt>");
//        sb.append(pd.getCwd().getAbsolutePath());
//        sb.append("</tt></p><p>Currently we are waiting for this process to finish.</p>");
//        sb.append("<p>If you do not see Oolite showing up, consider taking a look at the <a href=\"file://");
//        sb.append(logfile).append("\">logfiles</a>. More help may be available at <a href=\"http://aegidian.org/bb/viewtopic.php?f=9&t=21405\">the forum</a>.</p>");
//        sb.append("</html>");
//        
//        final String text = sb.toString();
//        
//        
//        new Thread(() -> {
//            try {
//                SwingUtilities.invokeAndWait(() -> waitPanel.setText(text) );
//                Thread.sleep(2000);
//            } catch (InterruptedException ex) {
//                log.debug("interrupted", ex);
//                Thread.currentThread().interrupt();
//            } catch (Exception e) {
//                log.error("Could not set text", e);
//            }
//            SwingUtilities.invokeLater(() -> {
//                if (runState == RunState.RUNNING) {
//                    JFrame f = (JFrame)SwingUtilities.getRoot(this);
//                    previousWindowState = f.getState();
//                    f.setState(java.awt.Frame.ICONIFIED);
//                }
//            });
//        }).start();
//        
    }

    @Override
    public void terminated() {
        log.warn("terminated()");
//        runState = RunState.IDLE;
//        update();
//        hideWaitPanel();
    }

    @Override
    public void activatedInstallation(Installation installation) {
        log.error("activatedInstallation({})", installation);
        
//        try {
//            update();
//        } catch (Exception e) {
//            log.error(STARTGAMEPANEL_COULD_NOT_RELOAD, e);
//            JOptionPane.showMessageDialog(null, STARTGAMEPANEL_COULD_NOT_RELOAD);
//        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btDelete;
    private javax.swing.JButton btPlay;
    private javax.swing.JButton jButton7;
    private javax.swing.JList<SaveGame> jList1;
    private javax.swing.JScrollPane jScrollPane4;
    // End of variables declaration//GEN-END:variables
}
