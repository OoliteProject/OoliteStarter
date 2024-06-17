/*
 */

package oolite.starter.model;

/**
 * An installation of Oolite.
 * This entity mainly contains paths but also version number and presence of
 * debug oxp.
 *
 * @author hiran
 */
public class Installation {

    /**
     * Class to hold all relevant MQTT configuration.
     */
    public static class Mqtt {
        private String brokerUrl;
        private String user;
        private char[] password;
        private String prefix;

        /**
         * Creates a new Mqtt instance.
         */
        public Mqtt() {
        }

        /**
         * Returns the broker URL.
         * 
         * @return the url
         */
        public String getBrokerUrl() {
            return brokerUrl;
        }

        /**
         * Sets the broker URL.
         * 
         * @param mqttBrokerUrl the url
         */
        public void setBrokerUrl(String mqttBrokerUrl) {
            this.brokerUrl = mqttBrokerUrl;
        }

        /**
         * Returns the username.
         * 
         * @return the username
         */
        public String getUser() {
            return user;
        }

        /**
         * Sets the username.
         * 
         * @param mqttUser the username
         */
        public void setUser(String mqttUser) {
            this.user = mqttUser;
        }

        /**
         * Returns the password.
         * 
         * @return the password
         */
        public char[] getPassword() {
            return password;
        }

        /**
         * Sets the password.
         * 
         * @param mqttPassword the password
         */
        public void setPassword(char[] mqttPassword) {
            this.password = mqttPassword;
        }

        /**
         * Returns the prefix.
         * 
         * @return the prefix
         */
        public String getPrefix() {
            return prefix;
        }

        /**
         * Sets the prefix.
         * 
         * @param prefix the prefix
         */
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        
    }
    
    private String homeDir;
    private String version;
    private String excecutable;
    private String savegameDir;
    private String addonDir;
    private String deactivatedAddonDir;
    private String managedAddonDir;
    private String managedDeactivatedAddonDir;
    private boolean debugCapable;
    private Mqtt mqtt;
    
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

    /**
     * Returns whether this installation can speak debug console protocol.
     * 
     * @return true if and only if it can
     */
    public boolean isDebugCapable() {
        return debugCapable;
    }

    /**
     * Set to true if this installation can speak debug console protocol.
     * 
     * @param debugCapable true if and only if it can, false otherwise
     */
    public void setDebugCapable(boolean debugCapable) {
        this.debugCapable = debugCapable;
    }

    /**
     * Returns the MQTT data.
     * 
     * @return the mqtt data
     */
    public Mqtt getMqtt() {
        return mqtt;
    }

    /**
     * Sets the MQTT data.
     * 
     * @param mqtt the mqtt data
     */
    public void setMqtt(Mqtt mqtt) {
        this.mqtt = mqtt;
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
        sb.append(", debugCapable=").append(debugCapable);
        sb.append(", mqtt=").append(mqtt);
        sb.append('}');
        return sb.toString();
    }
    
    
}
