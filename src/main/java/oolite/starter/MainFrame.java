/*
 */
package oolite.starter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.SwingWorker;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import oolite.starter.model.Command;
import oolite.starter.model.Expansion;
import oolite.starter.model.Installation;
import oolite.starter.model.ProcessData;
import oolite.starter.ui.AboutPanel;
import oolite.starter.ui2.FlavorsPanel;
import oolite.starter.ui.InstallationsPanel;
import oolite.starter.ui.MrGimlet;
import oolite.starter.ui.SplashPanel;
import oolite.starter.ui.StartGamePanel;
import oolite.starter.ui.Util;
import oolite.starter.ui2.ExpansionPanel;
import oolite.starter.ui2.ExpansionSetPanel;
import oolite.starter.ui2.ExpansionsPanel2;
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
    
    private static class InitFrameSwingWorker extends SwingWorker<MainFrame, Object> {
            
        private GithubVersionChecker gvc;
        private OoliteVersionChecker ovc;
        private JFrame newSplash;
        
        public InitFrameSwingWorker(JFrame newSplash) {
            this.newSplash = newSplash;
        }

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

            ovc = new OoliteVersionChecker();
            ovc.setUpdateCheckInterval(mf.getConfiguration().getUpdateCheckInterval());
            ovc.init();

            Duration spent = Duration.between(i0, i1);
            long spentMillis = spent.toMillis();

            if (spentMillis < 4000) {
                Thread.sleep(4000 - spentMillis);
            }

            return mf;
        }

        private boolean maybeAnnounceExpansionUpdate(MainFrame mf) {
            log.debug("maybeAnnounceExpansionUpdate(...)");
            List<Expansion> updates = mf.oolite2.getUpdates();

            if (!updates.isEmpty()) {
                List<Command> plan = mf.oolite.buildUpdateCommandList(mf.oolite2.getExpansions(), updates);
                // have user approve the plan
                if (JOptionPane.showConfirmDialog(mf, Util.createCommandListPanel(plan, "Updated expansions are available. Do you want to install them?"), "Confirm these actions...", JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                    // execute the plan
                    ExpansionManager.getInstance().addCommands(plan);
                    MrGimlet.showMessage(mf.getRootPane(), "Working on it...");
                }

                return true;
            } else {
                return false;
            }
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
                    log.info("No installation. MrGimlet point to installations");
                    
                    // point user to creating an active installation
                    mf.jTabbedPane1.setEnabledAt(0, false);
                    mf.jTabbedPane1.setEnabledAt(1, false);
                    mf.jTabbedPane1.setEnabledAt(2, false);
                    mf.jTabbedPane1.setEnabledAt(3, false);
                    mf.jTabbedPane1.setSelectedIndex(4);

                    StringBuilder message = new StringBuilder("<html>");
                    message.append("<p>I see a lot of blanks on this here board... Kid, you gotta do something about it.</p>");
                    message.append("<p>Have at least one active Oolite version. You need one. It's pretty much compulsory.<br/>");
                    message.append("Hit the Scan or Add button and fill in the form, at least once to add Oolite versions.");
                    message.append("</html>");

                    MrGimlet.showMessage(mf.getRootPane(), message.toString(), 0);
                } else if (!mf.isInstallationsValid()) {
                    log.info("Fishy installation. MrGimlet point to installations");

                    mf.jTabbedPane1.setSelectedIndex(4);

                    StringBuilder message = new StringBuilder("<html>");
                    message.append("<p>Hmmm. Something's fishy here.</p>");
                    message.append("<p>Is it possible some Oolite version changed meanwhile?<br>");
                    message.append("Better check.</p>");
                    message.append("</html>");

                    MrGimlet.showMessage(mf.getRootPane(), message.toString(), 0);
                } else if (mf.configuration.getActiveInstallation() == null) {
                    log.info("No active installation. MrGimlet point to installations");

                    // point user to creating an active installation
                    mf.jTabbedPane1.setEnabledAt(0, false);
                    mf.jTabbedPane1.setEnabledAt(1, false);
                    mf.jTabbedPane1.setEnabledAt(2, false);
                    mf.jTabbedPane1.setEnabledAt(3, false);
                    mf.jTabbedPane1.setSelectedIndex(4);

                    StringBuilder message = new StringBuilder("<html>");
                    message.append("<p>Much better, son. But there is still something to do:</p>");
                    message.append("<p>Decide for one of your Oolite versions. Otherwise this Starter would not know what to do.<br/>");
                    message.append("<p>Choose one from the list and click Select.");
                    message.append("</html>");

                    MrGimlet.showMessage(mf.getRootPane(), message.toString(), 0);
                } else {
                    boolean foundSomething = false;
                    // we always have an installation as the other case is above
                    
                    if (!foundSomething) {
                        // check for OoliteStarter version
                        foundSomething = gvc.maybeAnnounceUpdate(mf.getRootPane());
                    }

                    if (!foundSomething) {
                        // check for Oolite version
                        Installation i = mf.getConfiguration().getActiveInstallation();
                        foundSomething = ovc.maybeAnnounceUpdate(mf.getRootPane(), ModuleDescriptor.Version.parse(i.getVersion()));
                    }

                    if (foundSomething) {
                        log.trace("Notified user about upgrades");
                    }

                    if (!foundSomething) {
                        // check for expansion update
                        foundSomething = maybeAnnounceExpansionUpdate(mf);
                    }
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
    }            
    
    private transient Oolite oolite;
    private transient Oolite2 oolite2;
    private transient Configuration configuration;

    private StartGamePanel sgp;
    private ExpansionsPanel2 esp2;
    private ExpansionPanel ep2;
    private InstallationsPanel ip;
    
    private boolean expansionMangagerActive;
    private boolean ooliteActive;
    
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
            log.trace(msg);
            
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
                log.debug("statusChanged({})", status);
                
                ooliteActive = status == Oolite2.Status.INITIALIZING;
                updateBackgroundProcessIndicator();
            }

           /**
            * From Oolite2.OoliteListener.
            * @param message
            */
           @Override
           public void problemDetected(String message) {
               JOptionPane.showMessageDialog(MainFrame.this, message, "OoliteStarter", JOptionPane.ERROR_MESSAGE);
           }

            @Override
            public void launched(ProcessData pd) {
                log.debug("launched({})", pd);
            }

            @Override
            public void terminated() {
                log.debug("terminated()");
            }

            @Override
            public void activatedInstallation(Installation installation) {
                log.debug("activatedInstallation({})", installation);
            }
        });
        
        oolite2.initialize();

        configuration.addPropertyChangeListener(pce -> {
            if (pce.getSource() instanceof Configuration) {
                log.debug("Configuration change {}", pce);
                Installation i = (Installation)pce.getNewValue();
                setInstallationTitle(i);

                jTabbedPane1.setEnabledAt(0, i != null);
                jTabbedPane1.setEnabledAt(1, i != null);
                jTabbedPane1.setEnabledAt(2, i != null);
                jTabbedPane1.setEnabledAt(3, i != null);
            }
        });
        setInstallationTitle(configuration.getActiveInstallation());
        
        sgp = new StartGamePanel();
        sgp.setOolite(oolite, oolite2);
        jTabbedPane1.add(sgp);

        ExpansionManager em = ExpansionManager.getInstance();
        em.addExpansionManagerListener(new ExpansionManager.ExpansionManagerListener() {
            @Override
            public void updateStatus(ExpansionManager.Status status, List<Command> queue) {
                log.debug("updateStatus({}, {})", status, queue);
                
                expansionMangagerActive = status.activity() == ExpansionManager.Activity.PROCESSING;
                updateBackgroundProcessIndicator();
            }
        });
        em.start();

        JSplitPane expansions = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        jTabbedPane1.add(expansions, "Expansions");

        esp2 = new ExpansionsPanel2(oolite2);
        expansions.setTopComponent(esp2);

        ep2 = new ExpansionPanel();
        esp2.addSelectionListener(ep2);
        expansions.setBottomComponent(ep2);

        FlavorsPanel fp = new FlavorsPanel();
        fp.setOolite(oolite, oolite2);
        jTabbedPane1.add("Flavors", fp);
        
        ExpansionSetPanel esp = new ExpansionSetPanel();
        esp.setOolite(oolite, oolite2);
        jTabbedPane1.add(esp, "Expansion Set");
        
        ip = new InstallationsPanel();
        ip.setConfiguration(configuration);
        jTabbedPane1.add(ip);

        AboutPanel ap = new AboutPanel("text/html", getClass().getResource("/about.html"));
        jTabbedPane1.add("About", ap);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (configuration.isDirty()) {
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
                            // we have DefaultCloseOperation set to DO_NOTHING.
                            // doing nothing will keep the window
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
        
        jTabbedPane1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                log.info("stateChanged({})", ce);
                if (jTabbedPane1.getSelectedIndex() == 2) {
                    MrGimlet.showMessage(jTabbedPane1, "Flavours are collections of expansions which bend your Ooniverse in a particular way.<br/>These collections are not sacrosanct - use the Expansions tab to add or remove expansions until you have attained your desired utopia!<br/>You can save your collection by using the 'Expansion Set' tab.");
                }
            }
        });
    }
    
    /**
     * Checks if the configured installations still match the setup on disk.
     * 
     * @return true if good, false otherwise
     */
    protected boolean isInstallationsValid() {
        log.debug("isInstallationsValid()");

        for (Installation i: configuration.getInstallations()) {
            File f = new File(i.getExcecutable());
            if (!f.isFile()) {
                log.warn("File {} not found.", f);
                return false;
            }

            f = new File(i.getHomeDir());
            if (!f.isDirectory()) {
                log.warn("File {} not found.", f);
                return false;
            }
            
            try {
                String v = oolite.getVersionFromHomeDir(f);
                if (!i.getVersion().equals(v)) {
                    log.warn("Oolite declared as {} but found {}", i.getVersion(), v);
                    return false;
                }
            } catch (IOException e) {
                log.warn("Could not get version for {}", f, e);
            }
        }
        
        return true;
    }
    
    private void updateBackgroundProcessIndicator() {
        if (ooliteActive || expansionMangagerActive) {
            getContentPane().setEnabled(false);
            jProgressBar1.setIndeterminate(true);
            if (expansionMangagerActive) {
                int x = ExpansionManager.getInstance().getStatus().queueSize();
                jProgressBar1.setString("juggling %d expansions...".formatted(x));
            } else if (ooliteActive) {
                jProgressBar1.setString("rescanning...");
            }
            jProgressBar1.setVisible(true);
        } else {
            ooliteActive = false;
            jProgressBar1.setVisible(false);
            getContentPane().setEnabled(true);
        }
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
        jProgressBar1 = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(800, 600));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jProgressBar1.setFocusable(false);
        jProgressBar1.setIndeterminate(true);
        jProgressBar1.setStringPainted(true);
        getContentPane().add(jProgressBar1, java.awt.BorderLayout.PAGE_END);

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
        ImageIcon screen = new ImageIcon(MainFrame.class.getResource("/images/OoliteStarter_Splashscreen_800x450.png"));
        String motd = "";
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
        new InitFrameSwingWorker(newSplash).execute();
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
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
