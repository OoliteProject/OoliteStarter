/*
 */

package oolite.starter.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
 
/**
 *
 * @author hiran
 */
public class InstallationTest {
    private static final Logger log = LogManager.getLogger();

     public InstallationTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getExcecutable method, of class Installation.
     */
    @Test
    public void testSetGetExcecutable() {
        log.info("getExcecutable");
        
        Installation instance = new Installation();
        assertNull(instance.getExcecutable());

        instance.setExcecutable("myexec");
        assertEquals("myexec", instance.getExcecutable());
    }

    /**
     * Test of getVersion method, of class Installation.
     */
    @Test
    public void testSetGetVersion() {
        log.info("testSetGetVersion");

        Installation instance = new Installation();
        assertNull(instance.getVersion());
        
        instance.setVersion("myversion");
        assertEquals("myversion", instance.getVersion());
    }

    /**
     * Test of setHomeDir method, of class Installation.
     */
    @Test
    public void testSetGetHomeDir() {
        log.info("testSetGetHomeDir");
        Installation instance = new Installation();
        assertNull(instance.getHomeDir());
        
        instance.setHomeDir("myhomw");
        assertEquals("myhomw", instance.getHomeDir());
    }

    /**
     * Test of getSavegameDir method, of class Installation.
     */
    @Test
    public void testSetGetSavegameDir() {
        log.info("testSetGetSavegameDir");

        Installation instance = new Installation();
        assertNull(instance.getSavegameDir());
        
        instance.setSavegameDir("mzsaves");
        assertEquals("mzsaves", instance.getSavegameDir());
    }

    /**
     * Test of getAddonDir method, of class Installation.
     */
    @Test
    public void testSetGetAddonDir() {
        log.info("testSetGetAddonDir");

        Installation instance = new Installation();
        assertNull(instance.getAddonDir());
        
        instance.setAddonDir("mzaddon");
        assertEquals("mzaddon", instance.getAddonDir());
    }

    /**
     * Test of setManagedAddonDir method, of class Installation.
     */
    @Test
    public void testSetGetManagedAddonDir() {
        log.info("testSetGetManagedAddonDir");

        Installation instance = new Installation();
        assertNull(instance.getManagedAddonDir());
        
        instance.setManagedAddonDir("mymanagedaddondir");
        assertEquals("mymanagedaddondir", instance.getManagedAddonDir());
    }

    /**
     * Test of setDeactivatedAddonDir method, of class Installation.
     */
    @Test
    public void testSetGetDeactivatedAddonDir() {
        log.info("testSetGetDeactivatedAddonDir");

        Installation instance = new Installation();
        assertNull(instance.getDeactivatedAddonDir());
        
        instance.setDeactivatedAddonDir("blah");
        assertEquals("blah", instance.getDeactivatedAddonDir());
    }

    /**
     * Test of getManagedDeactivatedAddonDir method, of class Installation.
     */
    @Test
    public void testSetGetManagedDeactivatedAddonDir() {
        log.info("testSetGetManagedDeactivatedAddonDir");

        Installation instance = new Installation();
        assertNull(instance.getManagedDeactivatedAddonDir());
        
        instance.setManagedDeactivatedAddonDir("mdad");
        assertEquals("mdad", instance.getManagedDeactivatedAddonDir());
    }

    /**
     * Test of toString method, of class Installation.
     */
    @Test
    public void testToString() {
        log.info("toString");
        Installation instance = new Installation();
        assertEquals("Installation{excecutable=null, version=null, homeDir=null, savegameDir=null, addonDirs=null, deactivatedAddonDir=null, managedAddonDir=null, managedDeactivatedAddonDir=null, debugCapable=false}", instance.toString());
    }

}