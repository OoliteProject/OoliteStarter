/*
 */

package oolite.starter.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ScanOolitesSwingWorker extends SwingWorker<List<String>, String> {
    private static final Logger log = LogManager.getLogger();
    
    public interface ScanOoliteSwingWorkerListener {
        
        /**
         * Indicates the worker has started scanning.
         */
        public void startScan();
        
        /**
         * Indicates the worker has stopped scanning.
         */
        public void stopScan();
        
        /**
         * Adds a new installation (Oolite Home directory) to the listener.
         * 
         * @param s the path to the installation
         */
        public void addInstallation(String s);

        /**
         * Gives a quick message about scan progress.
         * 
         * @param s 
         */
        public void setNote(String s);
    }

    private List<Pattern> skipPatterns = new ArrayList<>();
    private List<Pattern> goodPatterns = new ArrayList<>();
    private List<String> result;
    private HashSet<String> scannedFiles;
    private int totalFiles;
    
    private List<ScanOoliteSwingWorkerListener> listeners = new ArrayList<>();
    
    /**
     * Creates a new instance and adds the listener.
     * 
     * @param listener the listener to be notified about progress
     */
    public ScanOolitesSwingWorker(ScanOoliteSwingWorkerListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Registers a listener for notification about progress.
     * 
     * @param listener the listener
     */
    public void addScanOoliteSwingWorkerListener(ScanOoliteSwingWorkerListener listener) {
        listeners.add(listener);
    }
            
    /**
     * Unregisters a listener from notification about progress.
     * 
     * @param listener the listener
     */
    public void removeScanOoliteSwingWorkerListener(ScanOoliteSwingWorkerListener listener) {
        listeners.remove(listener);
    }

    private void fireStartScan() {
        for (ScanOoliteSwingWorkerListener l: listeners) {
            l.startScan();
        }
    }

    private void fireStopScan() {
        for (ScanOoliteSwingWorkerListener l: listeners) {
            l.stopScan();
        }
    }
    
    private void fireSetNote(String s) {
        for (ScanOoliteSwingWorkerListener l: listeners) {
            l.setNote(s);
        }
    }
            
    private void fireAddInstallation(String s) {
        for (ScanOoliteSwingWorkerListener l: listeners) {
            l.addInstallation(s);
        }
    }
    
    private boolean shouldSkip(File f) {
        for (Pattern p: skipPatterns) {
            if (p.matcher(f.getAbsolutePath()).matches()) {
                return true;
            }
        }
        return false;
    }
    
    private void checkMatch(File f) {
        for (Pattern p: goodPatterns) {
            Matcher m = p.matcher(f.getAbsolutePath());
            if (m.matches()) {
                String s = m.group(1);
                result.add(s);

                // add to installations panel
                fireAddInstallation(s);
                publish(s);
            }
        }
    }
            
    private void scan(File f) throws IOException {
        log.trace("scan({})", f);
        log.trace("already scanned {}/{} files", scannedFiles.size(), totalFiles);

        publish (f.getAbsolutePath());
                
        if (scannedFiles.contains(f.getCanonicalPath())) {
            return;
        }
        scannedFiles.add(f.getCanonicalPath());

        if (shouldSkip(f)) {
            return;
        }
          
        checkMatch(f);
                        
        if (f.isDirectory()) {
            File[] entries = f.listFiles();
            if (entries != null) {
                totalFiles += entries.length;
                for (File entry: entries ) {
                    scan(entry);

                    if (isCancelled() ) {
                        return;
                    }
                }
            }
        }
    }
            
    /**
     * Entry point for this SwingWorker.
     * Scans the filesystem, then returns the collected results.
     */
    @Override
    protected List<String> doInBackground() throws Exception {
        log.debug("doInBackground()");
        fireStartScan();

        scannedFiles = new HashSet<>();

        skipPatterns.add(Pattern.compile("^/proc/.*"));
        skipPatterns.add(Pattern.compile("^/sys/.*"));
        skipPatterns.add(Pattern.compile(".*/proc/self/.*"));
        skipPatterns.add(Pattern.compile(".*/proc/thread-self/.*"));
        skipPatterns.add(Pattern.compile(".*/proc/\\d+/.*"));
        skipPatterns.add(Pattern.compile(".*/cwd/proc/.*/cwd/proc/.*"));
        skipPatterns.add(Pattern.compile(".*/cwd/sys/class/.*"));
        skipPatterns.add(Pattern.compile(".*/cwd/sys/devices/.*"));
        skipPatterns.add(Pattern.compile(".*/cwd/sys/dev/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/class/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/devices/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/dev/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/bus/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/block/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/module/.*"));

        // Linux version
        goodPatterns.add(Pattern.compile("(.*/oolite.app)/oolite-wrapper"));
        // Mac OS version
        goodPatterns.add(Pattern.compile("(.*\\.app)/Contents/MacOS/Oolite"));
        // Windows version
        goodPatterns.add(Pattern.compile("(.*\\\\oolite.app)\\\\oolite.exe"));

        try {
            result = new ArrayList<>();

            totalFiles += File.listRoots().length + 1;

            scan(new File(System.getProperty("user.home")));

            for(File f: File.listRoots()) {
                scan(f);
            }

            return result;
        } catch (Exception e) {
            log.error("could not scan", e);
            throw new Exception("could not scan", e);
        }
    }

    @Override
    protected void process(List<String> chunks) {
        log.trace("process({})", chunks);

        // can we read something from the amount of chunks?

        if (!chunks.isEmpty()) {
            fireSetNote(chunks.get(0));
        }
    }

    @Override
    protected void done() {
        log.debug("done()");
        fireStopScan();
        fireSetNote("Scanning finished.");

        log.debug("Found {} installations {}", result.size(), result);
    }
            
};
