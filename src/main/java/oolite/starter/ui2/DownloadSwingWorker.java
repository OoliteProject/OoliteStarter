/*
 */

package oolite.starter.ui2;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.swing.SwingWorker;
import oolite.starter.Oolite2;
import oolite.starter.model.Expansion;
import oolite.starter.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class DownloadSwingWorker extends SwingWorker<Void, Void> {
    private static final Logger log = LogManager.getLogger();

    private String downloadUrl;
    private Path destinationDir;
    private Oolite2 oolite;
    
    /**
     * Creates a new RemoveSwingWorker.
     * 
     * @param expansion the expansion to remove
     */
    public DownloadSwingWorker(Component parent, String downloadUrl, Path destinationDir, Oolite2 oolite) {
        log.debug("DownloadSwingWorker{}, {}, {}, {})", parent, downloadUrl, destinationDir, oolite);
        this.downloadUrl = downloadUrl;
        this.destinationDir = destinationDir;
        this.oolite = oolite;
    }

    @Override
    protected Void doInBackground() throws Exception {
        log.debug("doInBackground()");

        URL url = new URL(downloadUrl);
        File tempFile = File.createTempFile("OoliteStarter-", ".OXP");
        Path outpath = tempFile.toPath();
        
        try {
            HttpURLConnection httpurlconnection = (HttpURLConnection)url.openConnection();
            httpurlconnection.connect();
            log.info("HTTP response code {}", httpurlconnection.getResponseCode());
            try (InputStream in = httpurlconnection.getInputStream(); OutputStream out = Files.newOutputStream(outpath, StandardOpenOption.TRUNCATE_EXISTING)) {
                long bytes = in.transferTo(out);
                log.info("Downloaded {} bytes to {}", bytes, outpath);
            } 
            
            // file is downloaded. Do we need to unzip?
            if (Util.isZipFile(tempFile)) {
                log.warn("Downloaded file {} is zip", tempFile);
                
                // is it in OXZ format? If yes, just move.
                Expansion e = oolite.getExpansionFrom(tempFile);
                if (e != null) {
                    // we have an OXZ, just move it!
                    log.warn("Downloaded file {} is OXZ", tempFile);
                } else {
                    // we have a normal archive, just unzip
                    log.warn("Downloaded file {} is simple ZIP", tempFile);
                    
                    String filename = url.getFile();
                    filename = filename.substring(filename.lastIndexOf("/")+1);
                    
                    Path outputFile = destinationDir.resolve(filename);
                    try (InputStream in = new FileInputStream(tempFile)) {
                        Util.unzip(in, outputFile.toFile());
                    }
                }
            } else {
                log.warn("Not a ZIP file. What next?");
            }
            
        } catch (Exception e) {
            log.error("Could not download", e);
            throw e;
        } finally {
            Files.delete(tempFile.toPath());
            log.warn("Deleted {}", tempFile);
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
