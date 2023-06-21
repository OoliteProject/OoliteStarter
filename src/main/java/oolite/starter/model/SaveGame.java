/*
 */
package oolite.starter.model;

import java.io.File;
import java.util.List;

/**
 *
 * @author hiran
 */
public class SaveGame {
    
    private String name;
    private File file;
    private String playerName;
    private long credits;
    private String currentSystemName;
    private String ooliteVersion;
    private long shipKills;
    private String shipClassName;
    private String shipName;
    
    /** Holds the detected expansions, if possible. */
    private List<String> expansions;
    
    /**
     * Creates a new SaveGame.
     */
    public SaveGame() {
    }

    /**
     * Returns the save game's name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the save game's name.
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the file hosting the savegame data.
     * 
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets the file hosting the savegame data.
     * 
     * @param file the file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Returns the player name.
     * 
     * @return the name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Sets the player name.
     * 
     * @param playerName the name
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Returns the amount of credits.
     * 
     * @return the amount
     */
    public long getCredits() {
        return credits;
    }

    /**
     * Sets the amount of credits.
     * 
     * @param credits the amount
     */
    public void setCredits(long credits) {
        this.credits = credits;
    }

    /**
     * Returns the current solar system name.
     * 
     * @return the name
     */
    public String getCurrentSystemName() {
        return currentSystemName;
    }

    /**
     * Sets the current solar system name.
     * 
     * @param currentSystemName the name
     */
    public void setCurrentSystemName(String currentSystemName) {
        this.currentSystemName = currentSystemName;
    }

    /**
     * Returns the Oolite version number for this game.
     * 
     * @return the version number string
     */
    public String getOoliteVersion() {
        return ooliteVersion;
    }

    /**
     * Sets the Oolite version number for this game.
     * 
     * @param ooliteVersion the version number string
     */
    public void setOoliteVersion(String ooliteVersion) {
        this.ooliteVersion = ooliteVersion;
    }

    /**
     * Returns the amount of kills.
     * 
     * @return the amount
     */
    public long getShipKills() {
        return shipKills;
    }

    /**
     * Sets the amount of kills.
     * 
     * @param shipKills the amount
     */
    public void setShipKills(long shipKills) {
        this.shipKills = shipKills;
    }

    /**
     * Returns the ship class name.
     * 
     * @return the name
     */
    public String getShipClassName() {
        return shipClassName;
    }

    /**
     * Sets the ship class name.
     * 
     * @param shipClassName the name
     */
    public void setShipClassName(String shipClassName) {
        this.shipClassName = shipClassName;
    }

    /**
     * Returns the ship name.
     * 
     * @return the name
     */
    public String getShipName() {
        return shipName;
    }

    /**
     * Sets the ship name.
     * 
     * @param shipName the name
     */
    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    /**
     * Returns the list of expansions that were active when saving the game.
     * If the list is empty, no expansions were active. If the list is null,
     * we just do not know.
     * 
     * @return the list of expansions or null if unknown
     */
    public List<String> getExpansions() {
        return expansions;
    }

    /**
     * Sets the list of expansions that were active when saving the game.
     * If the list is empty, no expansions were active. If the list is null,
     * we just do not know.
     * 
     * @param expansions the list of expansions or null if unknown
     */
    public void setExpansions(List<String> expansions) {
        this.expansions = expansions;
    }

    @Override
    public String toString() {
        return "SaveGame{" + "file=" + file + ", playerName=" + playerName + ", credits=" + credits + ", currentSystemName=" + currentSystemName + ", ooliteVersion=" + ooliteVersion + ", shipKills=" + shipKills + ", shipClassName=" + shipClassName + ", shipName=" + shipName + '}';
    }
    
    
}
