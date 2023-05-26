/*
 */
package oolite.starter;

import com.chaudhuri.plist.PlistParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import oolite.starter.model.Expansion;
import oolite.starter.model.SaveGame;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author hiran
 */
public class Oolite {
    private static final Logger log = LogManager.getLogger();
    
    public interface OoliteListener {
        
        /**
         * Will be called whenever Oolite is started.
         */
        public void launched();
        
        /**
         * Will be called whenever Oolite has terminated.
         */
        public void terminated();
        
    }
    
    private List<OoliteListener> listeners;
    private Configuration configuration;
    
    /**
     * Creates a new Oolite instance.
     */
    public Oolite() {
        log.debug("Oolite()");
        this.listeners = new ArrayList<>();
    }
    
    /**
     * Registers an OoliteListener to be notified.
     * 
     * @param l the listener
     */
    public void addOoliteListener(OoliteListener l) {
        listeners.add(l);
    }
    
    /**
     * Unregisters an OoliteListener to be notified.
     * 
     * @param l the listener
     */
    public void removeOoliteListener(OoliteListener l) {
        listeners.remove(l);
    }
    
    /**
     * Sets the configuration to be used for this Oolite/installation environment.
     * @param configuration 
     */
    public void setConfiguration(Configuration configuration) {
        log.debug("setConfiguration({})", configuration);
        this.configuration = configuration;
    }
    
    /**
     * Returns the list of SaveGames.
     * 
     * @return the list
     * @throws IOException something went wrong
     * @throws ParserConfigurationException something went wrong
     * @throws SAXException something went wrong
     * @throws XPathExpressionException something went wrong
     */
    public List<SaveGame> getSaveGames() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.debug("getSaveGames()");
        
        if (configuration == null) {
            throw new IllegalStateException("configuration must not be null");
        }
        
        List<SaveGame> result = new ArrayList<>();
        
        File[] files = configuration.getSaveGameDir().listFiles();
        if (files != null) {
            for (File f: files) {
                if (f.getName().toLowerCase().endsWith(".oolite-save")) {
                    result.add(createSaveGame(f));
                }
            }
        }
        
        return result;
    }
    
    protected SaveGame createSaveGame(File f) throws IOException {
        SaveGame result = new SaveGame();
        
        result.setName(f.getName().substring(0, f.getName().length()-12));
        result.setFile(f);
        
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            dbf.setFeature("http://xml.org/sax/features/namespaces", false);
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(f);

            XPath xpath = XPathFactory.newInstance().newXPath();
            result.setPlayerName(xpath.evaluate("/plist/dict/key[.='player_name']/following-sibling::string", doc));
            result.setCredits(Long.parseLong(xpath.evaluate("/plist/dict/key[.='credits']/following-sibling::real", doc)));
            result.setCurrentSystemName(xpath.evaluate("/plist/dict/key[.='current_system_name']/following-sibling::string", doc));
            result.setOoliteVersion(xpath.evaluate("/plist/dict/key[.='written_by_version']/following-sibling::string", doc));
            result.setShipKills(Long.parseLong(xpath.evaluate("/plist/dict/key[.='ship_kills']/following-sibling::integer", doc)));
            result.setShipClassName(xpath.evaluate("/plist/dict/key[.='ship_class_name']/following-sibling::string", doc));
            result.setShipName(xpath.evaluate("/plist/dict/key[.='ship_unique_name']/following-sibling::string", doc));

        } catch (SAXException | XPathExpressionException | ParserConfigurationException e) {
            throw new IOException("Could not parse " + f.getAbsolutePath(), e);
        }

        return result;
    }
    
    /**
     * Runs Oolite.
     */
    public void run() throws IOException, InterruptedException {
        log.debug("run()");
        
        ProcessBuilder pb = new ProcessBuilder(configuration.getOoliteCommand());
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();
        
        for (OoliteListener l: listeners) {
            try {
                l.launched();
            } catch (Exception e) {
                log.warn("Listener threw exception", e);
            }
        }
        p.waitFor();
        for (OoliteListener l: listeners) {
            try {
                l.terminated();
            } catch (Exception e) {
                log.warn("Listener threw exception", e);
            }
        }
    }
    
    /**
     * Runs Oolite for the given savegame.
     * 
     * @param savegame the game to run
     */
    public void run(SaveGame savegame) throws IOException, InterruptedException {
        log.debug("run({})", savegame);

        List<String> command = new ArrayList<>();
        command.add(configuration.getOoliteCommand());
        command.add("-load");
        command.add(savegame.getFile().getAbsolutePath());
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process p = pb.start();

        for (OoliteListener l: listeners) {
            l.launched();
        }
        p.waitFor();
        for (OoliteListener l: listeners) {
            l.terminated();
        }
    }
        
    /**
     * Creates an Expansion from a value context.
     * 
     * @param vc the value context to read
     * @return the Expansion
     */
    public Expansion createExpansion(PlistParser.ValueContext vc) throws IOException {
        log.debug("createExpansion(...)");
        return createExpansion(vc.dictionary());
    }

    /**
     * Creates an Expansion from a dictionary context.
     * 
     * @param dc the dictionary context to read
     * @return the Expansion
     */    
    public Expansion createExpansion(PlistParser.DictionaryContext dc) throws IOException {
        log.debug("createExpansion(...)");
        Expansion result = new Expansion();
        for (PlistParser.KeyvaluepairContext kvc: dc.keyvaluepair()) {
            String key = kvc.STRING().getText();
            String value = kvc.value().getText();
            
            switch (key) {
                case "author":
                    result.setAuthor(value);
                    break;
                case "category":
                    result.setCategory(value);
                    break;
                case "conflict_oxps":
                    result.setConflictOxps(value);
                    break;
                case "description":
                    result.setDescription(value);
                    break;
                case "download_url":
                    result.setDownloadUrl(value);
                    break;
                case "file_size":
                    result.setFileSize(Long.parseLong(value));
                    break;
                case "identifier":
                    result.setIdentifier(value);
                    break;
                case "information_url":
                    result.setInformationUrl(value);
                    break;
                case "license":
                    result.setLicense(value);
                    break;
                case "maximum_oolite_version":
                    result.setMaximumOoliteVersion(value);
                    break;
                case "optional_oxps":
                    result.setOptionalOxps(value);
                    break;
                case "required_oolite_version":
                    result.setRequiredOoliteVersion(value);
                    break;
                case "requires_oxps":
                    result.setRequiresOxps(value);
                    break;
                case "tags":
                    result.setTags(value);
                    break;
                case "title":
                    result.setTitle(value);
                    break;
                case "upload_date":
                    try {
                        result.setUploadDate(LocalDateTime.ofEpochSecond(Long.parseLong(value), 0, ZoneOffset.UTC));
                    } catch (NumberFormatException e) {
                        throw new IOException("Could not parse " + kvc.start.getTokenSource().getSourceName() + " line " + kvc.start.getLine() + ":" + kvc.start.getCharPositionInLine(), e);
                    }
                    break;
                case "version":
                    result.setVersion(value);
                    break;
                default:
                    log.warn("unknown {}->{}", key, value);
            }
        }
        return result;
    }
    
    /**
     * Creates an Expansion from a manifest stream.
     * 
     * @param manifest the input stream to read (typically from a file or zipfile).
     * @param sourceName name of the source for the input stream
     * @return the Expansion
     */    
    public Expansion createExpansion(InputStream manifest, String sourceName) throws IOException {
        log.debug("createExpansion(...)");
        // parse plist, then create Expansion from that
        PlistParser.DictionaryContext dc = PlistUtil.parsePListDict(manifest, sourceName);
        return createExpansion(dc);
    }
    
    /**
     * Returns a list of online and local expansions.
     * 
     * @return the list
     */
    public List<Expansion> getAllExpansions() throws MalformedURLException {
        log.debug("getAllExpansions()");
        List<Expansion> result = new ArrayList<>();
        List<Expansion> locals = getLocalExpansions();
        List<Expansion> remote = getOnlineExpansions();
        
        locals.addAll(remote);
        for (Expansion e: locals) {
            if (result.contains(e)) {
                int index = result.indexOf(e);
                Expansion h = result.get(index);
                log.trace("we have this already: {} at position {}", e, index);
                log.trace("have: {}{} {}{}{}", h.getIdentifier(), h.getVersion(), h.isOnline(), h.isLocal(), h.isEnabled());
                log.trace("want: {}{} {}{}{}", e.getIdentifier(), e.getVersion(), e.isOnline(), e.isLocal(), e.isEnabled());
                
                h.setOnline( h.isOnline() || e.isOnline());
                if (e.getLocalFile() != null) {
                    h.setLocalFile(e.getLocalFile());
                }
            } else {
                result.add(e);
            }
        }
        
        Collections.sort(result);
        return result;
    }

    /**
     * Returns the list of expansion available for download.
     * 
     * @return the list
     */
    public List<Expansion> getOnlineExpansions() throws MalformedURLException {
        log.debug("getOnlineExpansion()");
        if (configuration == null) {
            throw new IllegalStateException("configuration must not be null");
        }

        List<Expansion> result = new ArrayList<>();
        
        for (URL url: configuration.getExpansionManagerURLs()) {
            log.debug("downloading {}", url);
            try (InputStream in = url.openStream()) {
                PlistParser.ListContext lc = PlistUtil.parsePListList(in, url.toString());
                
                for (PlistParser.ValueContext vc: lc.value()) {
                    Expansion expansion = createExpansion(vc);
                    expansion.setOnline(true);
                    expansion.setOolite(this);
                    result.add(expansion);
                }
                
            } catch (Exception e) {
                log.error("Could not read from " + url, e);
            }
        }
        
        Collections.sort(result);
        return result;
    }
    
    /**
     * Returns the list of locally available expansions.
     * Algorithm should be similar to Oolite's implementation.
     * 
     * @see https://github.com/OoliteProject/oolite/blob/master/src/Core/ResourceManager.m#L277
     * @return the list
     */
    public List<Expansion> getLocalExpansions() {
        log.debug("getLocalExpansions()");
        if (configuration == null) {
            throw new IllegalStateException("configuration must not be null");
        }
        
        List<Expansion> result = new ArrayList<>();
        
        String deactivated = configuration.getDeactivatedAddonsDir().getAbsolutePath();
        
        for (File dir: configuration.getAddonDirs()) {
            if (dir.isDirectory()) {
                log.debug("scanning {}", dir);
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f: files) {
                        try {
                            Expansion expansion = getExpansionFrom(f);
                        
                            if (expansion != null) {
                                expansion.setLocalFile(f);
                                expansion.setOolite(this);
                                result.add(expansion);
                            }
                        } catch (Exception e) {
                            log.warn("Could not read expansion in {}", f, e);
                        }
                    }
                }
            }
        }
        
        Collections.sort(result);
        return result;
    }

    private Expansion getExpansionFrom(File f) {
        log.debug("getExpansionsFrom({})", f);
        try {
            if (f.isDirectory()) {
                // if it is a directory, is it an OXP?
                if (f.getName().toLowerCase().endsWith(".oxp")) {
                    return getExpansionFromOxp(f);
                } else {
                    // not a subdirectory, but we do not scan subdirectories [sic]
                }
            } else {
                // if not a directory, is it an OXZ?
                if (f.getName().toLowerCase().endsWith(".oxz")) {
                    return getExpansionFromOxz(f);
                }
            }
        } catch (IOException e) {
            log.warn("Could not make sense out of {}", f, e);
        }
        
        return null;
    }
    
    private Expansion getExpansionFromOxp(File f) throws IOException {
        log.debug("getExpansionsFromOxp({})", f);
        File manifestFile = new File(f, "manifest.plist");
        if (!manifestFile.isFile()) {
            return null;
        }

        InputStream stream = new FileInputStream(manifestFile);
        return createExpansion(stream, manifestFile.getAbsolutePath());
    }
    
    private Expansion getExpansionFromOxz(File f) throws IOException {
        log.debug("getExpansionsfromOxz({})", f);
        
        ZipFile zipFile = new ZipFile(f);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            
            if ("manifest.plist".equals(entry.getName())) {
                InputStream stream = zipFile.getInputStream(entry);
                return createExpansion(stream, f.getAbsolutePath() + "!" + entry.getName());
            } else {
                // keep quiet log.debug("ignoring zipentry {}", entry.getName());
            }
        }
        
        // still here? then this was no OXZ...
        return null;
    }
    
    /**
     * Installs the given expansion.
     * 
     * @param expansion the expansion
     */
    public void install(Expansion expansion) throws IOException {
        log.debug("install({})", expansion);
        URL url = new URL(expansion.getDownloadUrl());
        
        String filename = url.getFile();
        int index = filename.lastIndexOf("/");
        if (index >=0) {
            filename = filename.substring(index);
        }
        
        File file = new File(configuration.getManagedAddonsDir(), expansion.getIdentifier() + expansion.getVersion() + ".oxz");
        downloadUrl(url, file);
        
        if (!file.isFile())
            throw new IOException("expected file to exist after download: " + file.getAbsolutePath());
        
        expansion.setLocalFile(file);
    }
    
    /**
     * Enables the given expansion.
     * 
     * @param expansion the expansion
     */
    public void enable(Expansion expansion) throws IOException {
        log.debug("enable({})", expansion);

        log.debug("Move {} to {}", expansion.getLocalFile(), configuration.getManagedAddonsDir());
        FileUtils.moveFileToDirectory(expansion.getLocalFile(), configuration.getManagedAddonsDir(), true);
        expansion.setLocalFile(new File(configuration.getManagedAddonsDir(), expansion.getLocalFile().getName()));
    }
    
    /**
     * Disables the given expansion.
     * 
     * @param expansion the expansion
     */
    public void disable(Expansion expansion) throws IOException {
        log.debug("disable({})", expansion);
        
        log.debug("Move {} to {}", expansion.getLocalFile(), configuration.getDeactivatedAddonsDir());
        FileUtils.moveFileToDirectory(expansion.getLocalFile(), configuration.getDeactivatedAddonsDir(), true);
        expansion.setLocalFile(new File(configuration.getDeactivatedAddonsDir(), expansion.getLocalFile().getName()));
    }
    
    /**
     * Removes the given expansion.
     * 
     * @param expansion the expansion
     */
    public void remove(Expansion expansion) throws IOException {
        log.debug("remove({})", expansion);

        log.debug("Remove {}", expansion.getLocalFile());
        FileUtils.delete(expansion.getLocalFile());
        expansion.setLocalFile(null);
    }
    
    private void downloadUrl(URL url, File file) throws IOException {
        log.debug("downloadUrl({}, {})", url, file);
        
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setReadTimeout(5000);

        int status = conn.getResponseCode();
        log.info("HTTP status for {}: {}", url, status);
        
        while (status != HttpURLConnection.HTTP_OK) {
            String newUrl = conn.getHeaderField("Location");
            conn = (HttpURLConnection)new URL(newUrl).openConnection();
            conn.setReadTimeout(5000);
            status = conn.getResponseCode();
            log.info("HTTP status for {}: {}", newUrl, status);
        }
        
        try (InputStream in = conn.getInputStream()) {
            FileUtils.copyToFile(in, file);
        }
    }
    
    /**
     * Determines if an Expansion is part of the deactivated expansions directory.
     * 
     * @param expansion the expansion to test
     * @return true if and only if it is deactivated
     */
    public boolean isDisabled(Expansion expansion) throws IOException {
        File parent = configuration.getDeactivatedAddonsDir();
        File test = expansion.getLocalFile();
        
        return FileUtils.directoryContains(parent, test);
    }
}
