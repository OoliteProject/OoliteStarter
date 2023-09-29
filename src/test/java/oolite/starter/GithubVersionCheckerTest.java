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
            instance.getLatestVersion();
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
        Semver s = instance.getLatestVersion();
        assertNotNull(s);
    }

    /**
     * Test of getReleasesURL method, of class GithubVersionChecker.
     */
    @Test
    public void testGetReleasesURL() throws Exception {
        log.info("getReleasesURL");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("https://api.github.com/repos/HiranChaudhuri/OoliteStarter/releases", instance.getReleasesURL().toExternalForm());
    }

    /**
     * Test of getReleaseURL method, of class GithubVersionChecker.
     */
    @Test
    public void testGetHtmlReleaseURL() throws Exception {
        log.info("testGetHtmlReleaseURL");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("https://github.com/HiranChaudhuri/OoliteStarter/releases/tag/vcoco", instance.getHtmlReleaseURL("coco").toExternalForm());
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
        
        Semver v = instance.getLatestVersion();
        assertNotNull(v);
    }

    /**
     * Test of getHtmlUserMessage method, of class GithubVersionChecker.
     */
    @Test
    public void testGetHtmlUserMessage() throws MalformedURLException {
        log.info("getHtmlUserMessage");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("<html><body><p>All right there. We heard rumors the new version 999.999.999 has been released.</p><p>You need to check <a href=\"https://github.com/HiranChaudhuri/OoliteStarter/releases/tag/v999.999.999\">https://github.com/HiranChaudhuri/OoliteStarter/releases/tag/v999.999.999</a> and report back to me.</p><p>But don't keep me waiting too long, kid!</p></body></html>", instance.getHtmlUserMessage(new Semver("999.999.999")));
    }

    /**
     * Test of getHtmlUserMessage method, of class GithubVersionChecker.
     */
    @Test
    public void testGetHtmlUserMessage2() throws MalformedURLException {
        log.info("getHtmlUserMessage2");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("<html><body><p>All right there. We heard rumors the new experimental version 999.999.999-test.1 has been released.</p><p>You need to check <a href=\"https://github.com/HiranChaudhuri/OoliteStarter/releases/tag/v999.999.999-test.1\">https://github.com/HiranChaudhuri/OoliteStarter/releases/tag/v999.999.999-test.1</a> and report back to me.</p><p>But don't keep me waiting too long, kid!</p></body></html>", instance.getHtmlUserMessage(new Semver("999.999.999-test.1")));
    }

}