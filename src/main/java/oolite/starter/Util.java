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
     * Returns the operating system's hostname.
     * 
     * @return the hostname
     */
    public static String getHostname() {

        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                return execReadToString("hostname");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac os x")) {
                return execReadToString("hostname");
            }
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
    public static enum OSType {
        Windows, MacOS, Linux, Other
    };
    
    /**
     * Detect the operating system from the os.name System property and cache
     * the result.
     * 
     * @returns - the operating system detected
     */
    public static OSType getOperatingSystemType() {
        OSType detectedOS = null;
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
            detectedOS = OSType.MacOS;
        } else if (OS.indexOf("win") >= 0) {
            detectedOS = OSType.Windows;
        } else if (OS.indexOf("nux") >= 0) {
            detectedOS = OSType.Linux;
        } else {
            detectedOS = OSType.Other;
        }
        return detectedOS;
    }
  
    /**
     * Return true if the current operating system is MacOS, otherwise false.
     * 
     * @return true if we are on a Mac.
     */
    public static boolean isMac() {
        return OSType.MacOS == getOperatingSystemType();
    }
}
