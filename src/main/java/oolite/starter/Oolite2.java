/*
 */

package oolite.starter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import oolite.starter.model.Installation;
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

    private List<Oolite.OoliteListener> listeners;
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
                    }
                }
            };
            this.configuration.addPropertyChangeListener(this.configurationListener);
        }
        status = Status.uninitialized;
    }

    void fireActivatedInstallation(Installation installation) {
        for (Oolite.OoliteListener l: listeners) {
            l.activatedInstallation(installation);
        }
    }
    
    /**
     * Installs filesystem watchers for the currently active installation.
     */
    void installWatchers() throws IOException {
        Installation i = configuration.getActiveInstallation();
        
        List<String> directories = new ArrayList<String>();
        directories.add(i.getAddonDir());
        directories.add(i.getDeactivatedAddonDir());
        directories.add(i.getManagedAddonDir());
        directories.add(i.getManagedDeactivatedAddonDir());
        directories.add(i.getSavegameDir());
        
        WatchService watchService = FileSystems.getDefault().newWatchService();
        for (String directory: directories) {
            Path path = Paths.get(directory);

            try {
                WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                log.debug("key {}", key);
            } catch (IOException e) {
                log.error("Cannot watch {}", directory);
            }
            
            // start a thread to query the watchservice and fire events
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
            }
        }).start();
    }
}
