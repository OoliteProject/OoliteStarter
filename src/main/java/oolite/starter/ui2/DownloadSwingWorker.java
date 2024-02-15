/*
 */

package oolite.starter.ui2;

import java.awt.Component;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.swing.SwingWorker;
import oolite.starter.Oolite2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class DownloadSwingWorker extends SwingWorker<Void, Void> {
    private static final Logger log = LogManager.getLogger();

    private Component parent;
    private String downloadUrl;
    private Path destinationDir;
    private Oolite2 oolite;
    
    /**
     * Creates a new RemoveSwingWorker.
     * 
     * @param expansion the expansion to remove
     */
    public DownloadSwingWorker(Component parent, String downloadUrl, Path destinationDir, Oolite2 oolite) {
        log.debug("DownloadSwingWorker(...)");
        this.parent = parent;
        this.downloadUrl = downloadUrl;
        this.destinationDir = destinationDir;
        this.oolite = oolite;
    }

    @Override
    protected Void doInBackground() throws Exception {
        log.debug("doInBackground()");

        URL url = new URL(downloadUrl);
        Path outpath = destinationDir.resolve("filename");
        
        try (InputStream in = url.openConnection().getInputStream(); OutputStream out = Files.newOutputStream(outpath, StandardOpenOption.CREATE_NEW)) {
            in.transferTo(out);
        }
        
        return null;
    }

    @Override
    protected void done() {
        log.debug("done()");
        
        // todo: ask Oolite to scan the path in question
        oolite.fire();
    }

}
