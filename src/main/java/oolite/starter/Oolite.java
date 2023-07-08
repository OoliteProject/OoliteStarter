/*
 */
package oolite.starter;

import com.chaudhuri.plist.PlistParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.swing.ProgressMonitor;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import oolite.starter.model.Expansion;
import oolite.starter.model.ExpansionReference;
import oolite.starter.model.SaveGame;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author hiran
 */
public class Oolite {
    private static final Logger log = LogManager.getLogger();
    
    private static final String OOLITE_CONFIGURATION_MUST_NOT_BE_NULL = "configuration must not be null";
    private static final String OOLITE_EXPANSION_FQN = "org.oolite.hiran.OoliteStarter.oxp";
    private static final String OOLITE_IDENTIFIER = "identifier";
    private static final String OOLITE_VERSION = "version";
    
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
            throw new IllegalStateException(OOLITE_CONFIGURATION_MUST_NOT_BE_NULL);
        }
        
        List<SaveGame> result = new ArrayList<>();
        
        File[] files = configuration.getSaveGameDir().listFiles();
        if (files != null) {
            for (File f: files) {
                if (f.getName().toLowerCase().endsWith(".oolite-save")) {
                    try {
                        result.add(createSaveGame(f));
                    } catch (Exception e) {
                        log.warn("Skipping savegame {}", f.getAbsolutePath(), e);
                    }
                }
            }
        }
        
        return result;
    }
    
    protected SaveGame createSaveGame(Document doc) throws XPathExpressionException {
        log.debug("createSaveGame({})", doc);
        SaveGame result = new SaveGame();
        XPath xpath = XPathFactory.newInstance().newXPath();
        result.setPlayerName(xpath.evaluate("/plist/dict/key[.='player_name']/following-sibling::string", doc));
        result.setCredits(Long.parseLong(xpath.evaluate("/plist/dict/key[.='credits']/following-sibling::real", doc)));
        result.setCurrentSystemName(xpath.evaluate("/plist/dict/key[.='current_system_name']/following-sibling::string", doc));
        result.setOoliteVersion(xpath.evaluate("/plist/dict/key[.='written_by_version']/following-sibling::string", doc));
        result.setShipKills(Long.parseLong(xpath.evaluate("/plist/dict/key[.='ship_kills']/following-sibling::integer", doc)));
        result.setShipClassName(xpath.evaluate("/plist/dict/key[.='ship_class_name']/following-sibling::string", doc));
        result.setShipName(xpath.evaluate("/plist/dict/key[.='ship_unique_name']/following-sibling::string", doc));

        String resourcepaths = xpath.evaluate("/plist/dict/key[.='mission_variables']/following-sibling::dict/key[.='mission_ooliteStarter_oxpList']/following-sibling::string", doc);

        if (resourcepaths != null && !resourcepaths.isEmpty()) {
            List<ExpansionReference> expansions = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(resourcepaths, ",");
            st.nextToken(); // Resources
            String managedAddOnDir = st.nextToken(); // ManagedAddOns
            String addOnDir = st.nextToken(); // AddOns
            String myAddOn = addOnDir + File.separator + OOLITE_EXPANSION_FQN;
            String debugAddOn = addOnDir + File.separator + "Basic-debug.oxp";

            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.startsWith(managedAddOnDir)) {
                    String name = token.substring(managedAddOnDir.length()+1);
                    expansions.add(getExpansionReference(name));
                } else if (token.equals(debugAddOn)) {
                    // do nothing
                } else if (token.equals(myAddOn) || token.endsWith(OOLITE_EXPANSION_FQN)) {
                    // do nothing
                } else if (token.startsWith(addOnDir)) {
                    String name = token.substring(addOnDir.length()+1);
                    expansions.add(getExpansionReference(name));
                } else {
                    expansions.add(getExpansionReference(token));
                }
            }

            Collections.sort(expansions);

            result.setExpansions(expansions);
        }
        return result;
    }
    
    protected boolean contains(List<ExpansionReference> list, Expansion expansion) {
        for (ExpansionReference ref: list) {
            if (expansion.getLocalFile().getName().endsWith(ref.getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks which active expansions are not contained in the expansion
     * references list.
     * If an expansion is active but not contained in the original list it will
     * be added and marked as 'surplus'.
     * 
     * @param references 
     */
    protected void checkSurplusExpansions(List<ExpansionReference> references) {
        if (references == null) {
            throw new IllegalArgumentException("references must not be null");
        }
        List<Expansion> localExpansions = getLocalExpansions();
        List<ExpansionReference> surplus = new ArrayList<>();
        
        for (Expansion expansion: localExpansions) {
            if (!OOLITE_EXPANSION_FQN.equals(expansion.getIdentifier()) 
                && !"org.oolite.oolite.debug".equals(expansion.getIdentifier())
                && !contains(references, expansion)
            ) {
                // add a SURPLUS
                ExpansionReference ref = new ExpansionReference();
                ref.setName(expansion.getIdentifier() + "@" + expansion.getVersion());
                ref.setStatus(ExpansionReference.Status.SURPLUS);
                surplus.add(ref);
            }
        }
        
        references.addAll(surplus);
    }
    
    protected SaveGame createSaveGame(File f) throws IOException {
        log.debug("createSaveGame({})", f);
        SaveGame result = null;
        
        try {
            Document doc = XmlUtil.parseXmlFile(f);
            result = createSaveGame(doc);
            
            if (result.getExpansions() != null) {
                // the savegame has it's references already checked. But
                // we need to find SURPLUS expansions...
                checkSurplusExpansions(result.getExpansions());
            }

        } catch (SAXException | XPathExpressionException | ParserConfigurationException e) {
            throw new IOException("Could not parse " + f.getAbsolutePath(), e);
        }

        result.setName(f.getName().substring(0, f.getName().length()-12));
        result.setFile(f);
        return result;
    }
    
    /**
     * Runs Oolite.
     */
    public void run() throws IOException, InterruptedException, ProcessRunException {
        log.debug("run()");

        String executable = configuration.getOoliteCommand();
        if (executable == null) {
            throw new IllegalStateException("active installation has no executable");
        }

        List<String> command = new ArrayList<>();
        command.add(executable);
        File dir = new File(executable).getParentFile();
        
        run(command, dir);
    }
    
    /**
     * Runs Oolite for the given savegame.
     * Uses the pretty much only command line option supported by Oolite.
     * See https://github.com/OoliteProject/oolite/blob/58bf7e1efb01ac346d06da5271cf755c0cb4f55a/src/SDL/main.m#L102
     * 
     * @param savegame the game to run
     */
    public void run(SaveGame savegame) throws IOException, InterruptedException, ProcessRunException {
        log.debug("run({})", savegame);

        List<String> command = new ArrayList<>();
        command.add(configuration.getOoliteCommand());
        command.add("-load");
        command.add(savegame.getFile().getAbsolutePath());
        File dir = new File(configuration.getOoliteCommand()).getParentFile();

        run(command, dir);
    }
    
    void fireLaunched() {
        for (OoliteListener l: listeners) {
            l.launched();
        }
    }
    
    void fireTerminated() {
        for (OoliteListener l: listeners) {
            l.terminated();
        }
    }
    
    /**
     * Runs Oolite using the specified command in the specified directory.
     */
    public void run(List<String> command, File dir) throws IOException, InterruptedException, ProcessRunException {
        log.debug("run({}, {})", command, dir);

        injectExpansion();
        
        try {
            log.info("executing {} in {}", command, dir);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(dir);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();

            fireLaunched();
            p.waitFor();
            fireTerminated();
            
            log.info("Process exited with code {}", p.exitValue());
            if (p.exitValue() != 0) {
                throw new ProcessRunException(String.format("Oolite terminated with code %d", p.exitValue()));
            }
            
        } finally {
            try {
                removeExpansion();
            } catch (Exception e) {
                log.info("Could not cleanup OoliteStarter.oxp after Oolite run.", e);
            }
        }            
    }
    
    /**
     * Parses a dependency list.
     * See https://wiki.alioth.net/index.php/Manifest.plist#Dependency_management_keys
     * @param vc 
     */
    private List<String> parseDependencyList(PlistParser.ValueContext vc) {
        log.debug("parseDependencyList({})", vc);
        
        List<String> result = new ArrayList<>();
        
        if (vc.list() != null) {
            for (PlistParser.ValueContext vc2: vc.list().value()) {
                PlistParser.DictionaryContext dict = vc2.dictionary();

                String identifier = "";
                String version = "";
                    
                for (PlistParser.KeyvaluepairContext kvc: dict.keyvaluepair()) {
                    String key = kvc.STRING().getText();
                    String value = kvc.value().getText();
                    switch(key) {
                        case OOLITE_IDENTIFIER:
                            identifier = value;
                            break;
                        case OOLITE_VERSION:
                            version = value;
                            break;
                        case "description":
                            break;
                        case "maximum_version":
                            break;
                        default:
                            log.warn("unknown dependency key {}", key);
                            break;
                    }
                }
                
                String id = identifier + ":" + version;
                result.add(id);
            }
        }
        
        Collections.sort(result);
        return result;
    }
        
    /**
     * Creates an Expansion from a value context.
     * 
     * @param vc the value context to read
     * @return the Expansion
     */
    public Expansion createExpansion(PlistParser.ValueContext vc) {
        log.debug("createExpansion({})", vc);
        return createExpansion(vc.dictionary());
    }

    /**
     * Creates an Expansion from a dictionary context.
     * 
     * @param dc the dictionary context to read
     * @return the Expansion
     */    
    public Expansion createExpansion(PlistParser.DictionaryContext dc) {
        log.debug("createExpansion({})", dc);
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
                    result.setConflictOxps(parseDependencyList(kvc.value()));
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
                case OOLITE_IDENTIFIER:
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
                    result.setOptionalOxps(parseDependencyList(kvc.value()));
                    break;
                case "required_oolite_version":
                    result.setRequiredOoliteVersion(value);
                    break;
                case "requires_oxps":
                    result.setRequiresOxps(parseDependencyList(kvc.value()));
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
                        log.warn("NumberFormatException on {} line {}:{}: {}", kvc.start.getTokenSource().getSourceName(), kvc.start.getLine(), kvc.start.getCharPositionInLine(), e.getMessage());
                    }
                    break;
                case OOLITE_VERSION:
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
        log.debug("createExpansion({}, {})", manifest, sourceName);
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
                
                h.setOnline( h.isOnline() || e.isOnline());
                if (e.getLocalFile() != null) {
                    h.setLocalFile(e.getLocalFile());
                }
                if (e.getDownloadUrl() != null) {
                    h.setDownloadUrl(e.getDownloadUrl());
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
            throw new IllegalStateException(OOLITE_CONFIGURATION_MUST_NOT_BE_NULL);
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
                log.warn("Could not read from {}", url, e);
            }
        }
        
        Collections.sort(result);
        return result;
    }
    
    List<Expansion> getLocalExpansions(File dir) {
        log.debug("scanning {}", dir);
        List<Expansion> result = new ArrayList<>();
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f: files) {
                try {
                    Expansion expansion = getExpansionFrom(f);

                    if (expansion != null) {
                        expansion.setOolite(this);
                        expansion.setLocalFile(f);
                        result.add(expansion);
                    }
                } catch (Exception e) {
                    log.warn("Could not read expansion in {}", f, e);
                }
            }
        }
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
            throw new IllegalStateException(OOLITE_CONFIGURATION_MUST_NOT_BE_NULL);
        }
        
        List<Expansion> result = new ArrayList<>();
        
        for (File dir: configuration.getAddonDirs()) {
            if (dir.isDirectory()) {
                result.addAll(getLocalExpansions(dir));
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
        
        try (ZipFile zipFile = new ZipFile(f)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if ("manifest.plist".equals(entry.getName())) {
                    InputStream stream = zipFile.getInputStream(entry);
                    return createExpansion(stream, f.getAbsolutePath() + "!" + entry.getName());
                } else {
                    log.trace("ignoring zipentry {}", entry.getName());
                }
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
        
        File file = new File(configuration.getManagedAddonsDir(), expansion.getIdentifier() + "@" + expansion.getVersion() + ".oxz");
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

        File destination = null;
        if (isManaged(expansion)) {
            destination = configuration.getManagedAddonsDir();
        } else {
            destination = configuration.getAddonsDir();
        }
        
        log.debug("Move {} to {}", expansion.getLocalFile(), destination);

        if (expansion.getLocalFile().isFile()) {
            FileUtils.moveFileToDirectory(expansion.getLocalFile(), destination, true);
        } else if (expansion.getLocalFile().isDirectory()) {
            FileUtils.moveDirectoryToDirectory(expansion.getLocalFile(), destination, true);
        }
        
        expansion.setLocalFile(new File(destination, expansion.getLocalFile().getName()));
    }
    
    /**
     * Disables the given expansion.
     * 
     * @param expansion the expansion
     */
    public void disable(Expansion expansion) throws IOException {
        log.debug("disable({})", expansion);
        
        File destination = null;
        if (isManaged(expansion)) {
            destination = configuration.getManagedDeactivatedAddonsDir();
        } else {
            destination = configuration.getDeactivatedAddonsDir();
        }
        
        log.debug("Move {} to {}", expansion.getLocalFile(), destination);
        if (expansion.getLocalFile().isFile()) {
            FileUtils.moveFileToDirectory(expansion.getLocalFile(), destination, true);
        } else if (expansion.getLocalFile().isDirectory()) {
            FileUtils.moveDirectoryToDirectory(expansion.getLocalFile(), destination, true);
        }
        expansion.setLocalFile(new File(destination, expansion.getLocalFile().getName()));
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
     * Determines if an Expansion is managed.
     * 
     * @param expansion the expansion to test
     * @return true if and only if it is managed
     */
    public boolean isManaged(Expansion expansion) throws IOException {
        File test = expansion.getLocalFile();
        if (test == null)
            return false;
        
        return FileUtils.directoryContains(configuration.getManagedDeactivatedAddonsDir(), test)
                || FileUtils.directoryContains(configuration.getManagedAddonsDir(), test);
    }
    
    /**
     * Determines if an Expansion can be found by Oolite.
     * 
     * @param expansion the expansion to test
     * @return true if and only if it is activated
     */
    public boolean isEnabled(Expansion expansion) throws IOException {
        File test = expansion.getLocalFile();
        if (test == null)
            return false;
        
        return FileUtils.directoryContains(configuration.getAddonsDir(), test)
                || FileUtils.directoryContains(configuration.getManagedAddonsDir(), test);
    }
    
    /**
     * Determines if an Expansion is part of the deactivated expansions directory.
     * 
     * @param expansion the expansion to test
     * @return true if and only if it is deactivated
     */
    public boolean isDisabled(Expansion expansion) throws IOException {
        return !isEnabled(expansion);
    }
    
    /**
     * Parses the list of expansions from a expansion set xml file
     * and esures only the right ones are enabled.
     * 
     * @param source the file to read from
     * @param expansions the expansions to work on
     */
    public void setEnabledExpansions(File source, List<Expansion> expansions, ProgressMonitor pm) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        pm.setMinimum(0);
        pm.setMaximum(expansions.size() + 2);
        int progress = 0;
        
        pm.setProgress(progress);
        pm.setNote("parseing " + source.getName() + "...");
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // to be compliant, completely disable DOCTYPE declaration:
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        // or completely disable external entities declarations:
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        // or prohibit the use of all protocols by external entities:
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        // or disable entity expansion but keep in mind that this doesn't prevent fetching external entities
        // and this solution is not correct for OpenJDK < 13 due to a bug: https://bugs.openjdk.java.net/browse/JDK-8206132
        dbf.setExpandEntityReferences(false);
        
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(source);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nl = (NodeList)xpath.evaluate("/ExpansionList/Expansion", doc, XPathConstants.NODESET);
        
        TreeMap<String, String> enabledAddons = new TreeMap<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element)nl.item(i);
            enabledAddons.put(e.getAttribute(OOLITE_IDENTIFIER) + ":" + e.getAttribute(OOLITE_VERSION), e.getAttribute("downloadUrl"));
        }

        progress++;
        pm.setMaximum(expansions.size() + enabledAddons.size() + 1);
        pm.setProgress(progress);
        log.debug("we want: {}", enabledAddons);
        
        
        // first remove what we do not need
        pm.setNote("Deactivating unneeded expansions...");
        for (Expansion expansion: expansions) {
            String i = expansion.getIdentifier() + ":" + expansion.getVersion();
            if (expansion.isLocal() && expansion.isEnabled() && !enabledAddons.containsKey(i)) {
                expansion.disable();
            }
            progress++;
            pm.setProgress(progress);
        }

        // now install what may be MISSING
        pm.setNote("Installing missing expansions...");
        TreeMap<String, Expansion> expansionMap = new TreeMap<>();
        for (Expansion expansion: expansions) { 
            expansionMap.put(expansion.getIdentifier() + ":" + expansion.getVersion(), expansion);
        }
        for (String i: enabledAddons.keySet()) {
            log.debug("checking {}", i);
            Expansion expansion = expansionMap.get(i);
            if (expansion == null) {
                log.error("Don't know how to handle {}", i);
            } else if (expansion.isLocal() && expansion.isEnabled()) {
                // already here - do nothing
                log.info("{} is already installed & enabled - doing nothing", i);
            } else if (expansion.isLocal() && !expansion.isEnabled()) {
                log.info("{} is already installed but disabled - enabling", i);
                pm.setNote("enabling "+ i);
                expansion.enable();
            } else {
                log.info("{} is not installed - installing", i);
                pm.setNote("installing "+ i);
                expansion.install();
            }
            progress ++;
            pm.setProgress(progress);
        }
        
    }
    
    /**
     * Writes the list of currently enabled expansions as xml file.
     * 
     * @param destination the file to write to
     */
    public void exportEnabledExpansions(File destination) throws IOException, ParserConfigurationException, TransformerException {
        List<Expansion> expansions = getAllExpansions();
        
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        Element root = doc.createElement("ExpansionList");
        root.setAttribute("generatedAt", Instant.now().toString());
        root.setAttribute("generatedBy", getClass().getPackage().getImplementationTitle() + " " + getClass().getPackage().getImplementationVersion());
        doc.appendChild(root);
        
        for (Expansion expansion: expansions) {
            if (expansion.isLocal() && expansion.isEnabled()) {
                Element element = doc.createElement("Expansion");
                element.setAttribute(OOLITE_IDENTIFIER, expansion.getIdentifier());
                element.setAttribute(OOLITE_VERSION, expansion.getVersion());
                element.setAttribute("downloadUrl", expansion.getDownloadUrl());
                root.appendChild(element);
            }
        }
        
        TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();
        // to be compliant, prohibit the use of all protocols by external entities:
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer t = factory.newTransformer();
        t.transform(new DOMSource(doc), new StreamResult(destination));
    }
    
    void validateRequirements(Expansion expansion, List<ExpansionReference> result) {
        if (expansion.getRequiresOxps() != null) {
            for (String dependency: expansion.getRequiresOxps()) {
                int idx = dependency.indexOf(":");
                if (idx >= 0) {
                    dependency = dependency.substring(0, idx);
                }

                ExpansionReference er = getExpansionReference(dependency);
                if (er.getStatus() == ExpansionReference.Status.MISSING) {
                    result.add(er);
                }
            }
        }
    }
    
    void validateConflicts(Expansion expansion, List<ExpansionReference> result) {
        if (expansion.getConflictOxps() != null) {
            for (String dependency: expansion.getConflictOxps()) {
                int idx = dependency.indexOf(":");
                if (idx >= 0) {
                    dependency = dependency.substring(0, idx);
                }

                ExpansionReference er = getExpansionReference(dependency);
                if (er.getStatus() == ExpansionReference.Status.OK) {
                    er.setStatus(ExpansionReference.Status.SURPLUS);
                    result.add(er);
                }
            }
        }
    }
    
    /**
     * Validates whether the list of expansions is acceptable in itself.
     * 
     * @param expansions the expansions to check
     * @return the list of discrepancies found
     */
    public List<ExpansionReference> validateDependencies(List<Expansion> expansions) {
        log.debug("validateDependencies(...)");
        
        List<ExpansionReference> result =  new ArrayList<>();
                
        for (Expansion expansion: expansions) {
            if (!expansion.isEnabled()) {
                continue;
            }                
            
            validateRequirements(expansion, result);
            validateConflicts(expansion, result);
        }
        
        return result;
    }
    
    /**
     * Injects the Oolitestarter expansion into the Oolite AddOns folder.
     */
    public void injectExpansion() throws IOException {
        log.debug("injectExpansion()");
        
        // just to be sure we do not rely on old stuff
        removeExpansion();
        
        URL src = getClass().getResource("/" + OOLITE_EXPANSION_FQN + ".zip");
        log.debug("src={}", src);
        String filename = new File(src.getFile()).getName();
        if (filename.endsWith(".zip")) {
            filename = filename.substring(0, filename.length()-4);
        }
        log.debug("srcFile={}", filename);
        File destDir = new File(configuration.getAddonsDir(), filename);
        log.info("installing {}", destDir);
        
        try (ZipInputStream zis = new ZipInputStream(src.openStream())) {
            ZipEntry zEntry = null;
            while ( (zEntry = zis.getNextEntry()) != null ) {
                log.debug("ZipEntry {}", zEntry.getName());
                File dest = new File(destDir, zEntry.getName());
                if (zEntry.isDirectory()) {
                    dest.mkdirs();
                } else {
                    try (OutputStream os = new FileOutputStream(dest)) {
                        IOUtils.copy(zis, os, 4096);
                    }
                }
            }
        }
    }

    /**
     * Removes the Oolitestarter expansion from the Oolite AddOns folder.
     * 
     * @throws IOException something went wrong
     */
    public void removeExpansion() throws IOException {
        log.debug("removeExpansion()");
        
        File destDir = new File(configuration.getAddonsDir(), OOLITE_EXPANSION_FQN);
        if (! destDir.exists()) {
            return;
        }
        log.info("removing {}", destDir);
        FileUtils.deleteDirectory(destDir);
    }
    
    protected ExpansionReference getExpansionReference(String name) {
        ExpansionReference result = new ExpansionReference();
        result.setName(name);
        result.setStatus(ExpansionReference.Status.MISSING);
        
        // find a direct match
        if (
                new File(configuration.getAddonsDir(), name).exists()
            ||
                new File(configuration.getManagedAddonsDir(), name).exists()
            ) {
            result.setStatus(ExpansionReference.Status.OK);
            return result;
        }
        
        // find some indirect match. First strip off version number, then find substring
        String id = name;
        int idx = id.indexOf("@");
        if (idx >= 0) {
            id = id.substring(0, idx);
        }
        File[] files = configuration.getAddonsDir().listFiles();
        for (File f: files) {
            if (f.getName().contains(id)) {
                result.setStatus(ExpansionReference.Status.OK);
                return result;
            }
        }
        files = configuration.getManagedAddonsDir().listFiles();
        for (File f: files) {
            if (f.getName().contains(id)) {
                result.setStatus(ExpansionReference.Status.OK);
                return result;
            }
        }

        return result;
    }

    /**
     * Type of a directory related to Oolite.
     */
    public enum OoliteDirectoryType {
        HOME_DIR,
        EXPANSION_DIR,
        SAVEGAME_DIR
    }
    
    /**
     * Return true if we detect an Oolite home directory.
     * 
     * @param f the directory to inspect
     * @return true if and only if it is a home directory
     */
    static boolean isOoliteHomeDirectory(File f) {
        String fname = f.getName();
        if ("oolite.app".equals(fname)) {
            // check linux directory
            // check windows directory
            
            File app = null;
            app = new File(f, "oolite.exe");
            if (app.isFile()) {
                // we found oolite.exe on Windows!
                return true;
            }
            app = new File(f, "oolite");
            if (app.isFile()) {
                // we found oolite on Linux!
                return true;
            }
        }
        if ("Oolite.app".equals(fname)) {
            // check MacOS directory
            File app = null;
            app = new File(f, "Contents/MacOS/Oolite");
            if (app.isFile()) {
                // we found oolite.exe on Windows!
                return true;
            }
        }
        
        return false;
    }
    
    static boolean isOoliteExpansionDirectory(File f) {
        File[] children = f.listFiles();
        if (children != null) {
            for (File child: children) {
                if (child.getName().endsWith(".oxz")) {
                    return true;
                }
                
                if (child.getName().endsWith(".oxp") && new File(child, "Config").isDirectory()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    static boolean isOoliteSaveGameDirectory(File f) {
        File[] children = f.listFiles();
        if (children != null) {
            for (File child: children) {
                if (child.getName().endsWith(".oolite-save")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Determines the type of a directory for Oolite.
     * 
     * @param f
     * @return 
     */
    public static OoliteDirectoryType isOoliteRelevant(File f) {
        if (f == null) {
            return null;
        }

        if (isOoliteHomeDirectory(f)) {
            // this works for Windows and Linux
            return OoliteDirectoryType.HOME_DIR;
        }
        
        if (isOoliteExpansionDirectory(f)) {
            // this works for Windows and Linux
            return OoliteDirectoryType.EXPANSION_DIR;
        }
        
        if (isOoliteSaveGameDirectory(f)) {
            // this works for Windows and Linux
            return OoliteDirectoryType.SAVEGAME_DIR;
        }
        
        return null;
    }

    /**
     * Returns a description of the directory type.
     * 
     * @param odt
     * @return 
     */
    public static String getDescription(OoliteDirectoryType odt) {
        if (odt == null) {
            return "";
        }
        
        switch (odt) {
            case EXPANSION_DIR:
                return "This directory contains Oolite expansions.";
            case HOME_DIR:
                return "This directory contains an Oolite installation.";
            case SAVEGAME_DIR:
                return "This directory contains Oolite save games.";
            default:
                return "";
        }
    }

    /**
     * Extracts the Oolite version from Info.plist.
     * 
     * @param f the Info.plist file to read
     * @return the version number found
     * @throws ParserConfigurationException something went wrong
     * @throws SAXException something went wrong
     * @throws IOException something went wrong
     * @throws XPathExpressionException something went wrong
     */
    public static String getVersionFromInfoPlist(File f) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Document info = XmlUtil.parseXmlFile(f);
        
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        
        return xp.evaluate("/plist/dict/key[text()='CFBundleVersion']/following-sibling::string[1]", info);
    }
    
    /**
     * Extracts the Oolite version from Resources/manifest.plist.
     * 
     * @param f the plist file to read
     * @return the version number found
     * @throws ParserConfigurationException something went wrong
     * @throws SAXException something went wrong
     * @throws IOException something went wrong
     * @throws XPathExpressionException something went wrong
     */
    public static String getVersionFromManifest(File f) throws IOException {
        log.debug("getVersionFromManifest({})", f);
        
        try (InputStream in = new FileInputStream(f)) {
            PlistParser.DictionaryContext dc = PlistUtil.parsePListDict(in, f.getAbsolutePath());

            for (PlistParser.KeyvaluepairContext kvc: dc.keyvaluepair()) {
                String key = kvc.STRING().getText();
                String value = kvc.value().getText();
                
                log.trace("looking at key {} value {}", key, value);
                if ("version".equals(key)) {
                    return value;
                }
            }

            return null;
        }
    }
}
