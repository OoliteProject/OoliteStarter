/*
 */
package oolite.starter;

import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class Util {
    public static final Logger log = LogManager.getLogger();

    /**
     * Prevent creating instances.
     */
    private Util() {
    }
    
    /**
     * Returns the operating system's hostname.
     * 
     * @return the hostname
     */
    public static String getHostname() {
        try {
            return execReadToString("hostname");
        } catch (Exception e) {
            log.info("Could not get hostname", e);
        }
        return "n/a";
    }

    /**
     * Executes a command and returns it's stdout.
     * 
     * @param execCommand the command to execute
     * @return the stdout output of the executed command
     * @throws IOException something went wrong
     */
    public static String execReadToString(String execCommand) throws IOException {
        try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    /**
     * Types of Operating Systems.
     */
    public enum OSType {
        WINDOWS, MACOS, LINUX, OTHER
    }
    
    /**
     * Detect the operating system from the os.name System property and cache
     * the result.
     * 
     * @returns - the operating system detected
     */
    public static OSType getOperatingSystemType() {
        OSType detectedOS = null;
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
            detectedOS = OSType.MACOS;
        } else if (os.indexOf("win") >= 0) {
            detectedOS = OSType.WINDOWS;
        } else if (os.indexOf("nux") >= 0) {
            detectedOS = OSType.LINUX;
        } else {
            detectedOS = OSType.OTHER;
        }
        return detectedOS;
    }
  
    /**
     * Return true if the current operating system is MACOS, otherwise false.
     * 
     * @return true if we are on a Mac.
     */
    public static boolean isMac() {
        return OSType.MACOS == getOperatingSystemType();
    }
}
