/*
 */
package oolite.starter.ui2;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import oolite.starter.ExpansionManager;
import oolite.starter.Oolite;
import oolite.starter.Oolite2;
import oolite.starter.model.Command;
import oolite.starter.model.OoliteFlavor;
import oolite.starter.ui.MrGimlet;
import oolite.starter.ui.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.NodeList;

/**
 *
 * @author hiran
 */
public class OoliteFlavorListCellRenderer extends javax.swing.JPanel implements ListCellRenderer<OoliteFlavor> {
    private static final Logger log = LogManager.getLogger();
    
    //private final Border emtpyBorder = new EmptyBorder(4, 2, 4, 4);
    //private final Border normalBorder = new CompoundBorder(new MatteBorder(0, 4, 0, 0, getBackground()), emtpyBorder);
    //private final Border warningBorder = new CompoundBorder(new MatteBorder(0, 4, 0, 0, Color.ORANGE), emtpyBorder);
    //private final Border problemBorder = new CompoundBorder(new MatteBorder(0, 4, 0, 0, Color.RED), emtpyBorder);
    
    private Oolite oolite;
    private Oolite2 oolite2;
    private OoliteFlavor data;

    /**
     * Creates new form OoliteFlavorListRenderer.
     */
    public OoliteFlavorListCellRenderer() {
        initComponents();
        setBorder(new CompoundBorder(new LineBorder(getBackground(), 8), new BevelBorder(BevelBorder.RAISED)));

        AbstractAction addAction = new AbstractAction("Add...") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.debug("actionPerformed(...)");
                log.warn("Install flavor {} from {}...", data.getName(), data.getExpansionSetUrl());
                
                confirmAndExecute(false);
            }
        };
        btAdd.setAction(addAction);

        AbstractAction pruneAddAction = new AbstractAction("Replace...") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                log.debug("actionPerformed(...)");
                log.warn("Install exclusive flavor {} from {}...", data.getName(), data.getExpansionSetUrl());
                
                confirmAndExecute(true);
            }
        };
        btPruneAdd.setAction(pruneAddAction);
    }
    
    /**
     * Sets the Oolite drivers.
     * 
     * @param oolite
     * @param oolite2 
     */
    public void setOolite(Oolite oolite, Oolite2 oolite2) {
        if (oolite == null) {
            throw new IllegalArgumentException("oolite must not be null");
        }
        if (oolite2 == null) {
            throw new IllegalArgumentException("oolite2 must not be null");
        }

        this.oolite = oolite;
        this.oolite2 = oolite2;
    }
    
    /**
     * Produces a list of necessary actions and allows the user to confirm
     * before it is actioned.
     * 
     * @param exclusive 
     */
    void confirmAndExecute(boolean exclusive) {
        if (oolite == null) {
            throw new IllegalStateException("oolite must not be null");
        }
        if (oolite2 == null) {
            throw new IllegalStateException("oolite2 must not be null");
        }
        
        try {
            NodeList nl = Oolite.parseExpansionSet(data.getExpansionSetUrl());
            log.warn("Parsed expansion set {}", data.getExpansionSetUrl());
                    
            // get the complete list what needs to be done
            List<Command> plan = oolite.buildCommandList(oolite2.getExpansions(), nl);

            // stuff that we keep is not worth mentioning
            plan = plan.stream()
                .filter(cmd -> 
                        cmd.getAction()!=Command.Action.KEEP
                )
                .collect(Collectors.toList());
            
            // if not exclusive we do not remove anything
            if (!exclusive) {
                plan = plan.stream()
                    .filter(cmd -> 
                            cmd.getAction()==Command.Action.INSTALL
                            || cmd.getAction()==Command.Action.INSTALL_ALTERNATIVE
                    )
                    .collect(Collectors.toList());
            }

            if (plan.isEmpty()) {
                JOptionPane.showConfirmDialog(this, "We're already there, kiddo.");
            } else  {
                // have user approve the plan
                String title = null;
                if (exclusive) {
                    title = "Confirm to install " + data.getName() + " exclusively...";
                } else {
                    title = "Confirm to install " + data.getName() + " additionally...";
                }

                if (JOptionPane.showConfirmDialog(this, Util.createCommandListPanel(plan), title, JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                    // execute the plan
                    ExpansionManager.getInstance().addCommands(plan);
                    MrGimlet.showMessage(this, "Working on it...");
                }
            }
        } catch (Exception e) {
            log.error("Could not install expansion set");
        }
    }
    
    /**
     * Sets the flavor data to be displayed.
     * 
     * @param data the flavor
     */
    public void setData(OoliteFlavor data) {
        this.data = data;
        
        if (data == null) {
            lbIcon.setIcon(null);
            txtTitle.setText("n/a");
            txtDescription.setText("n/a");
        } else {
            lbIcon.setIcon(new ImageIcon(data.getImage()));
            txtTitle.setText(data.getName());
            txtDescription.setText(data.getDescription());
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

        lbIcon = new javax.swing.JLabel();
        txtTitle = new javax.swing.JLabel();
        txtDescription = new javax.swing.JTextArea();
        btAdd = new javax.swing.JButton();
        btPruneAdd = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        lbIcon.setAlignmentX(0.5F);
        lbIcon.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(lbIcon, gridBagConstraints);

        txtTitle.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        txtTitle.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(txtTitle, gridBagConstraints);

        txtDescription.setEditable(false);
        txtDescription.setColumns(20);
        txtDescription.setLineWrap(true);
        txtDescription.setRows(5);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(txtDescription, gridBagConstraints);

        btAdd.setText("Add...");
        btAdd.setToolTipText("Installs missing. Other expansions are not touched.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(btAdd, gridBagConstraints);

        btPruneAdd.setText("Replace...");
        btPruneAdd.setToolTipText("Installs missing, but uninstalls not needed expansions.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(btPruneAdd, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public Component getListCellRendererComponent(JList<? extends OoliteFlavor> list, OoliteFlavor data, int i, boolean isSelected, boolean isFocused) {
        setData(data);
        
        btAdd.setVisible(isSelected);
        btPruneAdd.setVisible(isSelected);
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setEnabled(list.isEnabled());

        return this;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAdd;
    private javax.swing.JButton btPruneAdd;
    private javax.swing.JLabel lbIcon;
    private javax.swing.JTextArea txtDescription;
    private javax.swing.JLabel txtTitle;
    // End of variables declaration//GEN-END:variables
}
