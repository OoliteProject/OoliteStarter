/*
 */
package oolite.starter;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import oolite.starter.model.Installation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author hiran
 */
public class Configuration {
    private static final Logger log = LogManager.getLogger();
    
    private static final String CONF_NO_ACTIVE_INSTALLATION = "No active installation";
    public static final Color COLOR_ATTENTION = new Color(160, 80, 0);
    private static final String DEFAULT_URL = "https://addons.oolite.space/api/1.0/overview/";

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /**
     * The list of installations on this system.
     */
    private List<Installation> installations;
    
    /**
     * The one active installation.
     */
    private Installation activeInstallation;
    
    private List<URL> expansionManagerURLs;
    
    private Duration updateCheckInterval;
    
    /**
     * Flag to indicate if after load/save the configuration was changed.
     */
    private boolean dirty;

    /**
     * Creates a new Configuration instance.
     * Finds the platform specific configuration file to initialize.
     */
    public Configuration() throws MalformedURLException {
        expansionManagerURLs = new ArrayList<>();
        expansionManagerURLs.add(new URL(DEFAULT_URL));
        
        installations = new ArrayList<>();
        updateCheckInterval = Duration.ofDays(7);
    }

    /**
     * Creates a new Configuration instance.
     * Loads the given configuration file to initialize.
     * 
     * @param the configuration file to load
     */
    public Configuration(File f) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        this();
        
        this.loadConfiguration(f);
    }

    /**
     * Returns the minimum duration between update checks.
     * @return the duration
     */
    public Duration getUpdateCheckInterval() {
        return updateCheckInterval;
    }
    
    /**
     * Loads the configuration from the given configuration file.
     * 
     * @param f the file to load
     */
    public final void loadConfiguration(File f) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilder db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        Document doc = db.parse(f);
        XPath xpath = XPathFactory.newDefaultInstance().newXPath();
        
        dirty = false;
        
        // load expansion manager URLs
        NodeList nl = (NodeList)xpath.evaluate("/OoliteStarter/ExpansionManager/manifestUrl", doc, XPathConstants.NODESET);
        ArrayList<URL> urls = new ArrayList<>();
        for (int i = 0; i< nl.getLength(); i++) {
            Element eExpansionManagerUrl = (Element)nl.item(i);
            urls.add(new URL(eExpansionManagerUrl.getTextContent()));
        }
        expansionManagerURLs = urls;
        
        try {
            String i = xpath.evaluate("/OoliteStarter/ExpansionManager/updateCheckInterval", doc);
            this.updateCheckInterval = Duration.parse(i);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Still using default udpate interval.", e);
            } else {
                log.info("Still using default udpate interval: {}", e.getMessage());
            }
        }
        
        // load installations
        nl = (NodeList)xpath.evaluate("/OoliteStarter/Installations/Installation", doc, XPathConstants.NODESET);
        ArrayList<Installation> insts = new ArrayList<>();
        for (int i = 0; i< nl.getLength(); i++) {
            Element eInstallation = (Element)nl.item(i);
            Installation inst = new Installation();
            inst.setHomeDir(xpath.evaluate("HomeDir", eInstallation));
            inst.setVersion(xpath.evaluate("Version", eInstallation));
            inst.setExcecutable(xpath.evaluate("Executable", eInstallation));
            inst.setSavegameDir(xpath.evaluate("SaveGameDir", eInstallation));
            inst.setAddonDir(xpath.evaluate("AddonDir", eInstallation));
            inst.setDeactivatedAddonDir(xpath.evaluate("DeactivatedAddonDir", eInstallation));
            inst.setManagedAddonDir(xpath.evaluate("ManagedAddonDir", eInstallation));
            inst.setManagedDeactivatedAddonDir(xpath.evaluate("ManagedDeactivatedAddonDir", eInstallation));
            insts.add(inst);
            
            if ("true".equals(eInstallation.getAttribute("active"))) {
                activeInstallation = inst;
            }
        }

        List<Installation> oldInstallations = installations;
        installations = insts;
        pcs.firePropertyChange("installations", oldInstallations, insts);
    }
    
    /**
     * Stores configuration data to the given file.
     * 
     * @param the file to store into
     */
    public final void saveConfiguration(File f) throws ParserConfigurationException, TransformerException, MalformedURLException {
        DocumentBuilder db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        Element root = doc.createElement("OoliteStarter");
        root.setAttribute("stored", Instant.now().toString());
        root.setAttribute("starter-version", getClass().getPackage().getImplementationVersion());
        root.setAttribute("logging", new File(new File(System.getProperty("user.home")), ".Oolite/Logs").getAbsolutePath());
        doc.appendChild(root);
        
        // add Expansion Manager URLs
        Element expansionManager = doc.createElement("ExpansionManager");
        for (URL url: getExpansionManagerURLs()) {
            Element eUrl = doc.createElement("manifestUrl");
            eUrl.setTextContent(url.toString());
            expansionManager.appendChild(eUrl);
            
            Element eUpdateCheckInterval = doc.createElement("updateCheckInterval");
            eUpdateCheckInterval.setTextContent(String.valueOf(this.updateCheckInterval));
            expansionManager.appendChild(eUpdateCheckInterval);
        }
        root.appendChild(expansionManager);

        // add installations
        Element eInstallations = doc.createElement("Installations");
        for (Installation installation: installations) {
            Element eInstallation = doc.createElement("Installation");
            
            eInstallation.setAttribute("active", String.valueOf( installation == activeInstallation ));
            
            Element e = doc.createElement("HomeDir");
            e.setTextContent(installation.getHomeDir());
            eInstallation.appendChild(e);
            
            e = doc.createElement("Version");
            e.setTextContent(installation.getVersion());
            eInstallation.appendChild(e);

            e = doc.createElement("Executable");
            e.setTextContent(installation.getExcecutable());
            eInstallation.appendChild(e);
            
            e = doc.createElement("SaveGameDir");
            e.setTextContent(installation.getSavegameDir());
            eInstallation.appendChild(e);
            
            e = doc.createElement("AddonDir");
            e.setTextContent(installation.getAddonDir());
            eInstallation.appendChild(e);
            
            e = doc.createElement("DeactivatedAddonDir");
            e.setTextContent(installation.getDeactivatedAddonDir());
            eInstallation.appendChild(e);
            
            e = doc.createElement("ManagedAddonDir");
            e.setTextContent(installation.getManagedAddonDir());
            eInstallation.appendChild(e);
            
            e = doc.createElement("ManagedDeactivatedAddonDir");
            e.setTextContent(installation.getManagedDeactivatedAddonDir());
            eInstallation.appendChild(e);
            
            eInstallations.appendChild(eInstallation);
        }
        root.appendChild(eInstallations);
        
        TransformerFactory tf = TransformerFactory.newDefaultInstance();
        tf.setAttribute("indent-number", 4);
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(doc), new StreamResult(f));
        
        dirty = false;
    }
    
    /**
     * Returns the installations list.
     * 
     * @return the list
     */
    public List<Installation> getInstallations() {
        return installations;
    }
    
    /**
     * Activates the configuration with given identifier.
     * 
     * @param installationId the installation to activate
     */
    public void activateInstallation(String installationId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Activates the given installation.
     * 
     * @param installation the installation to activate. Null if no installation is chosen
     */
    public void activateInstallation(Installation installation) {
        if (installation == null) {
            Installation oldInstallation = activeInstallation;
            activeInstallation = null;
            
            dirty = true;
            
            PropertyChangeEvent pce = new PropertyChangeEvent(this, "activeInstallation", oldInstallation, installation);
            for (PropertyChangeListener pcl: pcs.getPropertyChangeListeners()) {
                pcl.propertyChange(pce);
            }
            // for some reason this simple call was not enough: pcs.firePropertyChange("activeInstallation", oldInstallation, installation);
            
            return;
        }
        
        if (installations.contains(installation)) {
            if (activeInstallation != installation) {
                Installation oldInstallation = installation;
                activeInstallation = installation;
                
                dirty = true;
                
                log.debug("firing propertyChange activeInstallation to {} listeners: {}", pcs.getPropertyChangeListeners().length, pcs.getPropertyChangeListeners());
                PropertyChangeEvent pce = new PropertyChangeEvent(this, "activeInstallation", oldInstallation, installation);
                for (PropertyChangeListener pcl: pcs.getPropertyChangeListeners()) {
                    pcl.propertyChange(pce);
                }
                // for some reason this simple call was not enough: pcs.firePropertyChange("activeInstallation", oldInstallation, installation);
            }
        } else {
            throw new IllegalArgumentException("installation must be known");
        }
    }

    /**
     * Returns the currently active installation.
     * 
     * @return the installation
     */
    public Installation getActiveInstallation() {
        return activeInstallation;
    }
    
    /**
     * Returns the directory where Oolite stores it's save games.
     * 
     * @return the directory
     */
    public File getSaveGameDir() {
        if (activeInstallation == null) {
            throw new IllegalStateException(CONF_NO_ACTIVE_INSTALLATION);
        }
        return new File(activeInstallation.getSavegameDir());
    }
    
    /**
     * Returns the command to execute for running Oolite.
     * 
     * @return the command
     */
    public String getOoliteCommand() {
        if (activeInstallation == null) {
            throw new IllegalStateException(CONF_NO_ACTIVE_INSTALLATION);
        }
        return activeInstallation.getExcecutable();
    }
    
    /**
     * Returns a list of Expansion Manager's manifest URLs.
     * 
     * @return the list of URLs
     * @throws MalformedURLException something went wrong
     */
    public List<URL> getExpansionManagerURLs() throws MalformedURLException {
        return expansionManagerURLs;
    }
    
    /**
     * Returns the directory where we hide unmanaged expansions from Oolite.
     * 
     * @return the directory
     */
    public File getDeactivatedAddonsDir() {
        if (activeInstallation == null) {
            throw new IllegalStateException(CONF_NO_ACTIVE_INSTALLATION);
        }
        return new File(activeInstallation.getDeactivatedAddonDir());
    }
    
    /**
     * Returns the directory where we hide managed expansions from Oolite.
     * 
     * @return the directory
     */
    public File getManagedDeactivatedAddonsDir() {
        if (activeInstallation == null) {
            throw new IllegalStateException(CONF_NO_ACTIVE_INSTALLATION);
        }
        return new File(activeInstallation.getManagedDeactivatedAddonDir());
    }
    
    /**
     * Returns the directory where Oolite finds managed addons.
     * 
     * @return the directory
     */
    public File getManagedAddonsDir() {
        if (activeInstallation == null) {
            throw new IllegalStateException(CONF_NO_ACTIVE_INSTALLATION);
        }
        return new File(activeInstallation.getManagedAddonDir());
    }
    
    /**
     * Returns the directory where Oolite finds unmanaged addons.
     * 
     * @return the directory
     */
    public File getAddonsDir() {
        if (activeInstallation == null) {
            throw new IllegalStateException(CONF_NO_ACTIVE_INSTALLATION);
        }
        String addonDir =  activeInstallation.getAddonDir();
        if (addonDir == null) {
            throw new IllegalStateException("active installation has no addonDir");
        }
        return new File(addonDir);
    }
    
    /**
     * Returns all directories where Oolite stores groups of OXPs.
     * That means it is both activated/deactivated for both managed/unmanaged addons.
     * @see https://wiki.alioth.net/index.php/OXP#Locating_your_AddOns_folder
     * 
     * @return the directory
     */
    public List<File> getAddonDirs() {
        if (activeInstallation == null) {
            throw new IllegalStateException(CONF_NO_ACTIVE_INSTALLATION);
        }

        List<File> result = new ArrayList<>();

        result.add(getManagedAddonsDir());
        result.add(getManagedDeactivatedAddonsDir());
        
        result.add(getAddonsDir());
        result.add(getDeactivatedAddonsDir());
        
        return result;
    }
    
    /**
     * Adds a PropertyChangeListener to be notified upon property changes.
     * 
     * @param l the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        log.debug("addPropertyChangeListener({})", l);
        pcs.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a PropertyChangeListener to no longer be notified upon property changes.
     * 
     * @param l the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        log.debug("removePropertyChangeListener({})", l);
        pcs.removePropertyChangeListener(l);
    }
    
    /**
     * Indicates if after load/save the configuration was changed.
     *
     * @return true if the configuration was changed
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * Sets the dirty flag. The flag can only be raised. To lower it,
     * run the save method.
     *
     * @param dirty the intended state of the flag
     */
    public void setDirty(boolean dirty) {
        this.dirty |= dirty;
    }

    /**
     * Returns the file where to expect the configuration. Usually points to the
     * user's home directory.
     * 
     * @return the file
     */
    public File getDefaultConfigFile() {
        return new File(System.getProperty("user.home") + File.separator + ".oolite-starter.conf");
    }
}
