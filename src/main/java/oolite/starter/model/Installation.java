/*
 */

package oolite.starter.model;

/**
 *
 * @author hiran
 */
public class Installation {
    
    private String homeDir;
    private String version;
    private String excecutable;
    private String savegameDir;
    private String addonDir;
    private String deactivatedAddonDir;
    private String managedAddonDir;
    private String managedDeactivatedAddonDir;
    
    /**
     * Creates a new Installation.
     */
    public Installation() {
        // no need for init code so far
    }

    /**
     * Returns the executable to run Oolite.
     * 
     * @return the full path 
     */
    public String getExcecutable() {
        return excecutable;
    }

    /**
     * Sets the executable to run Oolite.
     * 
     * @param excecutable the full path 
     */
    public void setExcecutable(String excecutable) {
        this.excecutable = excecutable;
    }

    /**
     * Returns the Oolite version.
     * 
     * @return the version string 
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the Oolite version.
     * 
     * @param version the version string 
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the Oolite home directory.
     * 
     * @return the full path 
     */
    public String getHomeDir() {
        return homeDir;
    }

    /**
     * Sets the Oolite home directory.
     * 
     * @param homeDir the full path 
     */
    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    /**
     * Returns the directory for save games.
     * 
     * @return the full path 
     */
    public String getSavegameDir() {
        return savegameDir;
    }

    /**
     * Sets the directory for save games.
     * 
     * @param savegameDir the full path 
     */
    public void setSavegameDir(String savegameDir) {
        this.savegameDir = savegameDir;
    }

    /**
     * Returns the addon directory.
     * 
     * @return the full path 
     */
    public String getAddonDir() {
        return addonDir;
    }

    /**
     * RSets the addon directory.
     * 
     * @param addonDir the full path 
     */
    public void setAddonDir(String addonDir) {
        this.addonDir = addonDir;
    }

    /**
     * Returns the managed addon directory.
     * 
     * @return the full path 
     */
    public String getManagedAddonDir() {
        return managedAddonDir;
    }

    /**
     * Sets the managed addon directory.
     * 
     * @param managedAddonDir the full path 
     */
    public void setManagedAddonDir(String managedAddonDir) {
        this.managedAddonDir = managedAddonDir;
    }

    /**
     * Returns the deactivated addon directory.
     * 
     * @return the full path 
     */
    public String getDeactivatedAddonDir() {
        return deactivatedAddonDir;
    }

    /**
     * Sets the deactivated addon directory.
     * 
     * @param deactivatedAddonDir the full path 
     */
    public void setDeactivatedAddonDir(String deactivatedAddonDir) {
        this.deactivatedAddonDir = deactivatedAddonDir;
    }

    /**
     * Returns the managed deactivated addon directory.
     * 
     * @return the full path 
     */
    public String getManagedDeactivatedAddonDir() {
        return managedDeactivatedAddonDir;
    }

    /**
     * Sets the managed deactivated addon directory.
     * 
     * @param managedDeactivatedAddonDir the full path 
     */
    public void setManagedDeactivatedAddonDir(String managedDeactivatedAddonDir) {
        this.managedDeactivatedAddonDir = managedDeactivatedAddonDir;
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
        sb.append(", deactivatedAddonDir=").append(deactivatedAddonDir);
        sb.append(", managedAddonDir=").append(managedAddonDir);
        sb.append(", managedDeactivatedAddonDir=").append(managedDeactivatedAddonDir);
        sb.append('}');
        return sb.toString();
    }
    
    
}
