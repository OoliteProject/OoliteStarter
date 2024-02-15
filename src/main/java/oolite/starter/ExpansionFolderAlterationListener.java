/*
 */

package oolite.starter;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This FileAlterationListener can check for changes
 * in Oolite addons folders (AddOns or ManagedAddOns)
 * and send events to rescan or remove an expansion.
 * Changes in expansion folders (OXZ or OXP) are sent to
 * registered listeners once the filesystem has stabilized
 * (change events have ebbed off).
 * 
 * @author hiran
 */
public class ExpansionFolderAlterationListener implements FileAlterationListener {
    private static final Logger log = LogManager.getLogger();

    public interface ExpansionFolderChangedListener {
        
        /**
         * Invoked when there were changes for an expansion folder.
         * 
         * @param files the list of expansion folders that were detected
         */
        public void foldersChanged(List<File> files);
    }
    
    private File directoryFile;
    private Path directoryPath;
    
    private Instant scanStart;
    private Timer timer;
    private List<File> changedExpansions;
    private List<ExpansionFolderChangedListener> listeners = new ArrayList<>();
    
    /**
     * Creates a new instance.
     * 
     * @param directory the directoryFile to watch
     */
    public ExpansionFolderAlterationListener(File directory) {
        log.debug("ExpansionFolderAlterationListener({})", directory);
        this.directoryFile = directory;
        this.directoryPath = directory.toPath();
        this.changedExpansions = new ArrayList<>();
        
        timer = new Timer(5000, (ae) -> {
            log.debug("timer fired");
            
            synchronized (changedExpansions) {
                // fire off all the changed expansions
                List<File> changes = changedExpansions;
                changedExpansions = new ArrayList<>();

                if (changes != null && !changes.isEmpty()) {
                    log.trace("need to fire {}", changes);
                    fireFoldersChanged(changes);
                }
            }
        });
        timer.setRepeats(false);
    }
    
    /**
     * From a file that is created/changed/deleted, detect the expansion location.
     * Expansions may be sitting in subdirectories, but not nested.
     * For OXPs file changes within the OXP can be detected.
     * 
     * @param file the file that was changed
     * @return the expansion that it belongs to
     */
    protected File detectExpansion(File file) {
        log.trace("detectExpansion({})", file);
        Path p = file.toPath();
        Path rel = directoryPath.relativize(p);

        File result = file;
        
        for (int i= 0; i< rel.getNameCount(); i++) {
            Path element = rel;
            if (i>0) {
                element = rel.subpath(0, i);
            }
            String elementStr = element.toString();
            log.trace("element {}: {}", i, element);
            
            if (elementStr.endsWith(".oxz")) {
                return directoryPath.resolve(element).toFile();
            } else if (elementStr.endsWith(".oxp")) {
                return directoryPath.resolve(element).toFile();
            }
        }
        
        return null;
    }
    
    protected void collectExpansion(File file) {
        File f = detectExpansion(file);
        if (f != null) {
            synchronized (changedExpansions) {
                if (!changedExpansions.contains(f)) {
                    changedExpansions.add(f);
                }
            }
        }
        timer.restart();
    }
    
    /**
     * Returns the directoryFile to watch.
     * 
     * @return the directoryFile
     */
    public File getDirectory() {
        return directoryFile;
    }

    @Override
    public void onDirectoryChange(File file) {
        log.debug("onDirectoryChange({})", file);
        collectExpansion(file);
    }

    @Override
    public void onDirectoryCreate(File file) {
        log.debug("onDirectoryCreate({})", file);
        collectExpansion(file);
    }

    @Override
    public void onDirectoryDelete(File file) {
        log.debug("onDirectoryDelete({})", file);
        collectExpansion(file);
    }

    @Override
    public void onFileChange(File file) {
        log.debug("onFileChange({})", file);
        collectExpansion(file);
    }

    @Override
    public void onFileCreate(File file) {
        log.debug("onFileCreate({})", file);
        collectExpansion(file);
    }

    @Override
    public void onFileDelete(File file) {
        log.debug("onFileDelete({})", file);
        collectExpansion(file);
    }

    @Override
    public void onStart(FileAlterationObserver fao) {
        //log.trace("onStart({})", fao);
        scanStart = Instant.now();
    }

    @Override
    public void onStop(FileAlterationObserver fao) {
        //log.trace("onStop({})", fao);
        log.trace("Scanned in {}", Duration.between(scanStart, Instant.now()));
    }

    /**
     * Registers a listener to receive folder changed events.
     * 
     * @param listener the listener to register
     */
    public void addExpansionFolderChangedListener(ExpansionFolderChangedListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Unregisters a listener from receiving folder changed events.
     * 
     * @param listener the listener to unregister
     */
    public void removeExpansionFolderChangedListener(ExpansionFolderChangedListener listener) {
        listeners.remove(listener);
    }
    
    protected void fireFoldersChanged(List<File> files) {
        for (ExpansionFolderChangedListener listener: listeners) {
            listener.foldersChanged(files);
        }
    }
}
