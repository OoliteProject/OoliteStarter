/*
 */

package oolite.starter.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;
import oolite.starter.util.WinRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ScanOolitesSwingWorker extends SwingWorker<List<String>, String> {
    private static final Logger log = LogManager.getLogger();
    
    private static String scanOoliteSwingWorkerDefaultInstalldir = "C:\\Oolite";
    
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

    /**
     * Thrown if problems during scanning are detected.
     */
    public static class ScanOoliteException extends Exception {

        /**
         * Creates a new exception.
         */
        public ScanOoliteException() {
        }

        /**
         * Creates a new exception.
         * 
         * @param message an additional message
         */
        public ScanOoliteException(String message) {
            super(message);
        }

        /**
         * Creates a new exception.
         * 
         * @param message an additional message
         * @param cause an additional cause
         */
        public ScanOoliteException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Creates a new exception.
         * 
         * @param cause an additional cause
         */
        public ScanOoliteException(Throwable cause) {
            super(cause);
        }
        
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
    
    /**
     * Returns true if the file should not be scanned.
     * 
     * It boils down to skipping symlinks and the skip patterns.
     * 
     * @param f the file to check
     * @return true if it should be skipped - false otherwise
     */
    private boolean shouldSkip(File f) {
        if (Files.isSymbolicLink(f.toPath())) {
            return true;
        }
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
    
    /**
     * Scans a file/directory recursively, not going into already scanned
     * areas again.
     * 
     * Scanned areas are stored in the scannedFiles list.
     * 
     * @param f the file to scan
     * @throws IOException something went wrong
     */
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
        skipPatterns.add(Pattern.compile(".*/cwd/proc/.*"));
        skipPatterns.add(Pattern.compile(".*/cwd/sys/class/.*"));
        skipPatterns.add(Pattern.compile(".*/cwd/sys/devices/.*"));
        skipPatterns.add(Pattern.compile(".*/cwd/sys/dev/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/class/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/devices/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/dev/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/bus/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/block/.*"));
        skipPatterns.add(Pattern.compile(".*/sys/module/.*"));

        List<File> preferredLocations = new ArrayList<>();
        switch (oolite.starter.util.Util.getOperatingSystemType()) {
            case LINUX: // Linux version
                goodPatterns.add(Pattern.compile("(.*/oolite.app)/oolite-wrapper"));
                preferredLocations.add(new File(new File(System.getProperty("user.home")), "GNUstep/Applications"));
                break;
            case MACOS: // Mac OS version
                goodPatterns.add(Pattern.compile("(.*\\.app)/Contents/MacOS/Oolite"));
                break;
            case WINDOWS: // Windows version
                // find the likely Oolite installation from Registry
                try {
                    log.warn("Reading registry...");
                    String path = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, "HKLM\\SOFTWARE\\WOW6432Node\\Oolite", "Install_Dir");
                    log.warn("Seems we found {}", path);
                    goodPatterns.add(Pattern.compile(path));
                } catch (Exception e) {
                    log.warn("Could not read from registry", e);
                }
                
                goodPatterns.add(Pattern.compile("(.*\\\\oolite.app)\\\\oolite.exe"));
                skipPatterns.add(Pattern.compile("C:\\\\Windows.*", Pattern.CASE_INSENSITIVE));
                preferredLocations.add(new File(scanOoliteSwingWorkerDefaultInstalldir));
                break;
            default:
                break;
        }
        preferredLocations.add(new File(System.getProperty("user.home")));
        preferredLocations.addAll(Arrays.asList(File.listRoots()));

        log.info("Scanning for Oolite in {}", preferredLocations);
        try {
            result = new ArrayList<>();

            totalFiles += File.listRoots().length + 1;

            for (File f: preferredLocations) {
                scan(f);
            }

            return result;
        } catch (Exception e) {
            log.error("could not scan", e);
            fireSetNote(String.format("Error during scan: %s", e.getMessage()));
            throw new ScanOoliteException("could not scan", e);
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
            
}
