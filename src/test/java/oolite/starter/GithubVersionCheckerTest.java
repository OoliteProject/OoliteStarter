/*
 */

package oolite.starter;

import com.vdurmont.semver4j.Semver;
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
public class GithubVersionCheckerTest {
    private static final Logger log = LogManager.getLogger();

     public GithubVersionCheckerTest() {
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
     * Test of init method, of class GithubVersionChecker.
     */
    @Test
    public void testInit() throws Exception {
        log.info("init");

        GithubVersionChecker instance = new GithubVersionChecker();
        try {
            instance.getLatestVersion(instance.getMyVersion());
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("versions is null. Use init()", e.getMessage());
        }
    }

    /**
     * Test of init method, of class GithubVersionChecker.
     */
    @Test
    public void testInit2() throws Exception {
        log.info("testInit2");

        GithubVersionChecker instance = new GithubVersionChecker();
        instance.setUpdateCheckInterval(Duration.ofSeconds(0));
        instance.init();
        Semver s = instance.getLatestVersion(instance.getMyVersion());
        assertNotNull(s);
    }

    /**
     * Test of getReleasesURL method, of class GithubVersionChecker.
     */
    @Test
    public void testGetReleasesURL() throws Exception {
        log.info("getReleasesURL");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("https://api.github.com/repos/OoliteProject/OoliteStarter/releases", instance.getReleasesURL().toExternalForm());
    }

    /**
     * Test of getReleaseURL method, of class GithubVersionChecker.
     */
    @Test
    public void testGetHtmlReleaseURL() throws Exception {
        log.info("testGetHtmlReleaseURL");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("https://github.com/OoliteProject/OoliteStarter/releases/tag/vcoco", instance.getHtmlReleaseURL("coco").toExternalForm());
    }

    /**
     * Test of getLatestVersion method, of class GithubVersionChecker.
     */
    @Test
    public void testGetLatestVersion() throws Exception {
        log.info("getLatestVersion");

        GithubVersionChecker instance = new GithubVersionChecker();
        instance.setUpdateCheckInterval(Duration.ofSeconds(0));
        instance.init();
        
        Semver v = instance.getLatestVersion(instance.getMyVersion());
        assertNotNull(v);
    }

    /**
     * Test of getHtmlUserMessage method, of class GithubVersionChecker.
     */
    @Test
    public void testGetHtmlUserMessage() throws MalformedURLException {
        log.info("getHtmlUserMessage");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("<html><body><p>All right there. We heard rumors the new version 999.999.999 has been released.</p><p>You need to check <a href=\"https://github.com/OoliteProject/OoliteStarter/releases/tag/v999.999.999\">https://github.com/OoliteProject/OoliteStarter/releases/tag/v999.999.999</a> and report back to me.</p><p>But don't keep me waiting too long, kid!</p></body></html>", instance.getHtmlUserMessage(new Semver("999.999.999")));
    }

    /**
     * Test of getHtmlUserMessage method, of class GithubVersionChecker.
     */
    @Test
    public void testGetHtmlUserMessage2() throws MalformedURLException {
        log.info("getHtmlUserMessage2");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("<html><body><p>All right there. We heard rumors the new experimental version 999.999.999-test.1 has been released.</p><p>You need to check <a href=\"https://github.com/OoliteProject/OoliteStarter/releases/tag/v999.999.999-test.1\">https://github.com/OoliteProject/OoliteStarter/releases/tag/v999.999.999-test.1</a> and report back to me.</p><p>But don't keep me waiting too long, kid!</p></body></html>", instance.getHtmlUserMessage(new Semver("999.999.999-test.1")));
    }

    /**
     */
    @Test
    public void testGetUpdateCheckInterval() throws MalformedURLException {
        log.info("testGetUpdateCheckInterval");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals(null, instance.getUpdateCheckInterval());
    }

    /**
     */
    @Test
    public void testGetUpdateCheckInterval2() throws MalformedURLException {
        log.info("testGetUpdateCheckInterval2");

        GithubVersionChecker instance = new GithubVersionChecker();
        instance.setUpdateCheckInterval(Duration.ofDays(12));
        assertEquals(Duration.ofDays(12), instance.getUpdateCheckInterval());
    }
}