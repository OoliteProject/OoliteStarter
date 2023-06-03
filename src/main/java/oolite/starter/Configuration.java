/*
 */
package oolite.starter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *
 * @author hiran
 */
public class Configuration {
    
    private Properties properties;

    /**
     * Creates a new Configuration instance.
     * Finds the platform specific configuration file to initialize.
     */
    public Configuration() {
        properties = new Properties();
        properties.setProperty("oolite.savegames.dir", System.getProperty("user.home") + "/oolite-saves");
        properties.setProperty("oolite.addons.deactivated.dir", System.getProperty("user.home") +"/GNUstep/Library/ApplicationSupport/Oolite/DeactivatedAddOns");
        properties.setProperty("oolite.addons.activated.dir", System.getProperty("user.home") +"/GNUstep/Library/ApplicationSupport/Oolite/ManagedAddOns");
        properties.setProperty("oolite.executable", "/home/hiran/GNUstep/Applications/Oolite/oolite.app/oolite-wrapper");
        
        StringBuilder s = new StringBuilder();
        s.append(new File(System.getProperty("user.home") +"/GNUstep/Applications/Oolite/AddOns").getAbsolutePath());
        s.append(File.pathSeparator);
        s.append(new File(System.getProperty("user.home") +"/.Oolite/Add-ons").getAbsolutePath());
        properties.setProperty("oolite.addons.additional_dirs", s.toString());
    }

    /**
     * Creates a new Configuration instance.
     * Loads the given configuration file to initialize.
     * 
     * @param the configuration file to load
     */
    public Configuration(File f) throws IOException {
        this();
        
        Properties p = new Properties();
        p.load(new FileInputStream(f));

        properties.putAll(p);
    }
    
    /**
     * Returns the directory where Oolite stores it's save games.
     * 
     * @return the directory
     */
    public File getSaveGameDir() {
        return new File(properties.getProperty("oolite.savegames.dir"));
    }
    
    /**
     * Returns the directory where we hide expansions from Oolite.
     * 
     * @return the directory
     */
    public File getDeactivatedAddonsDir() {
        return new File(properties.getProperty("oolite.addons.deactivated.dir"));
    }

    /**
     * Returns the directory where we hide expansions from Oolite.
     * 
     * @return the directory
     */
    public File getManagedAddonsDir() {
        return new File(properties.getProperty("oolite.addons.activated.dir"));
    }

    /**
     * Returns the directory where Oolite stores groups of OXPs.
     * @see https://wiki.alioth.net/index.php/OXP#Locating_your_AddOns_folder
     * 
     * @return the directory
     */
    public List<File> getAddonDirs() {
        List<File> result = new ArrayList<>();

        StringTokenizer st = new StringTokenizer(
                properties.getProperty("oolite.addons.additional_dirs"),
                File.pathSeparator
        );
        while(st.hasMoreTokens()) {
            result.add(new File(st.nextToken()));
        }
        
        // but here is where the expansion manager will install
        result.add(getManagedAddonsDir());
        
        // and this is where we will park deactivated expansions
        result.add(getDeactivatedAddonsDir());
        
        return result;
    }
    
    /**
     * Returns the command to execute for running Oolite.
     * 
     * @return the command
     */
    public String getOoliteCommand() {
        return properties.getProperty("oolite.executable");
    }
    
    /**
     * Returns a list of Expansion Manager's manifest URLs.
     * 
     * @return the list of URLs
     * @throws MalformedURLException something went wrong
     */
    public List<URL> getExpansionManagerURLs() throws MalformedURLException {
        List<URL> result = new ArrayList<>();
        result.add(new URL("https://addons.oolite.space/api/1.0/overview/"));
        //result.add(new URL("http://addons.oolite.space/api/1.0/overview/"));
        result.add(new URL("http://addons.oolite.org/api/1.0/overview/"));
        return result;
    }
}
