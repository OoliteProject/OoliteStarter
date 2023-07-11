/*
 */

package oolite.starter;

import com.owlike.genson.Genson;
import com.vdurmont.semver4j.Semver;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import oolite.starter.ui.MrGimlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Checks Github to see if we are still running the latest version.
 * 
 * @author hiran
 */
public class GithubVersionChecker {
    private static final Logger log = LogManager.getLogger();

    public static final String OWNER = "HiranChaudhuri";
    public static final String REPO = "OoliteStarter";
    
    private List<Semver> versions;
    
    /**
     * Initializes the version checker.
     * This is where required data is downloaded from Github.
     * 
     * @throws MalformedURLException something went wrong
     * @throws IOException something went wrong
     */
    public void init() throws IOException {
        versions = new ArrayList<>();
        List<Object> releases = new Genson().deserialize(getReleasesURL().openStream(), List.class);
        for (Object release: releases) {
            if (release instanceof Map<?,?> map) {
                String v = String.valueOf(map.get("tag_name"));
                if (v.startsWith("v")) {
                    v = v.substring(1);
                }
                versions.add(new Semver(v));
            } else {
                log.debug("class {}", release.getClass());
                log.debug("release {}", release);
            }
        }
    }
    
    /**
     * Provides the URL to check for releases.
     * 
     * @return the URL
     * @throws MalformedURLException something went wrong
     */
    public URL getReleasesURL() throws MalformedURLException {
        return new URL("https://api.github.com/repos/" + OWNER + "/" + REPO + "/releases");
    }
    
    /**
     * Checks if there is a later version online and returns it.
     * 
     * @return the new version number, or null if there is none
     * 
     * @throws MalformedURLException something went wrong
     * @throws IOException something went wrong
     */
    public String getLatestVersion() throws IOException {
        if (versions == null) {
            throw new IllegalStateException("versions is null. Use init()");
        }
        
        String v = getClass().getPackage().getImplementationVersion();
        if (v==null || v.contains("SNAPSHOT")) { // this is the case when running from the IDE
            v = "0.1.10";
        }
        Semver me = new Semver(v);
        
        if (!versions.isEmpty()) {
            Collections.sort(versions);
            log.debug("versions {}", versions);
            Semver latest = versions.get(versions.size()-1);
            log.debug("version me={} latest={}", me, latest);
            
            if (latest.isGreaterThan(me)) {
                log.debug("latest is greater!");
                return latest.toString();
            }
        }
        return null;
    }
    
    /**
     * Returns a user message in HTML.
     * It informs the user about the new version and suggests to visit the
     * homepage.
     * 
     * @param version the latest version
     * @return the html message
     */
    public String getHtmlUserMessage(String version) {
        return "<html><body><p>All right there. We heard rumors the new version " + version + " has been released.</p>"
            + "<p>You need to check <a href=\"https://github.com/" + OWNER + "/" + REPO + "/releases\">https://github.com/" + OWNER + "/" + REPO + "/releases</a> and report back to me.</p>"
            + "<p>But don't keep me waiting too long, kid!</p></body></html>";
    }
    
    /**
     * Checks for updates and displays a message.
     * 
     * @param parentComponent the component upon which to present the message
     */
    public void maybeAnnounceUpdate(Component parentComponent) {
        try {
            String latest = getLatestVersion();
            if (latest != null) {
                String message = getHtmlUserMessage(latest);
                EventQueue.invokeLater(() -> MrGimlet.showMessage(parentComponent, message) );
            }
        } catch (IOException e) {
            log.info("Could not check for update", e);
        }
    }
}
