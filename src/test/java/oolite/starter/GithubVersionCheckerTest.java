/*
 */

package oolite.starter;

import com.vdurmont.semver4j.Semver;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;
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
        System.out.println("init");

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
        System.out.println("testInit2");

        GithubVersionChecker instance = new GithubVersionChecker();
        instance.init();
        assertNotNull(instance.getLatestVersion());
    }

    /**
     * Test of getReleasesURL method, of class GithubVersionChecker.
     */
    @Test
    public void testGetReleasesURL() throws Exception {
        System.out.println("getReleasesURL");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("https://api.github.com/repos/HiranChaudhuri/OoliteStarter/releases", instance.getReleasesURL().toExternalForm());
    }

    /**
     * Test of getLatestVersion method, of class GithubVersionChecker.
     */
    @Test
    public void testGetLatestVersion() throws Exception {
        System.out.println("getLatestVersion");

        GithubVersionChecker instance = new GithubVersionChecker();
        instance.init();
        
        String v = instance.getLatestVersion();
        assertNotNull(new Semver(v));
    }

    /**
     * Test of getHtmlUserMessage method, of class GithubVersionChecker.
     */
    @Test
    public void testGetHtmlUserMessage() {
        System.out.println("getHtmlUserMessage");

        GithubVersionChecker instance = new GithubVersionChecker();
        assertEquals("<html><body><p>All right threre. We heard rumors the new version vX has been released.</p><p>You need to check <a href=\"https://github.com/HiranChaudhuri/OoliteStarter/releases\">https://github.com/HiranChaudhuri/OoliteStarter/releases</a> and report back to me.</p><p>But don't keep me waiting too long, kid!</p></body></html>", instance.getHtmlUserMessage("vX"));
    }

}