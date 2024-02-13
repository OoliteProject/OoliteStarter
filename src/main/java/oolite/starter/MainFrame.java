/*
 */
package oolite.starter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import oolite.starter.generic.ScrollablePanel;
import oolite.starter.model.Installation;
import oolite.starter.model.ProcessData;
import oolite.starter.ui.AboutPanel;
import oolite.starter.ui.ExpansionsPanel;
import oolite.starter.ui.InstallationsPanel;
import oolite.starter.ui.MrGimlet;
import oolite.starter.ui.SplashPanel;
import oolite.starter.ui.StartGamePanel;
import oolite.starter.ui2.ExpansionPanel;
import oolite.starter.ui2.ExpansionsPanel2;
import oolite.starter.ui2.StartGamePanel2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * This is the main entry point for Oolite.
 * 
 * @author hiran
 */
public class MainFrame extends javax.swing.JFrame {
    private static final Logger log = LogManager.getLogger();
    private static final Logger sysout = LogManager.getLogger("SysOut");

    private static transient JFrame newSplash;
    
    private transient Oolite oolite;
    private transient Oolite2 oolite2;
    private transient Configuration configuration;

    private StartGamePanel sgp;
    private StartGamePanel2 sgp2;
    private ExpansionsPanel esp;
    private ExpansionsPanel2 esp2;
    private oolite.starter.ui.ExpansionPanel ep;
    private ExpansionPanel ep2;
    private InstallationsPanel ip;
    
    /**
     * Creates new form MainFrame.
     */
    public MainFrame() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        initComponents();
        setInstallationTitle(null);
        setIconImage(new ImageIcon(getClass().getResource("/images/Mr_Gimlet_transparent.png")).getImage());

        File confFile = new File(System.getProperty("oolite.starter.configuration", System.getProperty("user.home") + "/.oolite-starter.conf"));
        if (confFile.exists()) {
            configuration = new Configuration(confFile);
        } else {
            String msg = String.format("<html><p>Heho, Kid! You've got a problem here that is technical, not financial.</p><p>The configuration file %s was not found.</p><p>I’m a busy frog, I can’t stay here all day to watching you poke buttons. So let's use defaults.</p></html>", confFile.getAbsolutePath());
            log.warn(msg);
            
            MrGimlet.showMessage(null, msg, 0);
            
            configuration = new Configuration();
        }

        oolite = new Oolite();
        oolite.setConfiguration(configuration);
        
        oolite2 = new Oolite2();
        oolite2.setConfiguration(configuration);
        oolite2.addOoliteListener(new Oolite2.OoliteListener() {
            private static Logger log = LogManager.getLogger();
            
            @Override
            public void statusChanged(Oolite2.Status status) {
                log.warn("statusChanged({})", status);
            }

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
        });
        
        oolite2.initialize();
        oolite2.installWatchers();

        configuration.addPropertyChangeListener(pce -> {
            if (pce.getSource() instanceof Configuration) {
                log.debug("Configuration change {}", pce);
                Installation i = (Installation)pce.getNewValue();
                setInstallationTitle(i);

                jTabbedPane1.setEnabledAt(0, i != null);
                jTabbedPane1.setEnabledAt(1, i != null);
            }
        });
        setInstallationTitle(configuration.getActiveInstallation());
        
        sgp = new StartGamePanel();
        sgp.setOolite(oolite);
        
        sgp2 = new StartGamePanel2();
        sgp2.setOolite(oolite);
        sgp2.setBorder(new LineBorder(Color.red));
        jTabbedPane1.add(sgp);

        ExpansionManager em = ExpansionManager.getInstance();
        em.start();

        esp = new ExpansionsPanel();
        esp.setOolite(oolite);
        esp.setBorder(new LineBorder(Color.orange));
        em.addExpansionManagerListener(esp);
//        jTabbedPane1.add(esp);

        JPanel expansions = new JPanel();
        expansions.setLayout(new BorderLayout());
        jTabbedPane1.add(expansions, "Expansions");

        esp2 = new ExpansionsPanel2();
        esp2.setOolite(oolite);
        expansions.add(esp2, BorderLayout.CENTER);

        ep = new oolite.starter.ui.ExpansionPanel();

        ep2 = new ExpansionPanel();
        esp2.addSelectionListener(ep2);
        expansions.add(ep2, BorderLayout.SOUTH);

        ip = new InstallationsPanel();
        ip.setConfiguration(configuration);
        ip.setBorder(new LineBorder(Color.blue));
        jTabbedPane1.add(ip);

        AboutPanel ap = new AboutPanel("text/html", getClass().getResource("/about.html"));
        jTabbedPane1.add("About", ap);

        //getContentPane().removeAll();
        //getContentPane().setLayout(new BorderLayout());
        ScrollablePanel content = new ScrollablePanel();
        content.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        content.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
        //content.add(sgp);
        content.add(sgp2);
        content.add(esp);
        //content.add(esp2);
        content.add(ep);
        //content.add(ep2);
        //content.add(ip);
        JScrollPane jsp = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setName("Experimental");
        jTabbedPane1.add(jsp);
        //sgp.setVisible(false);
        sgp2.setVisible(false);
        esp.setVisible(false);
        //esp2.setVisible(false);
        ep.setVisible(false);
        //ep2.setVisible(false);
        //ip.setVisible(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (configuration.isDirty()) {
//                    jTabbedPane1.setSelectedIndex(2);
//                    
                    // show dialog
                    int choice = MrGimlet.showConfirmation(MainFrame.this, "<html>Your configuration changed since it was last saved.<p>Would you like to save now?</html>");
                    switch (choice) {
                        case JOptionPane.YES_OPTION:
                            saveConfiguration();
                            dispose();
                            System.exit(0);
                            break;
                        case JOptionPane.NO_OPTION:
                            dispose();
                            System.exit(0);
                            break;
                        case JOptionPane.CANCEL_OPTION:
                        default:
//                            // we have DefaultCloseOperation set to DO_NOTHING.
//                            // doing nothing will keep the window
                    }
                } else {
                    // we have DefaultCloseOperation set to DO_NOTHING.
                    dispose();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
            
        });
    }

    /**
     * Return the configuraton.
     * 
     * @return  the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }
    
    private void saveConfiguration() {
        try {
            configuration.saveConfiguration( configuration.getDefaultConfigFile() );
        } catch (Exception e) {
            log.error("Could not save", e);
            JOptionPane.showMessageDialog(this, "Could not save. Check logfile.", "Error", JOptionPane.ERROR_MESSAGE);
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        miLaunchGamePanel = new javax.swing.JCheckBoxMenuItem();
        miLaunchGamePanel2 = new javax.swing.JCheckBoxMenuItem();
        miExpansionsPanel = new javax.swing.JCheckBoxMenuItem();
        miExpansionsPanel2 = new javax.swing.JCheckBoxMenuItem();
        miExpansionDetails = new javax.swing.JCheckBoxMenuItem();
        miExpansionDetails2 = new javax.swing.JCheckBoxMenuItem();
        miInstallationsPanel = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(800, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jMenu1.setText("Menu");

        miLaunchGamePanel.setText("Launch Game Panel");
        miLaunchGamePanel.setEnabled(false);
        miLaunchGamePanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miLaunchGamePanelActionPerformed(evt);
            }
        });
        jMenu1.add(miLaunchGamePanel);

        miLaunchGamePanel2.setText("Launch Game Panel 2");
        miLaunchGamePanel2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miLaunchGamePanel2ActionPerformed(evt);
            }
        });
        jMenu1.add(miLaunchGamePanel2);

        miExpansionsPanel.setText("Expansions Panel");
        miExpansionsPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miExpansionsPanelActionPerformed(evt);
            }
        });
        jMenu1.add(miExpansionsPanel);

        miExpansionsPanel2.setText("Expansions Panel 2");
        miExpansionsPanel2.setEnabled(false);
        miExpansionsPanel2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miExpansionsPanel2ActionPerformed(evt);
            }
        });
        jMenu1.add(miExpansionsPanel2);

        miExpansionDetails.setText("Expansion Details");
        miExpansionDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miExpansionDetailsActionPerformed(evt);
            }
        });
        jMenu1.add(miExpansionDetails);

        miExpansionDetails2.setText("Expansion Details 2");
        miExpansionDetails2.setEnabled(false);
        miExpansionDetails2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miExpansionDetails2ActionPerformed(evt);
            }
        });
        jMenu1.add(miExpansionDetails2);

        miInstallationsPanel.setText("Installations Panel");
        miInstallationsPanel.setEnabled(false);
        miInstallationsPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miInstallationsPanelActionPerformed(evt);
            }
        });
        jMenu1.add(miInstallationsPanel);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        log.debug("formWindowClosing(...)");
        // trigger Oolite shutdown
        if (oolite.isRunning()) {
            oolite.terminate();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("thread interrupted while waiting for Oolite to shutdown", e);
            }
        }
    }//GEN-LAST:event_formWindowClosing

    private void miLaunchGamePanel2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miLaunchGamePanel2ActionPerformed
        log.debug("miLaunchGamePanel2ActionPerformed({})", evt);
        sgp2.setVisible(miLaunchGamePanel2.isSelected());
    }//GEN-LAST:event_miLaunchGamePanel2ActionPerformed

    private void miExpansionsPanel2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miExpansionsPanel2ActionPerformed
        log.debug("miExpansionsPanel2ActionPerformed({})", evt);
        esp2.setVisible(miExpansionsPanel2.isSelected());
    }//GEN-LAST:event_miExpansionsPanel2ActionPerformed

    private void miExpansionDetails2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miExpansionDetails2ActionPerformed
        log.debug("miExpansionDetails2ActionPerformed({})", evt);
        ep2.setVisible(miExpansionDetails2.isSelected());
    }//GEN-LAST:event_miExpansionDetails2ActionPerformed

    private void miInstallationsPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miInstallationsPanelActionPerformed
        log.debug("miExpansionDetailsActionPerformed({})", evt);
        ip.setVisible(miInstallationsPanel.isSelected());
    }//GEN-LAST:event_miInstallationsPanelActionPerformed

    private void miLaunchGamePanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miLaunchGamePanelActionPerformed
        log.debug("miLaunchGamePanelActionPerformed({})", evt);
        sgp.setVisible(miLaunchGamePanel.isSelected());
    }//GEN-LAST:event_miLaunchGamePanelActionPerformed

    private void miExpansionsPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miExpansionsPanelActionPerformed
        log.debug("miExpansionsPanelActionPerformed({})", evt);
        esp.setVisible(miExpansionsPanel.isSelected());
    }//GEN-LAST:event_miExpansionsPanelActionPerformed

    private void miExpansionDetailsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miExpansionDetailsActionPerformed
        log.debug("miExpansionDetailsActionPerformed({})", evt);
        ep.setVisible(miExpansionDetails.isSelected());
    }//GEN-LAST:event_miExpansionDetailsActionPerformed

    private static void customizeSplashScreen() {
        SplashScreen ss = SplashScreen.getSplashScreen();
        if (ss != null) {
            Graphics2D g = ss.createGraphics();

            String text = MainFrame.class.getPackage().getImplementationTitle()
                    + " " + MainFrame.class.getPackage().getImplementationVersion();

            g.setFont(g.getFont().deriveFont(Font.BOLD, 22.0f));
            g.setColor(Color.white);
            g.drawString(text, 31, 51);
            g.setColor(new Color(46, 64, 82));
            g.drawString(text, 30, 50);
            ss.update();
        }
        
        newSplash = new JFrame();
        newSplash.setUndecorated(true);
        newSplash.setIconImage(new ImageIcon(MainFrame.class.getResource("/images/Mr_Gimlet_transparent.png")).getImage());
        //newSplash.add(new SplashPanel(new ImageIcon(MainFrame.class.getResource("/images/OoliteStarter_Splashscreen_640x360.png"))));
        ImageIcon screen = new ImageIcon(MainFrame.class.getResource("/images/Digebiti.png"));
        String motd = "This is an experimental prerelease. Use the menu to turn on UI elements.";
        SplashPanel sp = new SplashPanel(screen, motd);
        newSplash.add(sp);
        newSplash.pack();
        newSplash.setLocationRelativeTo(null);
        newSplash.setVisible(true);
    }
    
    /**
     * Sets the window title based on the installation version and OoliteStarter version.
     * 
     * @param installation the active installation
     */
    public void setInstallationTitle(Installation installation) {
        String iversion = "";
        if (installation != null) {
            iversion = installation.getHomeDir() + " " + installation.getVersion() + " - ";
        }
        String product = MainFrame.class.getPackage().getImplementationTitle() + " " + MainFrame.class.getPackage().getImplementationVersion();
        setTitle(iversion + product);
    }
    
    private static void installShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdownhook") {
            @Override
            public void run() {
                log.info("{} {}  shutdown", MainFrame.class.getPackage().getImplementationTitle(), MainFrame.class.getPackage().getImplementationVersion());
            }
            
        });
    }
    
    private static void setLookAndFeel() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            
            javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            
            // control frame title color
            javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);            
            javax.swing.JDialog.setDefaultLookAndFeelDecorated(true);
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            log.info("Could not set look and feel", ex);
        }
        //</editor-fold>
    }
    
    private static void startupUI() {
        log.info("{} {}  starting up...", MainFrame.class.getPackage().getImplementationTitle(), MainFrame.class.getPackage().getImplementationVersion());
        
        customizeSplashScreen();
        installShutdownHook();
        setLookAndFeel();

        
        /* Create and display the form */
        new SwingWorker<MainFrame, Object>() {
            
            private GithubVersionChecker gvc;
            
            @Override
            protected MainFrame doInBackground() throws Exception {
                Instant i0 = Instant.now();

                log.info("Initialize UI...");
                MainFrame mf = new MainFrame();
                mf.pack();
                mf.setLocationRelativeTo(null);

                Instant i1 = Instant.now();

                log.info("Check for new version...");
                gvc = new GithubVersionChecker();
                gvc.setUpdateCheckInterval(mf.getConfiguration().getUpdateCheckInterval());
                gvc.init();

                Duration spent = Duration.between(i0, i1);
                long spentMillis = spent.toMillis();

                if (spentMillis < 4000) {
                    Thread.sleep(4000 - spentMillis);
                }
                
                return mf;
            }

            @Override
            protected void done() {
                try {

                    MainFrame mf = get();
                    mf.setLocationRelativeTo(newSplash);
                    mf.setVisible(true);
                    if (newSplash != null) {
                        newSplash.setVisible(false);
                        newSplash.dispose();
                        newSplash = null;
                    }

                    if (mf.configuration.getInstallations().isEmpty()) {
                        // point user to creating an active installation
                        mf.jTabbedPane1.setEnabledAt(0, false);
                        mf.jTabbedPane1.setEnabledAt(1, false);
                        mf.jTabbedPane1.setSelectedIndex(2);

                        StringBuilder message = new StringBuilder("<html>");
                        message.append("<p>I see a lot of blanks on this here board... Kid, you gotta do something about it.</p>");
                        message.append("<p>Have at least one active Oolite version. You need one. It's pretty much compulsory.<br/>");
                        message.append("Hit the Scan or Add button and fill in the form, at least once to add Oolite versions.");
                        message.append("</html>");

                        MrGimlet.showMessage(mf.getRootPane(), message.toString(), 0);
                    } else if (mf.configuration.getActiveInstallation() == null) {
                        // point user to creating an active installation
                        mf.jTabbedPane1.setEnabledAt(0, false);
                        mf.jTabbedPane1.setEnabledAt(1, false);
                        mf.jTabbedPane1.setSelectedIndex(2);

                        StringBuilder message = new StringBuilder("<html>");
                        message.append("<p>Much better, son. But there is still something to do:</p>");
                        message.append("<p>Decide for one of your Oolite versions. Otherwise this Starter would not know what to do.<br/>");
                        message.append("<p>Choose one from the list and click Select.");
                        message.append("</html>");
 
                        MrGimlet.showMessage(mf.getRootPane(), message.toString(), 0);
                    } else {
                        gvc.maybeAnnounceUpdate(mf.getRootPane());
                    }

                } catch (InterruptedException e) {
                    log.fatal("Interrupted", e);
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.fatal("Could not initialize UI", e);
                    JOptionPane.showMessageDialog(null, e.getClass().getName() + ":\n" + e.getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }

        }.execute();
    }
    
    private static void showHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("OoliteStarter <options>", options);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (log.isInfoEnabled()) {
            log.info("Args: {}", ((Object)args));
            log.info("JVM: {} {}", System.getProperty("java.runtime.name"), Runtime.version());
            log.info("OS: {} {} {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"));
        }
        
        Options options = new Options();
        options.addOption("h", "help", false, "show usage help");
        options.addOption("v", "version", false, "print program version");
        
        CommandLine cmd = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            log.error("Could not parse command line", e);
            showHelp(options);
            System.exit(2);
        }
        
        if (cmd.hasOption("help")) {
            showHelp(options);
            System.exit(1);
        } else if (cmd.hasOption("version")) {
            String msg = "%s %s".formatted(MainFrame.class.getPackage().getImplementationTitle(), MainFrame.class.getPackage().getImplementationVersion());
            sysout.info(msg);
            System.exit(1);
        } else {
            // no special option - let's startup the UI
            startupUI();
        }
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JCheckBoxMenuItem miExpansionDetails;
    private javax.swing.JCheckBoxMenuItem miExpansionDetails2;
    private javax.swing.JCheckBoxMenuItem miExpansionsPanel;
    private javax.swing.JCheckBoxMenuItem miExpansionsPanel2;
    private javax.swing.JCheckBoxMenuItem miInstallationsPanel;
    private javax.swing.JCheckBoxMenuItem miLaunchGamePanel;
    private javax.swing.JCheckBoxMenuItem miLaunchGamePanel2;
    // End of variables declaration//GEN-END:variables
}
