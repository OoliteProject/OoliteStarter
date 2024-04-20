/*
 */
package oolite.starter.ui;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import oolite.starter.ExpansionManager;
import oolite.starter.Oolite2;
import oolite.starter.model.Command;
import oolite.starter.model.Expansion;
import oolite.starter.model.ExpansionReference;
import oolite.starter.model.SaveGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class SaveGamePanel extends javax.swing.JPanel implements ExpansionManager.ExpansionManagerListener {
    private static final Logger log = LogManager.getLogger();
    
    private transient SaveGame data;
    private DefaultListModel<ExpansionReference> dlm;
    
    private transient Action installAction = new AbstractAction("Install") {
        private static final Logger log = LogManager.getLogger();

        @Override
        public void actionPerformed(ActionEvent ae) {
            log.warn("actionPerformed({})", ae);
            if (oolite == null) {
                throw new IllegalStateException("oolite must not be null");
            }

            ExpansionReference er = lsExpansions.getSelectedValue();
            Expansion e = oolite.getExpansionByExpansionReference(er);
            log.warn("Expansion={}", e);

            if (e == null) {
                JOptionPane.showMessageDialog(SaveGamePanel.this, "Don't know how to install\n    " + er.getName() + "\nCould this possibly be an OXP?", "Error", JOptionPane.ERROR_MESSAGE);
            } else {

                new Thread(() -> {
                    try {
                        e.install();
                    } catch (Exception ex) {
                        log.error("Could not install", ex);
                    }
                }).start();
            }
        }
    };
    
    private transient Action removeAction = new AbstractAction("Remove") {
        private static final Logger log = LogManager.getLogger();

        @Override
        public void actionPerformed(ActionEvent ae) {
            log.warn("actionPerformed({})", ae);
            if (oolite == null) {
                throw new IllegalStateException("oolite must not be null");
            }

            ExpansionReference er = lsExpansions.getSelectedValue();
            if (er == null) {
                JOptionPane.showMessageDialog(SaveGamePanel.this, "Please select reference first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Expansion e = oolite.getExpansionByExpansionReference(er);
            log.warn("Expansion={}", e);

            if (e == null) {
                JOptionPane.showMessageDialog(SaveGamePanel.this, "Don't know how to remove " + er.getName(), "Error", JOptionPane.ERROR_MESSAGE);
            } else {

                new Thread(() -> {
                    try {
                        e.remove();
                    } catch (Exception ex) {
                        log.error("Could not remove", ex);
                    }
                }).start();
            }
        }
    };
    
    private transient Action copyAction =  new AbstractAction("Copy") {
        private static final Logger log = LogManager.getLogger();

        @Override
        public void actionPerformed(ActionEvent ae) {
            log.warn("actionPerformed({})", ae);

            StringBuilder sb = new StringBuilder("Identifier\tStatus\n");

            List<ExpansionReference> selection = lsExpansions.getSelectedValuesList();
            for (ExpansionReference er: selection) {
                log.warn("  er: {}", er);
                sb.append(er.getName());
                sb.append("\t");
                sb.append(er.getStatus());
                sb.append("\n");
            }

            Toolkit toolkit = lsExpansions.getToolkit();
            toolkit.getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
        }
    };
    
    private transient Action fixAction = new AbstractAction("Fix...", new ImageIcon(getClass().getResource("/icons/healing_FILL0_wght400_GRAD0_opsz24.png"))) {
        private static final Logger log = LogManager.getLogger();

        @Override
        public void actionPerformed(ActionEvent ae) {
            log.debug("actionPerformed({})", ae);
            
            // list all the missing/superfluous expansions
            List<Command> commands = oolite.buildCommandList(data.getExpansions());
            
            if (commands.isEmpty()) {
                JOptionPane.showMessageDialog(SaveGamePanel.this, "Don't know how to install the missing ones.");
            } else {
                DefaultListModel<Command> dlm = new DefaultListModel<>();
                dlm.addAll(commands);
                JList<Command> list = new JList<>(dlm);
                list.setCellRenderer(new CommandCellRenderer());
                JScrollPane jsp = new JScrollPane(list);
                // allow the user to approve
                if (JOptionPane.showOptionDialog(SaveGamePanel.this, jsp, "Confirm fix...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null)
                        == JOptionPane.OK_OPTION) {
                    
                    ExpansionManager.getInstance().addExpansionManagerListener(SaveGamePanel.this);
                    ExpansionManager.getInstance().addCommands(commands);
                }
            }
            
            // execute the approved list while rendering progress
        }
    };
    
    private transient Oolite2 oolite;
    
    /**
     * Creates new form SaveGamePanel.
     */
    public SaveGamePanel() {
        log.debug("SaveGamePanel()");
        initComponents();
        lsExpansions.setCellRenderer(new ExpansionReferenceCellRenderer());
        lsExpansions.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateTooltip(e);
            }
        });
        
        // add CCP support - see https://docs.oracle.com/javase/tutorial/uiswing/dnd/listpaste.html
        ActionMap map = lsExpansions.getActionMap();
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME), copyAction);
        
        InputMap imap = lsExpansions.getInputMap();
        imap.put(KeyStroke.getKeyStroke("ctrl C"), TransferHandler.getCopyAction().getValue(Action.NAME));
        
        lsExpansions.addListSelectionListener(lse -> {
            log.debug("selectionChanged({})", lse);
            ExpansionReference er = lsExpansions.getSelectedValue();
            if (er != null && er.getStatus() == ExpansionReference.Status.MISSING) {
                installAction.setEnabled(true);
                removeAction.setEnabled(false);
            } else if (er != null && (
                    er.getStatus() == ExpansionReference.Status.SURPLUS
                    || er.getStatus() == ExpansionReference.Status.CONFLICT
                    || er.getStatus() == ExpansionReference.Status.REQUIRED_MISSING)
                ) {
                installAction.setEnabled(false);
                removeAction.setEnabled(true);
            } else {
                installAction.setEnabled(false);
                removeAction.setEnabled(false);
            }
        });

        fixAction.setEnabled(false);
        btFix.setAction(fixAction);
    }
    
    private void updateTooltip(MouseEvent e) {
        int rowIndex = lsExpansions.locationToIndex(e.getPoint());
        if (dlm != null && rowIndex >= 0) {
            ExpansionReference er = dlm.getElementAt(rowIndex);
            if (er.getReasons().isEmpty()) {
                lsExpansions.setToolTipText(null);
            } else {
                lsExpansions.setToolTipText(String.valueOf(er.getReasons()));
            }
        }
    }
    
    /**
     * Sets the oolite instance to fix expansions.
     * 
     * @param oolite the oolite to use
     */
    public void setOolite(Oolite2 oolite) {
        this.oolite = oolite;
    }
    
    private void updateFields() {
        txtFilename.setText(String.valueOf(data.getFile()));
        txtOoliteVersion.setText(String.valueOf(data.getOoliteVersion()));
        txtShipName.setText(String.valueOf(data.getShipName()));
        txtShipClass.setText(String.valueOf(data.getShipClassName()));
        txtStarSystem.setText(String.valueOf(data.getCurrentSystemName()));
        txtCredits.setText(String.valueOf(data.getCredits()));
        txtShipKills.setText(String.valueOf(data.getShipKills()));
        txtPilotName.setText(String.valueOf(data.getPlayerName()));
        fixAction.setEnabled(false);

        dlm = new DefaultListModel<>();
        if (data.getExpansions() != null) {
            dlm.addAll(data.getExpansions());

            if (dlm.size()==0) {
                // indicate we have no data?
            }

            Border border = null;
            if (data.hasMissingExpansions()) {
                border = new LineBorder(Color.red);
                fixAction.setEnabled(true);
            } else if (data.hasTooManyExpansions()) {
                border = new LineBorder(Color.orange);
                fixAction.setEnabled(true);
            }
            jScrollPane1.setBorder(border);
        } else {
                // indicate we have no data?
                jScrollPane1.setBorder(null);
        }
        lsExpansions.setModel(dlm);
        
        if (!dlm.isEmpty()) {
            lsExpansions.setComponentPopupMenu(getPopupMenu());
        }
        
        revalidate();
    }
    
    private JPopupMenu getPopupMenu() {
        JPopupMenu jpm = new JPopupMenu();
        jpm.add(installAction);
        jpm.add(removeAction);
        return jpm;
    }
    
    private void emptyFields() {
        txtFilename.setText("");
        txtOoliteVersion.setText("");
        txtShipName.setText("");
        txtShipClass.setText("");
        txtStarSystem.setText("");
        txtCredits.setText("");
        txtShipKills.setText("");
        txtPilotName.setText("");

        lsExpansions.setModel(new DefaultListModel<>());
        lsExpansions.setComponentPopupMenu(null);
        jScrollPane1.setBorder(null);
    }
    
    /**
     * Sets the data to be rendered by this panel.
     * 
     * @param data the SaveGame data
     */
    public void setData(SaveGame data) {
        this.data = data;
        if (data != null) {
            updateFields();
        } else {
            emptyFields();
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

        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtStarSystem = new javax.swing.JTextField();
        txtCredits = new javax.swing.JTextField();
        txtShipKills = new javax.swing.JTextField();
        txtPilotName = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtShipName = new javax.swing.JTextField();
        txtShipClass = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtOoliteVersion = new javax.swing.JTextField();
        txtFilename = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lsExpansions = new javax.swing.JList<>();
        btFix = new javax.swing.JButton();

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Pilot"));

        jLabel1.setText("Name");

        jLabel4.setText("Ship Kills");

        jLabel3.setText("Star System");

        jLabel2.setText("Credits");

        txtStarSystem.setEditable(false);

        txtCredits.setEditable(false);

        txtShipKills.setEditable(false);

        txtPilotName.setEditable(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCredits)
                    .addComponent(txtStarSystem)
                    .addComponent(txtShipKills)
                    .addComponent(txtPilotName))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtPilotName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtShipKills, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtStarSystem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCredits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Ship"));

        jLabel5.setText("Class");

        jLabel6.setText("Name");

        txtShipName.setEditable(false);

        txtShipClass.setEditable(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addGap(54, 54, 54)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtShipName)
                    .addComponent(txtShipClass))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtShipClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtShipName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("System"));

        jLabel7.setText("Oolite Version");

        jLabel8.setText("File");

        txtOoliteVersion.setEditable(false);

        txtFilename.setEditable(false);

        jLabel9.setText("Expansions");

        jScrollPane1.setViewportView(lsExpansions);

        btFix.setText("Fix...");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btFix))
                    .addComponent(txtOoliteVersion)
                    .addComponent(txtFilename, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtOoliteVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(btFix))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btFix;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList<ExpansionReference> lsExpansions;
    private javax.swing.JTextField txtCredits;
    private javax.swing.JTextField txtFilename;
    private javax.swing.JTextField txtOoliteVersion;
    private javax.swing.JTextField txtPilotName;
    private javax.swing.JTextField txtShipClass;
    private javax.swing.JTextField txtShipKills;
    private javax.swing.JTextField txtShipName;
    private javax.swing.JTextField txtStarSystem;
    // End of variables declaration//GEN-END:variables

    /**
     * From ExpansionManager.ExpansionManagerListener.
     * 
     * @param status
     * @param queue 
     */
    @Override
    public void updateStatus(ExpansionManager.Status status, List<Command> queue) {
        log.debug("updateStatus(...)");
        int pending = 0;
        log.info("ExpansionManager queue size: {}", queue.size());
    }
}
