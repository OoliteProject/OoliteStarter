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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
     * Uses the pretty much only command line option supported by Oolite.
     * See https://github.com/OoliteProject/oolite/blob/58bf7e1efb01ac346d06da5271cf755c0cb4f55a/src/SDL/main.m#L102
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

        if (expansion.getLocalFile().isFile()) {
            FileUtils.moveFileToDirectory(expansion.getLocalFile(), configuration.getManagedAddonsDir(), true);
        } else if (expansion.getLocalFile().isDirectory()) {
            FileUtils.moveDirectoryToDirectory(expansion.getLocalFile(), configuration.getManagedAddonsDir(), true);
        }
        
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
        if (expansion.getLocalFile().isFile()) {
            FileUtils.moveFileToDirectory(expansion.getLocalFile(), configuration.getDeactivatedAddonsDir(), true);
        } else if (expansion.getLocalFile().isDirectory()) {
            FileUtils.moveDirectoryToDirectory(expansion.getLocalFile(), configuration.getDeactivatedAddonsDir(), true);
        }
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
     */
    public List<String> validateDependencies(List<Expansion> expansions) throws IOException {
        log.debug("validateDependencies(...)");
        
        List<String> warnings =  new ArrayList<>();
        
        Map<String, Expansion> indexedWithVersion = new TreeMap<>();
        for (Expansion expansion: expansions) {
            indexedWithVersion.put(expansion.getIdentifier() + ":" + expansion.getVersion(), expansion);
        }
        Map<String, Expansion> indexed = new TreeMap<>();
        for (Expansion expansion: expansions) {
            indexed.put(expansion.getIdentifier(), expansion);
        }
        
        for (Expansion expansion: expansions) {
            if (expansion.getRequiresOxps() != null) {
                for (String dependency: expansion.getRequiresOxps()) {
                    if (!indexedWithVersion.containsKey(dependency)) {
                        
                        String dep = dependency.substring(dependency.lastIndexOf(":"));
                        if (!indexed.containsKey(dep)) {
                            warnings.add(String.format("Expansion %s:%s: cannot find required %s", expansion.getIdentifier(), expansion.getVersion(), dependency));
                        }
                    }
                }
            }
            
        }
        
        return warnings;
    }
}
