/*
 */

package oolite.starter;

import com.vdurmont.semver4j.Semver;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.net.MalformedURLException;
import java.time.Duration;
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
public class OoliteVersionCheckerTest {
    private static final Logger log = LogManager.getLogger();

     public OoliteVersionCheckerTest() {
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
     * Test of init method, of class OoliteVersionChecker.
     */
    @Test
    public void testInit() throws Exception {
        log.info("init");

        OoliteVersionChecker instance = new OoliteVersionChecker();
        try {
            instance.getLatestVersion(ModuleDescriptor.Version.parse("1.89"));
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("versions is null. Use init()", e.getMessage());
        }
    }

    /**
     * Test of init method, of class OoliteVersionChecker.
     */
    @Test
    public void testInit2() throws Exception {
        log.info("testInit2");

        OoliteVersionChecker instance = new OoliteVersionChecker();
        instance.setUpdateCheckInterval(Duration.ofSeconds(0));
        instance.init();
        ModuleDescriptor.Version s = instance.getLatestVersion(ModuleDescriptor.Version.parse("0.1.9"));
        assertNotNull(s);
    }

    /**
     * Test of getReleasesURL method, of class OoliteVersionChecker.
     */
    @Test
    public void testGetReleasesURL() throws Exception {
        log.info("getReleasesURL");

        OoliteVersionChecker instance = new OoliteVersionChecker();
        assertEquals("https://api.github.com/repos/OoliteProject/oolite/releases", instance.getReleasesURL().toExternalForm());
    }

    /**
     * Test of getReleaseURL method, of class OoliteVersionChecker.
     */
    @Test
    public void testGetHtmlReleaseURL() throws Exception {
        log.info("testGetHtmlReleaseURL");

        OoliteVersionChecker instance = new OoliteVersionChecker();
        assertEquals("https://github.com/OoliteProject/oolite/releases/tag/coco", instance.getHtmlReleaseURL("coco").toExternalForm());
    }

    /**
     * Test of getLatestVersion method, of class OoliteVersionChecker.
     */
    @Test
    public void testGetLatestVersion() throws Exception {
        log.info("getLatestVersion");

        OoliteVersionChecker instance = new OoliteVersionChecker();
        instance.setUpdateCheckInterval(Duration.ofSeconds(0));
        instance.init();
        
        ModuleDescriptor.Version v = instance.getLatestVersion(ModuleDescriptor.Version.parse("0.1.9"));
        assertNotNull(v);
    }

    /**
     * Test of getHtmlUserMessage method, of class OoliteVersionChecker.
     */
    @Test
    public void testGetHtmlUserMessage() throws MalformedURLException {
        log.info("getHtmlUserMessage");

        OoliteVersionChecker instance = new OoliteVersionChecker();
        assertEquals("<html><body><p>All right there. We heard rumors the new oolite version 999.999.999 has been released.</p><p>You need to check <a href=\"https://github.com/OoliteProject/oolite/releases/tag/999.999.999\">https://github.com/OoliteProject/oolite/releases/tag/999.999.999</a> and report back to me.</p><p>But don't keep me waiting too long, kid!</p></body></html>", instance.getHtmlUserMessage(ModuleDescriptor.Version.parse("999.999.999")));
    }

    /**
     */
    @Test
    public void testGetUpdateCheckInterval() throws MalformedURLException {
        log.info("testGetUpdateCheckInterval");

        OoliteVersionChecker instance = new OoliteVersionChecker();
        assertEquals(null, instance.getUpdateCheckInterval());
    }

    /**
     */
    @Test
    public void testGetUpdateCheckInterval2() throws MalformedURLException {
        log.info("testGetUpdateCheckInterval2");

        OoliteVersionChecker instance = new OoliteVersionChecker();
        instance.setUpdateCheckInterval(Duration.ofDays(12));
        assertEquals(Duration.ofDays(12), instance.getUpdateCheckInterval());
    }

    /**
     * Check if we can list Oolite versions.
     */
    @Test
    public void testOoliteVersion() throws IOException {
        log.info("testOoliteVersion()");
        
        OoliteVersionChecker instance = new OoliteVersionChecker("OoliteProject", "oolite");
        instance.setUpdateCheckInterval(Duration.ofMillis(0));
        instance.init();
        
        assertNotNull(instance.getLatestVersion(ModuleDescriptor.Version.parse("0.1.9")));
    }
}