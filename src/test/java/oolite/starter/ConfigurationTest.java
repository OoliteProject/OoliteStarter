/*
 */

package oolite.starter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
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
public class ConfigurationTest {
    private static final Logger log = LogManager.getLogger();

     public ConfigurationTest() {
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
     * Test of testGetOoliteCommand method, of class Configuration.
     */
    @Test
    public void testDefaultConfiguration() throws MalformedURLException {
        Configuration c = new Configuration();
        List<File> addonDirs = c.getAddonDirs();
        assertEquals(4, addonDirs.size());
        assertTrue(String.valueOf(c.getDeactivatedAddonsDir()).contains("GNUstep/Library/ApplicationSupport/Oolite/DeactivatedAddOns"));
        assertEquals("[https://addons.oolite.space/api/1.0/overview/, http://addons.oolite.org/api/1.0/overview/]", String.valueOf(c.getExpansionManagerURLs()));
        assertTrue(String.valueOf(c.getManagedAddonsDir()).contains("GNUstep/Library/ApplicationSupport/Oolite/ManagedAddOns"));
        assertTrue(c.getOoliteCommand().contains("GNUstep/Applications/Oolite/oolite.app/oolite-wrapper"));
        assertTrue(String.valueOf(c.getSaveGameDir()).contains("oolite-saves"));
    }

    /**
     * Test of testGetOoliteCommand2 method, of class Configuration.
     */
    @Test
    public void testDefaultConfiguration2() throws MalformedURLException, IOException {
        Configuration c = new Configuration(new File("src/test/resources/testConfig.properties"));
        List<File> addonDirs = c.getAddonDirs();
        assertEquals(4, addonDirs.size());
        assertEquals("inactive", String.valueOf(c.getDeactivatedAddonsDir()));
        assertEquals("[https://addons.oolite.space/api/1.0/overview/, http://addons.oolite.org/api/1.0/overview/]", String.valueOf(c.getExpansionManagerURLs()));
        assertEquals("active", String.valueOf(c.getManagedAddonsDir()));
        assertEquals("oolite", c.getOoliteCommand());
        assertEquals("savegames", String.valueOf(c.getSaveGameDir()));
    }

}