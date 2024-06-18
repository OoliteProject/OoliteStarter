/*
 */
package oolite.starter.ui;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import oolite.starter.Oolite;
import oolite.starter.model.Installation;
import oolite.starter.mqtt.MqttUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class InstallationForm extends javax.swing.JPanel {

    private static final Logger log = LogManager.getLogger();

    private static final String INSTALLATIONFORM_USER_HOME = "user.home";
    private static final String INSTALLATIONFORM_SELECT = "Select";
    private static final String INSTALLATIONFORM_SUCCESS = "Success";
    private static final String INSTALLATIONFORM_ERROR = "Error";
    private static final String INSTALLATIONFORM_WARNING = "Warning";
    private static final String INSTALLATIONFORM_BROWSE = "Browse";

    private transient Installation data;
    private transient boolean passwordDirty = false;

    /**
     * Creates new InstallationForm.
     */
    public InstallationForm() {
        initComponents();
        
        pfMqttPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                passwordDirty = true;
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                passwordDirty = true;
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                passwordDirty = true;
            }
        });
        this.setData(new Installation());
    }

    /**
     * Populates the form with given data.
     *
     * @param data the data to display
     */
    public void setData(Installation data) {
        this.data = data;

        passwordDirty = false;
        
        if (data == null) {
            txtAddOnDir.setText("");
            txtDeactivatedAddOnDir.setText("");
            txtManagedDeactivatedAddOnDir.setText("");
            txtExecutable.setText("");
            txtHomeDir.setText("");
            txtManagedAddOnDir.setText("");
            txtSavegameDir.setText("");
            txtVersion.setText("");
            cbDCP.setSelected(false);

            cbUseMqtt.setSelected(false);
            txtMqttBrokerUrl.setText("");
            txtMqttUsername.setText("");
            txtMqttPrefix.setText("");
        } else {
            txtAddOnDir.setText(data.getAddonDir());
            txtDeactivatedAddOnDir.setText(data.getDeactivatedAddonDir());
            txtManagedDeactivatedAddOnDir.setText(data.getManagedDeactivatedAddonDir());
            txtExecutable.setText(data.getExcecutable());
            txtHomeDir.setText(data.getHomeDir());
            txtManagedAddOnDir.setText(data.getManagedAddonDir());
            txtSavegameDir.setText(data.getSavegameDir());
            txtVersion.setText(data.getVersion());
            cbDCP.setSelected(data.isDebugCapable());
            
            if (data.getMqtt() == null) {
                cbUseMqtt.setSelected(false);
                txtMqttBrokerUrl.setText("");
                txtMqttUsername.setText("");
                txtMqttPrefix.setText("");
            } else {
                cbUseMqtt.setSelected(true);
                txtMqttBrokerUrl.setText(data.getMqtt().getBrokerUrl());
                txtMqttUsername.setText(data.getMqtt().getUser());
                txtMqttPrefix.setText(data.getMqtt().getPrefix());
            }
        }
        
    }

    /**
     * Returns the form data including user edits.
     *
     * @return the data
     */
    public Installation getData() {
        data.setAddonDir(txtAddOnDir.getText());
        data.setDeactivatedAddonDir(txtDeactivatedAddOnDir.getText());
        data.setManagedAddonDir(txtManagedAddOnDir.getText());
        data.setManagedDeactivatedAddonDir(txtManagedDeactivatedAddOnDir.getText());
        data.setExcecutable(txtExecutable.getText());
        data.setHomeDir(txtHomeDir.getText());
        data.setSavegameDir(txtSavegameDir.getText());
        data.setVersion(txtVersion.getText());
        data.setDebugCapable(cbDCP.isSelected());
        
        if (cbUseMqtt.isSelected()) {
            if (data.getMqtt() == null) {
                data.setMqtt(new Installation.Mqtt());
            }
            data.getMqtt().setBrokerUrl(txtMqttBrokerUrl.getText());
            data.getMqtt().setUser(txtMqttUsername.getText());
            if (passwordDirty) {
                data.getMqtt().setPassword(pfMqttPassword.getPassword());
                passwordDirty = false;
            }
            data.getMqtt().setPrefix(txtMqttPrefix.getText());
        }
        
        return data;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        btHomeDir.setVisible(enabled);
        btExecutable.setVisible(enabled);
        btSavegameDir.setVisible(enabled);
        btAddOnDir.setVisible(enabled);
        btDeactivatedAddOnDir.setVisible(enabled);
        btManagedAddOnDir.setVisible(enabled);
        btManagedDeactivatedAddOnDir.setVisible(enabled);

        txtHomeDir.setEnabled(enabled);
        txtExecutable.setEnabled(enabled);
        txtSavegameDir.setEnabled(enabled);
        txtAddOnDir.setEnabled(enabled);
        txtDeactivatedAddOnDir.setEnabled(enabled);
        txtManagedAddOnDir.setEnabled(enabled);
        txtManagedDeactivatedAddOnDir.setEnabled(enabled);
        txtVersion.setEnabled(enabled);
        cbDCP.setEnabled(enabled);
        
        cbUseMqtt.setEnabled(enabled);
        txtMqttBrokerUrl.setEnabled(enabled);
        txtMqttUsername.setEnabled(enabled);
        pfMqttPassword.setEnabled(enabled);
        txtMqttPrefix.setEnabled(enabled);
        
        btTest.setVisible(enabled);
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtManagedDeactivatedAddOnDir = new javax.swing.JTextField();
        txtManagedAddOnDir = new javax.swing.JTextField();
        txtAddOnDir = new javax.swing.JTextField();
        txtSavegameDir = new javax.swing.JTextField();
        txtExecutable = new javax.swing.JTextField();
        txtVersion = new javax.swing.JTextField();
        txtHomeDir = new javax.swing.JTextField();
        btHomeDir = new javax.swing.JButton();
        btExecutable = new javax.swing.JButton();
        btSavegameDir = new javax.swing.JButton();
        btAddOnDir = new javax.swing.JButton();
        btManagedAddOnDir = new javax.swing.JButton();
        btManagedDeactivatedAddOnDir = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        btDeactivatedAddOnDir = new javax.swing.JButton();
        txtDeactivatedAddOnDir = new javax.swing.JTextField();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(3, 0), new java.awt.Dimension(3, 0), new java.awt.Dimension(32767, 0));
        jLabel9 = new javax.swing.JLabel();
        cbDCP = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        cbUseMqtt = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        txtMqttBrokerUrl = new javax.swing.JTextField();
        txtMqttUsername = new javax.swing.JTextField();
        pfMqttPassword = new javax.swing.JPasswordField();
        txtMqttPrefix = new javax.swing.JTextField();
        btTest = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Home Directory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel1, gridBagConstraints);

        jLabel2.setText("Version");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel2, gridBagConstraints);

        jLabel3.setText("Executable");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel3, gridBagConstraints);

        jLabel4.setText("Savegame Directory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel4, gridBagConstraints);

        jLabel5.setText("AddOn Directory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel5, gridBagConstraints);

        jLabel6.setText("Managed AddOn Directory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel6, gridBagConstraints);

        jLabel7.setText("Managed Deactivated AddOn Directory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(txtManagedDeactivatedAddOnDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(txtManagedAddOnDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(txtAddOnDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(txtSavegameDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(txtExecutable, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(txtVersion, gridBagConstraints);

        txtHomeDir.setMinimumSize(new java.awt.Dimension(100, 24));
        txtHomeDir.setPreferredSize(new java.awt.Dimension(250, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(txtHomeDir, gridBagConstraints);

        btHomeDir.setText(INSTALLATIONFORM_BROWSE);
        btHomeDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btHomeDirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 9, 0, 6);
        add(btHomeDir, gridBagConstraints);

        btExecutable.setText(INSTALLATIONFORM_BROWSE);
        btExecutable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btExecutableActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 9, 0, 6);
        add(btExecutable, gridBagConstraints);

        btSavegameDir.setText(INSTALLATIONFORM_BROWSE);
        btSavegameDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSavegameDirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 9, 0, 6);
        add(btSavegameDir, gridBagConstraints);

        btAddOnDir.setText(INSTALLATIONFORM_BROWSE);
        btAddOnDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddOnDirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 9, 0, 6);
        add(btAddOnDir, gridBagConstraints);

        btManagedAddOnDir.setText(INSTALLATIONFORM_BROWSE);
        btManagedAddOnDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btManagedAddOnDirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 9, 0, 6);
        add(btManagedAddOnDir, gridBagConstraints);

        btManagedDeactivatedAddOnDir.setText(INSTALLATIONFORM_BROWSE);
        btManagedDeactivatedAddOnDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btManagedDeactivatedAddOnDirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 9, 6, 6);
        add(btManagedDeactivatedAddOnDir, gridBagConstraints);

        jLabel8.setText("Deactivated AddOn Directory");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel8, gridBagConstraints);

        btDeactivatedAddOnDir.setText(INSTALLATIONFORM_BROWSE);
        btDeactivatedAddOnDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDeactivatedAddOnDirActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 9, 0, 6);
        add(btDeactivatedAddOnDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(txtDeactivatedAddOnDir, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        add(filler2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        add(filler3, gridBagConstraints);

        jLabel9.setText("Debug Console Protocol");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel9, gridBagConstraints);

        cbDCP.setText("Debug Capable");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(cbDCP, gridBagConstraints);

        jLabel10.setText("MQTT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 6, 0, 0);
        add(jLabel10, gridBagConstraints);

        cbUseMqtt.setText("use MQTT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(cbUseMqtt, gridBagConstraints);

        jLabel11.setText("Broker URL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 18, 0, 0);
        add(jLabel11, gridBagConstraints);

        jLabel12.setText("Username");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 18, 0, 0);
        add(jLabel12, gridBagConstraints);

        jLabel13.setText("Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 18, 0, 0);
        add(jLabel13, gridBagConstraints);

        jLabel14.setText("Prefix");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 18, 0, 0);
        add(jLabel14, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(txtMqttBrokerUrl, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(txtMqttUsername, gridBagConstraints);

        pfMqttPassword.setText("jPasswordField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(pfMqttPassword, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 6, 0);
        add(txtMqttPrefix, gridBagConstraints);

        btTest.setText("Test");
        btTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btTestActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 9, 6, 6);
        add(btTest, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    void maybeFillVersion(File homeDir) throws IOException {
        txtVersion.setText(Oolite.getVersionFromHomeDir(homeDir));
    }

    void maybeFillExecutable(File homeDir) {
        if (txtExecutable.getText().isBlank()) {
            File executable = Oolite.getExecutable(homeDir);

            if (executable.isFile()) {
                txtExecutable.setText(executable.getAbsolutePath());
            }
        }
    }

    void maybeFillSavegameDir(File homeDir) {
        if (txtSavegameDir.getText().isBlank()) {
            File d = Oolite.getSavegameDir(homeDir);
            if (d != null) {
                txtSavegameDir.setText(d.getAbsolutePath());
            }
        }
    }

    void maybeFillAddonDir(File homeDir) throws IOException {
        if (txtAddOnDir.getText().isBlank()) {
            File d = Oolite.getAddOnDir(homeDir);
            if (d != null) {
                txtAddOnDir.setText(d.getCanonicalPath());
            }

            if (txtDeactivatedAddOnDir.getText().isBlank()) {
                File dd = new File(d, "../DeactivatedAddOns");
                txtDeactivatedAddOnDir.setText(dd.getCanonicalPath());
            }
        }
    }

    void maybeFillManagedAddonDir(File homeDir) throws IOException {
        log.debug("maybeFillManagedAddonDir({})", homeDir);

        if (txtManagedAddOnDir.getText().isBlank()) {
            File d = Oolite.getManagedAddOnDir(homeDir);
            if (d != null) {
                txtManagedAddOnDir.setText(d.getAbsolutePath());

                if (txtManagedDeactivatedAddOnDir.getText().isBlank()) {
                    File dd = Oolite.getManagedDeactivatedAddOnDir(homeDir);
                    if (dd != null) {
                        txtManagedDeactivatedAddOnDir.setText(dd.getCanonicalPath());
                    }
                }
            }
        }
    }
    
    void maybeDetectDebugOXP(File homeDir) throws IOException {
        log.debug("maybeDetectDebugOXP({})", homeDir);
        
        File d = new File(Oolite.getAddOnDir(homeDir), "Basic-debug.oxp");
        cbDCP.setSelected(d.isDirectory());
    }
    
    /**
     * After a homeDir set, check which other fields we want to populate.
     * 
     * @param homeDir the home directory
     */
    void tryToFillOtherFields(File homeDir) {
        log.warn("tryToFillOtherFields({})", homeDir);
        try {
            maybeFillVersion(homeDir);
            maybeFillExecutable(homeDir);
            maybeFillSavegameDir(homeDir);
            maybeFillAddonDir(homeDir);
            maybeFillManagedAddonDir(homeDir);
            maybeDetectDebugOXP(homeDir);
        } catch (Exception e) {
            log.warn("Could not fill in other fields", e);
            JOptionPane.showMessageDialog(this, "Could not guess other values automatically. See logfile for more information.", INSTALLATIONFORM_WARNING, JOptionPane.WARNING_MESSAGE);
        }
    }

    private void btHomeDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btHomeDirActionPerformed
        log.debug("btHomeDirActionPerformed({})", evt);

        try {
            String dir = txtHomeDir.getText();
            if (dir == null || dir.trim().isEmpty()) {
                System.getProperty(INSTALLATIONFORM_USER_HOME);
            }

            JFileChooser jfc = new JFileChooser(new File(dir));
            jfc.setDialogTitle("Select Oolite Home Directory...");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showDialog(this, INSTALLATIONFORM_SELECT) == JFileChooser.APPROVE_OPTION) {
                txtHomeDir.setText(jfc.getSelectedFile().getAbsolutePath());
                tryToFillOtherFields(jfc.getSelectedFile());
            }
        } catch (Exception e) {
            log.error("Could not set home dir", e);
            JOptionPane.showMessageDialog(this, "Could not set home directory. See logfile for more information.", INSTALLATIONFORM_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btHomeDirActionPerformed

    private void btExecutableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btExecutableActionPerformed
        log.debug("btExecutableActionPerformed({})", evt);

        try {
            String dir = txtExecutable.getText();
            if (dir == null || dir.trim().isEmpty()) {
                System.getProperty(INSTALLATIONFORM_USER_HOME);
            }

            JFileChooser jfc = new JFileChooser(new File(dir));
            jfc.setDialogTitle("Select Oolite Executable...");
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (jfc.showDialog(this, INSTALLATIONFORM_SELECT) == JFileChooser.APPROVE_OPTION) {
                txtExecutable.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Could not set executable", e);
            JOptionPane.showMessageDialog(this, "Could not set executable. See logfile for more information.", INSTALLATIONFORM_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btExecutableActionPerformed

    private void btSavegameDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSavegameDirActionPerformed
        log.debug("btSavegameDirActionPerformed({})", evt);

        try {
            String dir = txtSavegameDir.getText();
            if (dir == null || dir.trim().isEmpty()) {
                System.getProperty(INSTALLATIONFORM_USER_HOME);
            }

            JFileChooser jfc = new JFileChooser(new File(dir));
            jfc.setDialogTitle("Select Oolite Savegame Directory...");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showDialog(this, INSTALLATIONFORM_SELECT) == JFileChooser.APPROVE_OPTION) {
                txtSavegameDir.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Could not set savegame dir", e);
            JOptionPane.showMessageDialog(this, "Could not set savegame dir. See logfile for more information.", INSTALLATIONFORM_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btSavegameDirActionPerformed

    private void btAddOnDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddOnDirActionPerformed
        log.debug("btAddOnDirActionPerformed({})", evt);

        try {
            String dir = txtAddOnDir.getText();
            if (dir == null || dir.trim().isEmpty()) {
                System.getProperty(INSTALLATIONFORM_USER_HOME);
            }

            JFileChooser jfc = new JFileChooser(new File(dir));
            jfc.setDialogTitle("Select Oolite AddOn Directory...");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showDialog(this, INSTALLATIONFORM_SELECT) == JFileChooser.APPROVE_OPTION) {
                txtAddOnDir.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Could not set addon dir", e);
            JOptionPane.showMessageDialog(this, "Could not set addon dir. See logfile for more information.", INSTALLATIONFORM_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btAddOnDirActionPerformed

    private void btManagedAddOnDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btManagedAddOnDirActionPerformed
        log.debug("btManagedAddOnDirActionPerformed({})", evt);

        try {
            String dir = txtManagedAddOnDir.getText();
            if (dir == null || dir.trim().isEmpty()) {
                System.getProperty(INSTALLATIONFORM_USER_HOME);
            }

            JFileChooser jfc = new JFileChooser(new File(dir));
            jfc.setDialogTitle("Select Oolite Managed AddOn Directory...");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showDialog(this, INSTALLATIONFORM_SELECT) == JFileChooser.APPROVE_OPTION) {
                txtManagedAddOnDir.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Could not set managed addon dir", e);
            JOptionPane.showMessageDialog(this, "Could not set managed addon directory. See logfile for more information.", INSTALLATIONFORM_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btManagedAddOnDirActionPerformed

    private void btManagedDeactivatedAddOnDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btManagedDeactivatedAddOnDirActionPerformed
        log.debug("btManagedDeactivatedAddOnDirActionPerformed({})", evt);

        try {
            String dir = txtManagedDeactivatedAddOnDir.getText();
            if (dir == null || dir.trim().isEmpty()) {
                System.getProperty(INSTALLATIONFORM_USER_HOME);
            }

            JFileChooser jfc = new JFileChooser(new File(dir));
            jfc.setDialogTitle("Select Oolite Managed but Deactivated AddOn Directory...");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showDialog(this, INSTALLATIONFORM_SELECT) == JFileChooser.APPROVE_OPTION) {
                txtManagedDeactivatedAddOnDir.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Could not set managed deactivated addon dir", e);
            JOptionPane.showMessageDialog(this, "Could not set managed deactivated addon dir. See logfile for more information.", INSTALLATIONFORM_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btManagedDeactivatedAddOnDirActionPerformed

    private void btDeactivatedAddOnDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDeactivatedAddOnDirActionPerformed
        log.debug("btDeactivatedAddOnDirActionPerformed({})", evt);

        try {
            String dir = txtDeactivatedAddOnDir.getText();
            if (dir == null || dir.trim().isEmpty()) {
                System.getProperty(INSTALLATIONFORM_USER_HOME);
            }

            JFileChooser jfc = new JFileChooser(new File(dir));
            jfc.setDialogTitle("Select Oolite deactivated AddOn Directory...");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showDialog(this, INSTALLATIONFORM_SELECT) == JFileChooser.APPROVE_OPTION) {
                txtDeactivatedAddOnDir.setText(jfc.getSelectedFile().getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Could not set deactivated addon dir", e);
            JOptionPane.showMessageDialog(this, "Could not set deactivated addon dir. See logfile for more information.", INSTALLATIONFORM_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btDeactivatedAddOnDirActionPerformed

    private void btTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btTestActionPerformed

        try {
            char[] password = null;
            if (passwordDirty) {
                password = pfMqttPassword.getPassword();
            } else {
                password = data.getMqtt().getPassword();
            }
        
            MqttUtil.testConnection(
                txtMqttBrokerUrl.getText(),
                txtMqttUsername.getText(),
                password,
                txtMqttPrefix.getText()
            );
            
            JOptionPane.showMessageDialog(this, "Sent MQTT test message.", INSTALLATIONFORM_SUCCESS, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            log.error("Could not send test message", e);
            JOptionPane.showMessageDialog(this, "Could not send test message.", INSTALLATIONFORM_ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btTestActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddOnDir;
    private javax.swing.JButton btDeactivatedAddOnDir;
    private javax.swing.JButton btExecutable;
    private javax.swing.JButton btHomeDir;
    private javax.swing.JButton btManagedAddOnDir;
    private javax.swing.JButton btManagedDeactivatedAddOnDir;
    private javax.swing.JButton btSavegameDir;
    private javax.swing.JButton btTest;
    private javax.swing.JCheckBox cbDCP;
    private javax.swing.JCheckBox cbUseMqtt;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPasswordField pfMqttPassword;
    private javax.swing.JTextField txtAddOnDir;
    private javax.swing.JTextField txtDeactivatedAddOnDir;
    private javax.swing.JTextField txtExecutable;
    private javax.swing.JTextField txtHomeDir;
    private javax.swing.JTextField txtManagedAddOnDir;
    private javax.swing.JTextField txtManagedDeactivatedAddOnDir;
    private javax.swing.JTextField txtMqttBrokerUrl;
    private javax.swing.JTextField txtMqttPrefix;
    private javax.swing.JTextField txtMqttUsername;
    private javax.swing.JTextField txtSavegameDir;
    private javax.swing.JTextField txtVersion;
    // End of variables declaration//GEN-END:variables
}
