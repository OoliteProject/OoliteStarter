/*
 */
package oolite.starter.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Locale;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class Util {
    public static final Logger log = LogManager.getLogger();

    private static final String EXCEPTION_IN_MUST_NOT_BE_NULL = "in must not be null";
        
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
        log.debug("execReadToString({})", execCommand);
        if (execCommand == null) {
            throw new IllegalArgumentException("execCommand must not be null");
        }
        if (execCommand.isBlank()) {
            throw new IllegalArgumentException("execCommand must contain something");
        }
        
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

    /**
     * Copies a stream into a memory buffer and returns it for reading.
     * This allows the file to be re-read if need be.
     * 
     * @param in The inputstream to read from
     * @return the re-readable memory based InputStream
     * @throws IOException something went wrong
     * 
     */
    public static InputStream getBufferedStream(InputStream in) throws IOException {
        log.debug("getBufferedStream(...)");
        if (in == null) {
            throw new IllegalArgumentException(EXCEPTION_IN_MUST_NOT_BE_NULL);
        }
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read = 0;
        while ( (read = in.read(buffer))>=0) {
            bos.write(buffer, 0, read);
        }
        bos.flush();
        bos.close();
        
        return new ByteArrayInputStream(bos.toByteArray());
    }
    
    /**
     * Takes a size in Bytes and transforms it into a human-readable String.
     * That means if the number is too high it will be converted to kilobytes,
     * or Megabytes respectively.
     * 
     * @param size the amount of bytes
     * @return the human readable String
     */
    public static String humanreadableSize(long size) {
        log.debug("humanreadableSize({})", size);
        if (size >= 1024*1024) {
            return String.format("%.2f MB", (size)/(1024f*1024f));
        }
        if (size >= 1024) {
            return String.format("%.2f kB", (size)/1024f);
        }
        return String.valueOf(size);
    }
    
    /**
     * Checks if a file is a ZIP archive.
     * 
     * @param f the file to check
     * @return true if it is a ZIP, false otherwise
     */
    public static boolean isZipFile(File f) throws IOException {
        log.debug("isZipFile({})", f);
        
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            int fileSignature = raf.readInt();
            return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
        }
    }
    
    /**
     * Unzips the ZIP file's inputstream to the destination directory.
     * 
     * @param src The input ZIP
     * @param destDir the destination directory
     * @throws IOException something went wrong
     */
    public static void unzip(InputStream src, File destDir) throws IOException {
        log.warn("unzip({}, {})", src, destDir);
        try (ZipInputStream zis = new ZipInputStream(src)) {
            ZipEntry zEntry = null;
            
            while ( (zEntry = zis.getNextEntry()) != null ) {
                File dest = new File(destDir, zEntry.getName());
                log.warn("ZipEntry {} -> {}", zEntry.getName(), dest);
                if (zEntry.isDirectory()) {
                    dest.mkdirs();
                } else {
                    dest.getParentFile().mkdirs();
                    try (OutputStream os = new FileOutputStream(dest)) {
                        IOUtils.copy(zis, os, 4096);
                    }
                }
            }
        }
    }
}
