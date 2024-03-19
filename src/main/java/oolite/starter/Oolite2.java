/*
 */

package oolite.starter;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.module.ModuleDescriptor;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import oolite.starter.model.Expansion;
import oolite.starter.model.ExpansionReference;
import oolite.starter.model.Installation;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * This class shall model the Oolite expansion state on disk.
 * As soon as it receives a configuration (or the configuration is changed)
 * it throws away an existing cache and scans the disk.
 * It also registers as FileSystemListener to directly react if files are
 * installed, changed or removed. This will apply both to expansions and
 * savegames.
 * 
 * @author hiran
 */
public class Oolite2 {
    private static final Logger log = LogManager.getLogger();

    public enum Status {
        UNINITIALIZED,
        INITIALIZING,
        INITIALIZED,
        RESCANNING
    }
    
    public static interface OoliteListener extends Oolite.OoliteListener {
        
        /**
         * Invoked whenever the Oolite2 status changes.
         * 
         * @param status the new status
         */
        public void statusChanged(Status status);
    }

    public class OoliteExpansionListModel extends AbstractListModel<Expansion> {
        private static final Logger log = LogManager.getLogger();

        @Override
        public int getSize() {
            log.debug("getSize()");
            
            if (expansions == null) {
                log.trace("expansions list is null");
                return 0;
            }
            
            log.trace("return {}", expansions.size());
            return expansions.size();
        }

        @Override
        public Expansion getElementAt(int i) {
            log.debug("getElementAt({})", i);
            
            if (expansions == null) {
                return null;
            }
            
            return expansions.get(i);
        }
        
        protected void fire() {
            log.debug("fire()");
            fireContentsChanged(this, 0, Math.max(0, getSize()-1));
        }
    }

    private final List<OoliteListener> listeners = new ArrayList<>();
    private Configuration configuration;
    private Status status = Status.UNINITIALIZED;
    private PropertyChangeListener configurationListener;
    private FileAlterationMonitor monitor;
    private final Oolite oolite;
    
    private List<Expansion> expansions;
    private final List<WeakReference<OoliteExpansionListModel>> ooliteExpansionListModels;
    
    /**
     * Creates a new Oolite2 driver.
     */
    public Oolite2() {
        log.debug("Oolite2()");
        oolite = new Oolite();
        
        ooliteExpansionListModels = new ArrayList<>();
    }
    
    /**
     * Returns a new managed ListModel for Expansions.
     * 
     * @return the model
     */
    public OoliteExpansionListModel getExpansionListModel() {
        log.debug("getExpansionListModel()");
        
        OoliteExpansionListModel result = new OoliteExpansionListModel();
        ooliteExpansionListModels.add(new WeakReference<>(result));
        
        return result;
    }
    
    /**
     * Fires a model changed event to all managed list models.
     */
    public void fire() {
        ArrayList<WeakReference<OoliteExpansionListModel>> refs = new ArrayList<>(ooliteExpansionListModels);
        for (WeakReference<OoliteExpansionListModel> ref: refs) {
            OoliteExpansionListModel lm = ref.get();
            
            if (lm != null) {
                // reference is valid. Fire event.
                lm.fire();
            } else {
                // reference is expired. Remove it from list.
                ooliteExpansionListModels.remove(ref);
            }
        }
    }
    
    /**
     * Returns this driver's status.
     * 
     * @return the current status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Sets the configuration to be used for this Oolite/installation environment.
     * @param configuration 
     */
    public void setConfiguration(Configuration configuration) {
        if (this.configuration != null) {
            this.configuration.removePropertyChangeListener(configurationListener);
        }
        this.configuration = configuration;
        
        if (configuration != null) {
            this.configurationListener = pce -> {
                log.debug("configurationListener propertyChange({})", pce);
                if ("activeInstallation".equals(pce.getPropertyName())) {
                    Installation i = (Installation)pce.getNewValue();
                    status = Status.UNINITIALIZED;
                    fireActivatedInstallation(i);
                    fireStatusChanged();

                    initialize();
                }
            };
            this.configuration.addPropertyChangeListener(this.configurationListener);
        }

        oolite.setConfiguration(configuration);

        status = Status.UNINITIALIZED;
        fireStatusChanged();
    }

    protected void fireActivatedInstallation(Installation installation) {
        for (Oolite.OoliteListener l: listeners) {
            l.activatedInstallation(installation);
        }
    }
    
    protected void fireStatusChanged() {
        List<OoliteListener> ls = new ArrayList<>(listeners);
        for (OoliteListener l: ls) {
            l.statusChanged(status);
        }
    }
    
    /**
     * Installs filesystem watchers for the currently active installation.
     */
    public void installWatchers() {
        log.debug("installWatchers()");
        
        Installation i = configuration.getActiveInstallation();
        
        ExpansionFolderAlterationListener.ExpansionFolderChangedListener efcl = files -> {
            log.debug("ExpansionFolderChangedListener called with {}", files);
            for (File f: files) {
                rescan(f);
            }
            fire();
        };
        monitor = new FileAlterationMonitor(1000);

        File dir = new File(i.getManagedAddonDir());
        FileAlterationObserver observer = new FileAlterationObserver(dir);
        ExpansionFolderAlterationListener efal = new ExpansionFolderAlterationListener(dir);
        efal.addExpansionFolderChangedListener(efcl);
        observer.addListener(efal);
        monitor.addObserver(observer);

        dir = new File(i.getAddonDir());
        observer = new FileAlterationObserver(dir);
        efal = new ExpansionFolderAlterationListener(dir);
        efal.addExpansionFolderChangedListener(efcl);
        observer.addListener(efal);
        monitor.addObserver(observer);
        
        try {
            monitor.start();
        } catch (Exception e) {
            log.error("Could not start file monitor", e);
        }
    }
    
    /**
     * Removes all filesystem watchers.
     */
    public void removeWatchers() throws Exception {
        if (monitor != null) {
            monitor.stop();
            monitor = null;
        }
    }
    
    /**
     * Initializes the driver, which means to trigger a full scan.
     * This scan will run the background. Check the driver's state
     * to see it is still ongoing. Add a listener to be informed
     * about state changes.
     */
    public synchronized void initialize() {
        if (status != Status.UNINITIALIZED) {
            throw new IllegalStateException();
        }
        
        new Thread( () ->  {
            try {
                status = Status.INITIALIZING;
                SwingUtilities.invokeLater(this::fireStatusChanged);

                // remove filesyste watchers
                removeWatchers();

                // scan directories
                expansions = oolite.getAllExpansions();

                // install filesystem watchers
                installWatchers();

                // fire update events to clients
                fire();

                status = Status.INITIALIZED;
                SwingUtilities.invokeLater(Oolite2.this::fireStatusChanged);
            } catch (Exception e) {
                log.error("Problem in deferred initialization", e);
            }
        }).start();
    }

    @Override
    public String toString() {
        return "Oolite2{" + "status=" + status + '}';
    }
    
    /**
     * Registers a listener.
     * 
     * @param listener the listener to register
     */
    public void addOoliteListener(OoliteListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Unregisters a listener.
     * 
     * @param listener the listener to unregister
     */
    public void removeOoliteListener(OoliteListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Returns the currently active installation.
     * 
     * @return the installation
     */
    public Installation getActiveInstallation() {
        return oolite.getActiveInstallation();
    }
    
    void validateDependencies() {
        log.warn("validateDependencies()");
        
        oolite.validateCompatibility(expansions);
        oolite.validateConflicts(expansions);
        oolite.validateDependencies2(expansions);
        oolite.validateUpdates(expansions);
    }
    
    /**
     * Checks the status for one expansion.
     * 
     * @param e 
     */
    public void rescan(Expansion e) {
        log.warn("rescan({})", e);
        
        if (e.isLocal()) {
            rescan(e.getLocalFile());
        } else {
            // no longer local? Was it removed?
            validateDependencies();
            fire();
        }
    }
    
    /**
     * Checks the status for one expansion's directory.
     * @param f 
     */
    public void rescan(File f) {
        log.warn("rescan({})", f);
        
        status = Status.RESCANNING;
        fireStatusChanged(); // notify listeners
        
        if (!f.exists()) {
            // looks like something was deleted
            // let's find the expansion in our list and mark it up accordingly
            String fstr = f.getAbsolutePath();
            
            for (Expansion e: expansions) {
                if (e.getLocalFile() != null) {
                    String gstr = e.getLocalFile().getAbsolutePath();
                    if (fstr.equals(gstr)) {
                        log.warn("Need to remove expansion {}", e);
                        // todo: really remove that expansion, or mark it as not installed
                    }
                }
            }
        } else {
            log.warn("File exists!");
            try {
                Expansion newExpansion = oolite.getExpansionFrom(f);
                if (newExpansion != null) {
                    // replace the right one
                }
            } catch (Exception e) {
                log.warn("could not read {}", f, e);
            }
        }

        // recompute dependencies
        validateDependencies();
        
        status = Status.INITIALIZED;
        fireStatusChanged(); // notify listeners
        
        // notify managed list models
        fire();
    }
    
    /**
     * Returns the Expansion matching the reference.
     * 
     * @param er the reference to match
     * @return the expansion, or null if not found
     */
    public Expansion getExpansionByExpansionReference(ExpansionReference er) {
        log.warn("getExpansionByExpansionReference({})", er);
        if (er == null) {
            throw new IllegalArgumentException("er must not be null");
        }
        
        String id = er.getName();
        String versionStr = er.getName();
        int idx = id.indexOf("@");
        if (idx >= 0) {
            id = id.substring(0, idx);
            versionStr = versionStr.substring(idx+1);
        }
        
        for (Expansion e: expansions) {
            // check if this expansion matches
            if (id.equals(e.getIdentifier())) {
                // identifier matches. Check version information
                if (versionStr.equals(e.getVersion())) {
                    // exact match found!
                    return e;
                }
                
                log.warn("Expansion {} needed in {} but found {}", id, versionStr, e.getVersion());

                // else we need to check if we found a newer version
                ModuleDescriptor.Version erv = ModuleDescriptor.Version.parse(versionStr);
                ModuleDescriptor.Version ev = ModuleDescriptor.Version.parse(e.getVersion());
                if (ev.compareTo(erv) >= 0) {
                    // found greater or equal - let's use it
                    return e;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Investigates whether a File holds an expansion and returns it.
     * 
     * @param f the file to investigate
     * @return the expansion found, or null
     */
    public Expansion getExpansionFrom(File f) throws ParserConfigurationException, SAXException, XPathExpressionException {
        return oolite.getExpansionFrom(f);
    }

    /**
     * Returns expansions that are updates for already installed ones..
     * 
     * @return the list of updates
     */
    public List<Expansion> getUpdates() {
        if (expansions==null) {
            log.warn("Cannot make out updates in status {}", status);
            return new ArrayList<>();
        }
        return expansions.stream()
                .filter(exp -> exp.getEMStatus().isUpdate())
                .toList();
    }

}
