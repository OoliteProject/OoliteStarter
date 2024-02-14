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
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
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
    
    public static interface OoliteListener extends Oolite.OoliteListener {
        
        /**
         * Invoked whenever the Oolite2 status changes.
         * 
         * @param status the new status
         */
        public void statusChanged(Status status);
    }
    
    private class Watcher implements Runnable {
        private static final Logger log = LogManager.getLogger();
        
        private boolean done = false;
        private Path addonDir;
        private Path deactivatedAddonDir;
        private Path managedAddonDir;
        private Path managedDeactivatedAddonDir;
        private Path savegameDir;
        
        public Watcher(Installation installation) {
            log.debug("Watcher({})", installation);
        
            this.addonDir = Paths.get(installation.getAddonDir()).toAbsolutePath();
            this.deactivatedAddonDir = Paths.get(installation.getDeactivatedAddonDir()).toAbsolutePath();
            this.managedAddonDir = Paths.get(installation.getManagedAddonDir()).toAbsolutePath();
            this.managedDeactivatedAddonDir = Paths.get(installation.getManagedDeactivatedAddonDir()).toAbsolutePath();
            this.savegameDir = Paths.get(installation.getSavegameDir()).toAbsolutePath();
        }

        @Override
        public void run() {
            log.debug("run()");

            List<Path> directories = new ArrayList<>();
            directories.add(addonDir);
            directories.add(deactivatedAddonDir);
            directories.add(managedAddonDir);
            directories.add(managedDeactivatedAddonDir);
            directories.add(savegameDir);
            
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                for (Path path: directories) {
                    if (path.toFile().exists()) {
                        WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                        log.debug("key {}", key);
                    } else {
                        log.info("skip path {}", path);
                    }
                }


                log.debug("start watching");
                while (!done) {
                    WatchKey key = null;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException ex) {
                        log.warn("interrupted", ex);
                        done = true;
                        return;
                    }

                    for (WatchEvent<?> event: key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        // This key is registered only
                        // for ENTRY_CREATE events,
                        // but an OVERFLOW event can
                        // occur regardless if events
                        // are lost or discarded.
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }
                        
                        processEvent(kind, ((Path)event.context()));
                    }
                    key.reset();
                }
            } catch (IOException e) {
                log.error("Error watching", e);
            }
            log.debug("done watching.");
        }
        
        public void stop() {
            log.debug("stop()");
            done = true;
        }
        
        protected void processEvent(WatchEvent.Kind<?> kind, Path path) {
            log.info("processEvent({} {})", kind.name(), path);
            
            if (path.startsWith(savegameDir)) {
                log.info("savegame {} {}", kind.name(), path);
            } else if (
                    path.startsWith(addonDir) ||
                    path.startsWith(managedAddonDir)
                    ) {
                log.info("expansion {} {}", kind.name(), path);
            } else {
                log.info("unknown {} {}", kind.name(), path);
            }
        }
    }

    private List<OoliteListener> listeners = new ArrayList<>();
    private Configuration configuration;
    private Status status = Status.uninitialized;
    private PropertyChangeListener configurationListener;
    private Watcher watcher;
    
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
        
        if (watcher != null) {
            watcher.stop();
        }
        
        watcher = new Watcher(i);
        new Thread(watcher).start();
        
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
