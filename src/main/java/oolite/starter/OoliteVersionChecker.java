/*
 */

package oolite.starter;

import com.owlike.genson.Genson;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import oolite.starter.ui.MrGimlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Checks Github to see if we are still running the latest version.
 * 
 * @author hiran
 */
public class OoliteVersionChecker {
    private static final Logger log = LogManager.getLogger();

    private String OWNER = "OoliteProject";
    private String REPO = "oolite";
    
    private List<ModuleDescriptor.Version> versions;
    private Duration updateCheckInterval = null;
    
    /**
     * Creates a default version checker for OoliteStarter.
     */
    public OoliteVersionChecker() {
        log.debug("GithubVersionChecker()");
    }

    /**
     * Creates a version checker for OoliteStarter.
     * 
     * @param owner the repository owner
     * @param repo the repository name
     */
    public OoliteVersionChecker(String owner, String repo) {
        log.debug("GithubVersionChecker({}, {})", owner, repo);
        
        this.OWNER = owner;
        this.REPO = repo;
    }

    /**
     * Returns the minimum duration between update checks.
     * 
     * @return the duration
     */
    public Duration getUpdateCheckInterval() {
        return updateCheckInterval;
    }

    /**
     * Sets the minimum duration between update checks.
     * 
     * @param updateCheckInterval the duration
     */
    public void setUpdateCheckInterval(Duration updateCheckInterval) {
        this.updateCheckInterval = updateCheckInterval;
    }
    
    private Instant readLastCheckInstant() {
        Preferences prefs = Preferences.userRoot().node(getClass().getName());
        String s = prefs.get("lastUpdateCheckInstant." + OWNER + "." + REPO, "2007-12-03T10:15:30.00Z");
        return Instant.parse(s);
    }
    
    private void storeLastCheckInstant(Instant instant) {
        Preferences prefs = Preferences.userRoot().node(getClass().getName());
        prefs.put("lastUpdateCheckInstant." + OWNER + "." + REPO, instant.toString());
    }
    
    /**
     * Initializes the version checker.
     * This is where required data is downloaded from Github.
     * 
     * @throws MalformedURLException something went wrong
     * @throws IOException something went wrong
     */
    public void init() throws IOException {
        if (updateCheckInterval == null) {
            throw new IllegalArgumentException("updateCheckInterval not set.");
        }
        
        versions = new ArrayList<>();

        Instant lastUpdateCheckInstant = readLastCheckInstant();
        Instant nextUpdateCheckInstant = lastUpdateCheckInstant.plus(updateCheckInterval);
        
        if (Instant.now().isAfter(nextUpdateCheckInstant)) {
            URL url = null;
            try {
                url = getReleasesURL();
                url.openStream();
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                //connection.setRequestProperty("Referer", "http://oolite.org");
                connection.setDoInput(true);
                InputStream in = connection.getInputStream();

                List<Object> releases = new Genson().deserialize(in, List.class);
                for (Object release: releases) {
                    if (release instanceof Map<?,?> map) {
                        String v = String.valueOf(map.get("tag_name"));
                        if (v.startsWith("v")) {
                            v = v.substring(1);
                        }
                        
                        versions.add(ModuleDescriptor.Version.parse(v));
                    } else {
                        log.debug("class {}", release.getClass());
                        log.debug("release {}", release);
                    }
                }
            } catch (Exception e) {
                log.info("Could not check for new versions at " + url, e);
                versions.add(ModuleDescriptor.Version.parse("0.1.11"));
            } finally {
                storeLastCheckInstant(Instant.now());
            }
        } else {
            log.info("Update check skipped until {}", nextUpdateCheckInstant);
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
     * Provides the URL for a human to download a release.
     * 
     * @param releaseTag the name the release was tagged with (e.g. v0.1.16-yard.9)
     * @return the URL
     * @throws MalformedURLException something went wrong
     */
    public URL getHtmlReleaseURL(String releaseTag) throws MalformedURLException {
//        if (!releaseTag.startsWith("v")) {
//            releaseTag = "v" + releaseTag;
//        }
        return new URL("https://github.com/" + OWNER + "/" + REPO + "/releases/tag/" + releaseTag);
    }
    
    /**
     * Checks if there is a later version online and returns it.
     * 
     * @return the new version number, or null if there is none
     * 
     * @throws MalformedURLException something went wrong
     * @throws IOException something went wrong
     */
    public ModuleDescriptor.Version getLatestVersion(ModuleDescriptor.Version currentVersion) throws IOException {
        if (versions == null) {
            throw new IllegalStateException("versions is null. Use init()");
        }
        
        if (!versions.isEmpty()) {
            Collections.sort(versions);
            log.debug("versions {}", versions);
            ModuleDescriptor.Version latest = versions.get(versions.size()-1);
            log.debug("version me={} latest={}", currentVersion, latest);
            
            if (latest.compareTo(currentVersion)>0) {
                log.debug("latest is greater!");
                return latest;
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
    public String getHtmlUserMessage(ModuleDescriptor.Version version) throws MalformedURLException {
        URL url = getHtmlReleaseURL(version.toString());
        
        StringBuilder html = new StringBuilder("<html><body>");
        html.append("<p>All right there. We heard rumors the new " + REPO);
        html.append(" version " + version + " has been released.</p>");
        html.append("<p>You need to check <a href=\"" + url + "\">" + url + "</a> and report back to me.</p>");
        html.append("<p>But don't keep me waiting too long, kid!</p>");
        html.append("</body></html>");
        return html.toString();
    }
    
    /**
     * Checks for updates of this software package and displays a message.
     * 
     * @param parentComponent the component upon which to present the message
     * @return true if an update was found and announced, false otherwise
     */
    public boolean maybeAnnounceUpdate(Component parentComponent, ModuleDescriptor.Version myVersion) {
        try {
            ModuleDescriptor.Version latest = getLatestVersion(myVersion);
            if (latest != null) {
                String message = getHtmlUserMessage(latest);
                EventQueue.invokeLater(() -> MrGimlet.showMessage(parentComponent, message, 10000) );
                return true;
            }
        } catch (IOException e) {
            log.info("Could not check for update", e);
        }
        return false;
    }
}
