/*
 */

package oolite.starter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import oolite.starter.model.Installation;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        uninitialized,
        initializing,
        initialized
    }
    
    public static interface OoliteListener extends Oolite.OoliteListener {
        
        /**
         * Invoked whenever the Oolite2 status changes.
         * 
         * @param status the new status
         */
        public void statusChanged(Status status);
    }
    

    private List<OoliteListener> listeners = new ArrayList<>();
    private Configuration configuration;
    private Status status = Status.uninitialized;
    private PropertyChangeListener configurationListener;
    
    /**
     * Creates a new Oolite2 driver.
     */
    public Oolite2() {
        log.debug("Oolite2()");
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
            this.configurationListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent pce) {
                    log.debug("configurationListener propertyChange({})", pce);
                    if ("activeInstallation".equals(pce.getPropertyName())) {
                        Installation i = (Installation)pce.getNewValue();
                        status = Status.uninitialized;
                        fireActivatedInstallation(i);
                        fireStatusChanged();
                    }
                }
            };
            this.configuration.addPropertyChangeListener(this.configurationListener);
        }
        status = Status.uninitialized;
        fireStatusChanged();
    }

    protected void fireActivatedInstallation(Installation installation) {
        for (Oolite.OoliteListener l: listeners) {
            l.activatedInstallation(installation);
        }
    }
    
    protected void fireStatusChanged() {
        for (OoliteListener l: listeners) {
            l.statusChanged(status);
        }
    }
    
    /**
     * Installs filesystem watchers for the currently active installation.
     */
    public void installWatchers() throws IOException {
        log.debug("installWatchers()");
        
        Installation i = configuration.getActiveInstallation();
        
        FileAlterationMonitor monitor = new FileAlterationMonitor(1000);

        File dir = new File(i.getManagedAddonDir());
        FileAlterationObserver observer = new FileAlterationObserver(dir);
        observer.addListener(new ExpansionFolderAlterationListener(dir));
        monitor.addObserver(observer);

        dir = new File(i.getAddonDir());
        observer = new FileAlterationObserver(dir);
        observer.addListener(new ExpansionFolderAlterationListener(dir));
        monitor.addObserver(observer);
        
        try {
            monitor.start();
        } catch (Exception e) {
            log.error("Could not start file monitor", e);
        }
    }
    
    /**
     * Initializes the driver, which means to trigger a full scan.
     * This scan will run the background. Check the driver's state
     * to see it is still ongoing. Add a listener to be informed
     * about state changes.
     */
    public synchronized void initialize() {
        if (status != Status.uninitialized) {
            throw new IllegalStateException();
        }
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                status = Status.initializing;
                SwingUtilities.invokeLater(() -> {
                    fireStatusChanged();
                });
                
                // remove filesyste watchers
                // scan directories
                // install filesystem watchers

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    log.debug("sleep interrupted", ex);
                }
                
                // fire update events to clients
                
                status = Status.initialized;
                SwingUtilities.invokeLater(() -> {
                    fireStatusChanged();
                });
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
}
