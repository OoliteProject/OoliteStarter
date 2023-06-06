/*
 */

package oolite.starter.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class Installation {
    private static final Logger log = LogManager.getLogger();

    private String homeDir;
    private String version;
    private String excecutable;
    private String savegameDir;
    private String addonDir;
    private String managedAddonDir;
    private String deactivatedAddonDir;
    
    /**
     * Creates a new Installation.
     */
    public Installation() {
        
    }

    public String getExcecutable() {
        return excecutable;
    }

    public void setExcecutable(String excecutable) {
        this.excecutable = excecutable;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    public String getSavegameDir() {
        return savegameDir;
    }

    public void setSavegameDir(String savegameDir) {
        this.savegameDir = savegameDir;
    }

    public String getAddonDir() {
        return addonDir;
    }

    public void setAddonDir(String addonDirs) {
        this.addonDir = addonDirs;
    }

    public String getManagedAddonDir() {
        return managedAddonDir;
    }

    public void setManagedAddonDir(String managedAddonDir) {
        this.managedAddonDir = managedAddonDir;
    }

    public String getDeactivatedAddonDir() {
        return deactivatedAddonDir;
    }

    public void setDeactivatedAddonDir(String deactivatedAddonDir) {
        this.deactivatedAddonDir = deactivatedAddonDir;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Installation{");
        sb.append("excecutable=").append(excecutable);
        sb.append(", version=").append(version);
        sb.append(", homeDir=").append(homeDir);
        sb.append(", savegameDir=").append(savegameDir);
        sb.append(", addonDirs=").append(addonDir);
        sb.append(", managedAddonDir=").append(managedAddonDir);
        sb.append(", deactivatedAddonDir=").append(deactivatedAddonDir);
        sb.append('}');
        return sb.toString();
    }
    
    
}
