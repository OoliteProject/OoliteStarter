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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import oolite.starter.model.Expansion;
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
            
            String resourcepaths = xpath.evaluate("/plist/dict/key[.='mission_variables']/following-sibling::dict/key[.='mission_ooliteStarter_oxpList']/following-sibling::string", doc);
            
            if (resourcepaths != null && !resourcepaths.isEmpty()) {
                List<SaveGame.ExpansionReference> expansions = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(resourcepaths, ",");
                st.nextToken(); // Resources
                String managedAddOnDir = st.nextToken(); // ManagedAddOns
                String addOnDir = st.nextToken(); // AddOns
                String myAddOn = addOnDir + File.separator + "org.oolite.hiran.OoliteStarter.oxp";
                String debugAddOn = addOnDir + File.separator + "Basic-debug.oxp";
                
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.startsWith(managedAddOnDir)) {
                        String name = token.substring(managedAddOnDir.length()+1);
                        expansions.add(getExpansionReference(name));
                    } else if (token.equals(debugAddOn)) {
                        // do nothing
                    } else if (token.equals(myAddOn) || token.endsWith("org.oolite.hiran.OoliteStarter.oxp")) {
                        // do nothing
                    } else if (token.startsWith(addOnDir)) {
                        String name = token.substring(addOnDir.length()+1);
                        expansions.add(getExpansionReference(name));
                    } else {
                        expansions.add(getExpansionReference(token));
                    }
                }
                
                Collections.sort(expansions);
                
                for (SaveGame.ExpansionReference s: expansions) {
                    log.warn("we have {}", s);
                }
                result.setExpansions(expansions);
            }

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
        
        injectExpansion();
        
        try {
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
        } finally {
            removeExpansion();
        }
    }
    
    /**
     * Runs Oolite for the given savegame.
     * Uses the pretty much only command line option supported by Oolite.
     * See https://github.com/OoliteProject/oolite/blob/58bf7e1efb01ac346d06da5271cf755c0cb4f55a/src/SDL/main.m#L102
     * 
     * @param savegame the game to run
     */
    public void run(SaveGame savegame) throws IOException, InterruptedException {
        log.debug("run({})", savegame);

        injectExpansion();
        
        try {
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
        } finally {
            removeExpansion();
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
                        case "identifier":
                            identifier = value;
                            break;
                        case "version":
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
    public Expansion createExpansion(PlistParser.ValueContext vc) throws IOException {
        log.debug("createExpansion({})", vc);
        return createExpansion(vc.dictionary());
    }

    /**
     * Creates an Expansion from a dictionary context.
     * 
     * @param dc the dictionary context to read
     * @return the Expansion
     */    
    public Expansion createExpansion(PlistParser.DictionaryContext dc) throws IOException {
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
                        log.warn("Could not parse {} line {}:{}", kvc.start.getTokenSource().getSourceName(), kvc.start.getLine(), kvc.start.getCharPositionInLine(), e);
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
                log.warn("Could not read from " + url, e);
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
                                expansion.setOolite(this);
                                expansion.setLocalFile(f);
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
        
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(source);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nl = (NodeList)xpath.evaluate("/ExpansionList/Expansion", doc, XPathConstants.NODESET);
        
        TreeMap<String, String> enabledAddons = new TreeMap<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element)nl.item(i);
            enabledAddons.put(e.getAttribute("identifier") + ":" + e.getAttribute("version"), e.getAttribute("downloadUrl"));
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

        // now install what may be missing
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
                log.info("{} is already installed & enabled - doing nothing");
            } else if (expansion.isLocal() && !expansion.isEnabled()) {
                log.info("{} is already installed but disabled - enabling");
                pm.setNote("enabling "+ i);
                expansion.enable();
            } else {
                log.info("{} is not installed - installing");
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
    public void exportEnabledExpansions(File destination) throws IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
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
                element.setAttribute("identifier", expansion.getIdentifier());
                element.setAttribute("version", expansion.getVersion());
                element.setAttribute("downloadUrl", expansion.getDownloadUrl());
                root.appendChild(element);
            }
        }
        
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.transform(new DOMSource(doc), new StreamResult(destination));
    }
    
    /**
     * Validates whether the list of expansions is acceptable in itself.
     * 
     * @param expansions the expansions to check
     * @return the list of discrepancies found
     */
    public List<SaveGame.ExpansionReference> validateDependencies(List<Expansion> expansions) throws IOException {
        log.debug("validateDependencies(...)");
        
        List<SaveGame.ExpansionReference> result =  new ArrayList<>();
                
        for (Expansion expansion: expansions) {
            if (!expansion.isEnabled()) {
                continue;
            }                
            
            if (expansion.getRequiresOxps() != null) {
                for (String dependency: expansion.getRequiresOxps()) {
                    int idx = dependency.indexOf(":");
                    if (idx >= 0) {
                        dependency = dependency.substring(0, idx);
                    }
                    
                    SaveGame.ExpansionReference er = getExpansionReference(dependency);
                    if (er.status == SaveGame.ExpansionReference.Status.missing) {
                        result.add(er);
                    }
                }
            }

            if (expansion.getConflictOxps() != null) {
                for (String dependency: expansion.getConflictOxps()) {
                    int idx = dependency.indexOf(":");
                    if (idx >= 0) {
                        dependency = dependency.substring(0, idx);
                    }

                    SaveGame.ExpansionReference er = getExpansionReference(dependency);
                    if (er.status == SaveGame.ExpansionReference.Status.ok) {
                        er.status = SaveGame.ExpansionReference.Status.surplus;
                        result.add(er);
                    }
                }
            }
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
        
        URL src = getClass().getResource("/" + "org.oolite.hiran.OoliteStarter.oxp" + ".zip");
        log.warn("src={}", src);
        String filename = new File(src.getFile()).getName();
        if (filename.endsWith(".zip")) {
            filename = filename.substring(0, filename.length()-4);
        }
        log.warn("srcFile={}", filename);
        File destDir = new File(configuration.getAddonsDir(), filename);
        log.warn("dst={}", destDir);
        
        try (ZipInputStream zis = new ZipInputStream(src.openStream())) {
            ZipEntry zEntry = null;
            while ( (zEntry = zis.getNextEntry()) != null ) {
                log.info("ZipEntry {}", zEntry.getName());
                File dest = new File(destDir, zEntry.getName());
                if (zEntry.isDirectory()) {
                    dest.mkdirs();
                } else {
                    OutputStream os = new FileOutputStream(dest);
                    IOUtils.copy(zis, os, 4096);
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
        File destDir = new File(configuration.getAddonsDir(), "org.oolite.hiran.OoliteStarter.oxp");
        log.info("removing {}", destDir);
        FileUtils.deleteDirectory(destDir);
    }
    
    protected SaveGame.ExpansionReference getExpansionReference(String name) {
        SaveGame.ExpansionReference result = new SaveGame.ExpansionReference();
        result.name = name;
        result.status = SaveGame.ExpansionReference.Status.missing;
        
        // find a direct match
        if (
                new File(configuration.getAddonsDir(), name).exists()
            ||
                new File(configuration.getManagedAddonsDir(), name).exists()
            ) {
            result.status = SaveGame.ExpansionReference.Status.ok;
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
                result.status = SaveGame.ExpansionReference.Status.ok;
                return result;
            }
        }
        files = configuration.getManagedAddonsDir().listFiles();
        for (File f: files) {
            if (f.getName().contains(id)) {
                result.status = SaveGame.ExpansionReference.Status.ok;
                return result;
            }
        }

        return result;
    }
}
