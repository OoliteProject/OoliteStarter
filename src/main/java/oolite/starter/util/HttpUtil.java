/*
 */

package oolite.starter.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class HttpUtil {
    private static final Logger log = LogManager.getLogger();

    private HttpUtil() {
        // just utility methods
    }
    
    /**
     * Downloads a file from a URL.
     * 
     * @param url where to download from
     * @param file where to store the data
     * @throws IOException something went wrong
     */
    public static void downloadUrl(URL url, File file) throws IOException {
        log.debug("downloadUrl({}, {})", url, file);
        
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setReadTimeout(5000);

        int status = conn.getResponseCode();
        log.info("HTTP status for {}: {}", url, status);
        
        while (status != HttpURLConnection.HTTP_OK) {
            String newUrl = conn.getHeaderField("Location");
            conn = (HttpURLConnection)new URL(newUrl).openConnection();
            conn.setReadTimeout(5000);
            status = conn.getResponseCode();
            log.info("HTTP status for {}: {}", newUrl, status);
        }
        
        try (InputStream in = conn.getInputStream()) {
            FileUtils.copyToFile(in, file);
        }
    }
    
}
