/*
 */
package oolite.starter;

import oolite.starter.util.PlistUtil;
import oolite.starter.util.Util;
import oolite.starter.util.XmlUtil;
import com.chaudhuri.plist.PlistParser;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.module.ModuleDescriptor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import oolite.starter.model.Command;
import oolite.starter.model.Expansion;
import oolite.starter.model.ExpansionReference;
import oolite.starter.model.Installation;
import oolite.starter.model.ProcessData;
import oolite.starter.model.SaveGame;
import oolite.starter.util.HttpUtil;
import org.antlr.v4.runtime.misc.ParseCancellationException;
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
public class Oolite implements PropertyChangeListener {
    private static final Logger log = LogManager.getLogger();
    private static final Logger logOolite = LogManager.getLogger("Oolite");
    
    private static final String OOLITE_CONFIGURATION_MUST_NOT_BE_NULL = "configuration must not be null";
    private static final String OOLITE_DOWNLOAD_URL = "downloadUrl";
    private static final String OOLITE_EXPANSION_FQN = "org.oolite.hiran.OoliteStarter.oxp";
    private static final String OOLITE_EXPANSIONS_MUST_NOT_BE_NULL = "expansions must not be null";
    private static final String OOLITE_EXPANSION_MUST_NOT_BE_NULL = "expansion must not be null";
    private static final String OOLITE_IDENTIFIER = "identifier";
    private static final String OOLITE_USER_HOME = "user.home";
    private static final String OOLITE_VERSION = "version";
    private static final String OOLITE_XML_HEADER = "<?xml";

    private boolean terminate = false;
    private int running = 0;
    
    /**
     * Compares the given expansion with the given oolite version.
     * The result is updated in the expansion's EMStatus.
     * 
     * @param oolite the oolite version to compare with
     * @param expansion the list of expansions to compare
     */
    protected void validateCompatibility(ModuleDescriptor.Version oolite, Expansion expansion) {
        expansion.getEMStatus().setIncompatible(false);

        if (expansion.getRequiredOoliteVersion() != null && !expansion.getRequiredOoliteVersion().isBlank()) {
            ModuleDescriptor.Version reqVersion = parseVersion(expansion.getRequiredOoliteVersion());
            if (oolite.compareTo(reqVersion) < 0) {
                log.trace("we have {} but need minimum {}", oolite, reqVersion);
                expansion.getEMStatus().setIncompatible(true);
            }
        }
        if (expansion.getMaximumOoliteVersion() != null && !expansion.getMaximumOoliteVersion().isBlank()) {
            ModuleDescriptor.Version maxVersion = parseVersion(expansion.getMaximumOoliteVersion());
            if (oolite.compareTo(maxVersion) > 0) {
                log.trace("we have {} but need maximum {}", oolite, maxVersion);
                expansion.getEMStatus().setIncompatible(true);
            }
        }
    }
    
    /**
     * Compares the given list of expansions with the currently activated
     * installation.
     * The result is updated in each expansion's EMStatus.
     * 
     * @param expansions the list of expansions to compare
     */
    protected void validateCompatibility(List<Expansion> expansions) {
        log.debug("validateCompatibility(...)");
        if (configuration == null) {
            throw new IllegalStateException(OOLITE_CONFIGURATION_MUST_NOT_BE_NULL);
        }
        if (expansions == null) {
            throw new IllegalArgumentException(OOLITE_EXPANSIONS_MUST_NOT_BE_NULL);
        }
        ModuleDescriptor.Version oolite =  parseVersion(configuration.getActiveInstallation().getVersion());
        
        for (Expansion expansion: expansions) {
            try {
                validateCompatibility(oolite, expansion);
            } catch (Exception ex) {
                log.warn("Could not verify compatibility for expansion {}", expansion.getIdentifier(), ex);
            }
        }
    }
    
    /**
     * Finds an expansion by given reference. Mainly used during requires and collision checks.
     * That's why the version and the maximum_version need to be taken into account.
     * 
     * @param reference the reference
     * @param expansions the list to find it in
     * @param checkEnabled set to true if only enabled expansions shall be considered
     * @return the Expansions found
     */
    List<Expansion> getExpansionByReference(Expansion.Dependency reference, List<Expansion> expansions, boolean checkEnabled) {
        log.debug("getExpansionByReference({}, {}, {})", reference, expansions, checkEnabled);
        
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }
        if (expansions == null) {
            throw new IllegalArgumentException(OOLITE_EXPANSIONS_MUST_NOT_BE_NULL);
        }
        if (reference.getIdentifier() == null) {
            throw new IllegalArgumentException("reference must have a non-null identifier");
        }
        
        List<Expansion> result = new ArrayList<>();
        
        ModuleDescriptor.Version minVersion = parseVersion(reference.getVersion());
        ModuleDescriptor.Version maxVersion = parseVersion(reference.getMaximumVersion());
        
        log.trace("minVersion {}", minVersion);
        log.trace("maxVersion {}", maxVersion);
        
        for (Expansion expansion: expansions) {
            log.trace("checking expansion {}", expansion);
            
            if (checkEnabled && !expansion.isEnabled()) {
                // we need to check that the expansion is enabled.
                // this one is not - so continue
                continue;
            }
            
            if (reference.getIdentifier().equals(expansion.getIdentifier())) {
                log.trace("identifier matched");
                
                ModuleDescriptor.Version expVersion = parseVersion(expansion.getVersion());
                
                log.info("expVersion {}", expVersion);
                if (minVersion == null) {
                    // we have not even a minimum version? Then all versions match
                    result.add(expansion);
                } else if (minVersion.compareTo(expVersion) <= 0 || "0".equals(reference.getVersion())) {
                    log.trace("minVersion matched");
                    // we have a minVersion that matches. What about the maxversion?
                    if (maxVersion == null) {
                        result.add(expansion);
                    } else if (expVersion.compareTo(maxVersion) <= 0) {
                        log.trace("maxVersion matched");
                        result.add(expansion);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Finds an expansion by given reference. Mainly used during update checks.
     * 
     * @param reference the reference
     * @param expansions the list to find it in
     * @param checkEnabled set to true if only enabled expansions shall be considered
     * @return the Expansions found
     * @deprecated use the Dependency type for reference instead
     */
    @Deprecated(since = "21FEB24", forRemoval = true)
    List<Expansion> getExpansionByReference(String reference, List<Expansion> expansions, boolean checkEnabled) {
        log.debug("getExpansionByReference({}, {})", reference, expansions);
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null");
        }
        if (expansions == null) {
            throw new IllegalArgumentException(OOLITE_EXPANSIONS_MUST_NOT_BE_NULL);
        }

        List<Expansion> result = new ArrayList<>();
       
        // first find the full reference
        for (Expansion e: expansions) {
            if (reference.equals(e.getIdentifier())
                    && (!checkEnabled || e.isEnabled())) {
                result.add(e);
            }
        }
        
        if (!result.isEmpty()) {
            // we found something? Let's return it.
            return result;
        }
        
        // then strip off version and try again

        int pos = reference.lastIndexOf(":");
        if (pos >= 0) {
            reference = reference.substring(0, pos);

            for (Expansion e: expansions) {
                if (reference.equals(e.getIdentifier())
                        && (!checkEnabled || e.isEnabled())) {
                    result.add(e);
                }
            }
        }
        
        return result;
    }

    /**
     * Checks for conflicts between OXPs.
     * 
     * @param expansions list of OXPs that need to be checked with each other.
     *      Pass in all expansions (enabled and not enabled).
     */
    void validateConflicts(List<Expansion> expansions) {
        log.debug("validateConflicts({})", expansions);
        if (expansions == null) {
            throw new IllegalArgumentException(OOLITE_EXPANSIONS_MUST_NOT_BE_NULL);
        }

        // reset conflict flag
        log.trace("Reset conflict flags for {} expansions", expansions.size());
        expansions.stream()
                .forEach(t -> t.getEMStatus().getConflicting().removeAll(expansions) );

        log.trace("Fetching conflicts...");
        expansions.stream()
            .filter(t -> t.isEnabled())
            .forEach(expansion -> {
                log.info("Fetching conflicts for {}:{}...", expansion.getIdentifier(), expansion.getVersion());
                try {
                    List<Expansion.Dependency> conflictDeps = expansion.getConflictOxps();
                    if (conflictDeps != null) {
                        log.trace("potential conflicts {}", conflictDeps);
                        for (Expansion.Dependency dep: conflictDeps) {
                            log.trace("processing potential conflicts on {}", dep);
                            List<Expansion> conflicts = getExpansionByReference(dep, expansions, true);
                            log.trace("found conflicts {}", conflicts);
                            if (!conflicts.isEmpty()) {
                                log.warn("Expansion {} conflicts with {}", expansion.getIdentifier(), conflicts);
                                expansion.getEMStatus().getConflicting().addAll(conflicts);
                            }
                            conflicts.stream()
                              .forEach(t -> t.getEMStatus().getConflicting().add(expansion) );
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Could not assess conflicts for {}", expansion.getIdentifier(), ex);
                }
            });
    }
    
    /**
     * Parses a version string. The first 'v' character is omitted.
     * 
     * @param version the version string to parse
     * @return the version number, or null if the input string was null
     */
    protected ModuleDescriptor.Version parseVersion(String version) {
        if (version == null) {
            return null;
        }
        
        if (version.startsWith("v")) {
            version = version.substring(1);
        }
        return ModuleDescriptor.Version.parse(version);
    }

    /**
     * Checks for updates and returns a list of commands.
     * These commands will uninstall the old expansions and install the new ones instead.
     * 
     * @param expansions 
     * @return the list of commands to be up to date, or null if there is nothing to do
     */
    public List<Command> checkForUpdates(List<Expansion> expansions) {
        log.debug("checkForUpdates(...)");
        if (expansions == null) {
            throw new IllegalArgumentException(OOLITE_EXPANSIONS_MUST_NOT_BE_NULL);
        }
        
        List<Command> result = new ArrayList<>();
        
        expansions.stream()
            .filter(t -> t.isEnabled())
            .forEach(t -> expansions.stream()
                    .filter(t2 -> t.getIdentifier().equals(t2.getIdentifier()))
                    .filter(t2 -> {
                        ModuleDescriptor.Version v1 = null;
                        ModuleDescriptor.Version v2 = null;
                        try {
                            v1 = parseVersion(t.getVersion());
                        } catch (IllegalArgumentException iae) {
                            log.warn("Problem on {} {}", t.getIdentifier(), t.getVersion(), iae);
                        }

                        try {
                            v2 = parseVersion(t2.getVersion());
                        } catch (IllegalArgumentException iae) {
                            log.warn("Problem on {} {}", t2.getIdentifier(), t2.getVersion(), iae);
                        }

                        if (v1 == null) {
                            return false;
                        } else {
                            return v1.compareTo(v2) < 0;
                        }
                    })
                    .forEach(t2 -> {
                        log.debug("{} -- {}", t.getIdentifier(), t.getVersion());
                        log.debug("    {} -- {}", t2.getIdentifier(), t2.getVersion());

                        result.add(new Command(Command.Action.DELETE, t));
                        result.add(new Command(Command.Action.INSTALL, t2));
                    })
            );
        
        return result;
    }

    public interface OoliteListener {
        
        /**
         * Will be called whenever Oolite is started.
         */
        public void launched(ProcessData pd);
        
        /**
         * Will be called whenever Oolite has terminated.
         */
        public void terminated();

        /**
         * Will be called whenever a new configuration has been activated.
         * 
         * @param installation the installation that was activated
         */
        public void activatedInstallation(Installation installation);
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
        if (this.configuration != null) {
            this.configuration.removePropertyChangeListener(this);
        }
        this.configuration = configuration;
        if (configuration != null) {
            this.configuration.addPropertyChangeListener(this);
        }
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
                        log.info("Skipping savegame {}", f.getAbsolutePath(), e);
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
                    // get the last path component
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

    /**
     * Returns true if the expansion is contained in the list of ExpansionReferences.
     * 
     * @param list the list of ExpansionReferences
     * @param expansion the expansion to search for
     * @return true if found, false otherwise
     */
    protected boolean contains(List<ExpansionReference> list, Expansion expansion) {
        log.debug("contains({}, {})", list, expansion);
        if (list == null) {
            throw new IllegalArgumentException("list must not be null");
        }
        if (expansion == null) {
            throw new IllegalArgumentException(OOLITE_EXPANSION_MUST_NOT_BE_NULL);
        }
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
                ref.addReason("not required but installed");
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
    
    void fireLaunched(ProcessData pd) {
        for (OoliteListener l: listeners) {
            l.launched(pd);
        }
    }
    
    void fireTerminated() {
        for (OoliteListener l: listeners) {
            l.terminated();
        }
    }
    
    void fireActivatedInstallation(Installation installation) {
        for (OoliteListener l: listeners) {
            Instant start = Instant.now();
            l.activatedInstallation(installation);
            log.warn("Listener {} took {} to process activatedInstallation(...)", l, Duration.between(start, Instant.now()));
        }
    }
    
    private class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumeInputLine;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumeInputLine) {
            this.inputStream = inputStream;
            this.consumeInputLine = consumeInputLine;
        }

        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumeInputLine);
        }
    }
    
    /**
     * Terminates a running process.
     * Sets a flag to terminates whichever process is being executed by run(...).
     */
    public void terminate() {
        terminate = true;
    }
    
    /**
     * Checks whether Oolite is running some process.
     * 
     * @return true is some process is running, false otherwise
     */
    public boolean isRunning() {
        return running > 0;
    }
    
    void destroyProcessTree(ProcessHandle ph, boolean forcibly) {
        if (ph.isAlive()) {
            if (Util.getOperatingSystemType() == Util.OSType.LINUX) {
                try {
                    List<String> cmd = new ArrayList<>();
                    cmd.add("pkill");
                    cmd.add("-P");
                    cmd.add(String.valueOf(ph.pid()));
                    run(cmd, new File("."));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Could not run pkill");
                } catch (Exception e) {
                    log.info("Could not run pkill");
                }
            }
            
            ph.descendants().forEach(t -> destroyProcessTree(t, forcibly) );

            if (forcibly) {
                log.info("destroying forcibly pid {}", ph.pid());
                ph.destroyForcibly();
            } else {
                log.info("destroying pid {}", ph.pid());
                ph.destroy();
            }
            
        }
    }
    
    /**
     * Runs Oolite using the specified command in the specified directory.
     */
    public void run(List<String> command, File dir) throws IOException, InterruptedException, ProcessRunException {
        log.debug("run({}, {})", command, dir);

        if (configuration != null) {
            injectExpansion();
        }
        
        try {
            log.info("executing {} in {}", command, dir);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(dir);
            running++;
            Process p = pb.start();
            
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), logOolite::info);
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), logOolite::error);

            Thread t1 = new Thread(outputGobbler);
            Thread t2 = new Thread(errorGobbler);
            t1.start();
            t2.start();

            terminate = false;
            fireLaunched(new ProcessData(dir, command, p.pid()));
            while (p.isAlive()) {
                p.waitFor(1000, TimeUnit.MILLISECONDS);
                
                if (terminate) {
                    destroyProcessTree(p.toHandle(), false);
                    Thread.sleep(1000);
                    destroyProcessTree(p.toHandle(), true);
                }
            }
            
            t1.join(2000);
            t2.join(2000);
            
            fireTerminated();
            
            log.info("Process exited with code {}", p.exitValue());
            if (p.exitValue() != 0) {
                throw new ProcessRunException(String.format("Oolite terminated with code %d", p.exitValue()));
            }
            
        } finally {
            running--;
            if (configuration != null) {
                try {
                    removeExpansion();
                } catch (Exception e) {
                    log.info("Could not cleanup OoliteStarter.oxp after Oolite run.", e);
                }
            }
        }            
    }
    
    /**
     * Parses a dependency list.
     * See https://wiki.alioth.net/index.php/Manifest.plist#Dependency_management_keys
     * @param vc 
     */
    protected List<Expansion.Dependency> parseDependencyList(PlistParser.ValueContext vc) {
        log.debug("parseDependencyList({})", vc);
        if (vc == null) {
            throw new IllegalArgumentException("vc must not be null");
        }
        
        List<Expansion.Dependency> result = new ArrayList<>();
        
        if (vc.list() != null) {
            for (PlistParser.ValueContext vc2: vc.list().value()) {
                PlistParser.DictionaryContext dict = vc2.dictionary();

                Expansion.Dependency dependency = new Expansion.Dependency();
                    
                for (PlistParser.KeyvaluepairContext kvc: dict.keyvaluepair()) {
                    String key = kvc.STRING().getText();
                    String value = kvc.value().getText();
                    switch(key) {
                        case OOLITE_IDENTIFIER:
                            dependency.setIdentifier(value);
                            break;
                        case OOLITE_VERSION:
                            dependency.setVersion(value);
                            break;
                        case "description":
                            dependency.setDescription(value);
                            break;
                        case "maximum_version":
                            dependency.setMaximumVersion(value);
                            break;
                        default:
                            log.info("unknown dependency key {}", key);
                            break;
                    }
                }
                
                result.add(dependency);
            }
        }
        
//        TODO: Sort the list - somehow
//        Collections.sort(result, new Comparator<Expansion.Dependency>() {
//            @Override
//            public int compare(Expansion.Dependency t, Expansion.Dependency t1) {
//                if (t==null && t1== null) {
//                    return 0;
//                }
//            }
//        });
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
        return createExpansionFromManifest(vc.dictionary());
    }
    
    protected List<Expansion.Dependency> parseDependencies(Document doc, String depname) throws XPathExpressionException {
        List<Expansion.Dependency> result = new ArrayList<>();
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nl = (NodeList)xpath.evaluate("/plist/dict/key[.='" + depname + "']/following-sibling::array[1]/dict", doc, XPathConstants.NODESET);
        
        for (int i=0; i< nl.getLength(); i++) {
            Element dict = (Element)nl.item(i);
            
            Expansion.Dependency dep = new Expansion.Dependency();
            dep.setIdentifier(xpath.evaluate("key[.='identifier']/following-sibling::string", dict));
            dep.setVersion(xpath.evaluate("key[.='version']/following-sibling::string", dict));
            dep.setDescription(xpath.evaluate("key[.='description']/following-sibling::string", dict));
            dep.setMaximumVersion(xpath.evaluate("key[.='maximum_version']/following-sibling::string", dict));
            
            result.add(dep);
        }
        
        return result;
    }
    
    /**
     * Parses the tags and returns a comma separated list.
     * 
     * @param doc the XML manifest of the expansion
     * @return the string
     */
    private String parseTags(Document doc) throws XPathExpressionException {
        log.debug("parseTags({})", doc);
                
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nl = (NodeList)xpath.evaluate("/plist/dict/key[.='tags']/following-sibling::array[1]/string", doc, XPathConstants.NODESET);
        
        List<String> tags = new ArrayList<>();
        for (int i=0; i<nl.getLength(); i++) {
            Element s = (Element)nl.item(i);
            tags.add(s.getTextContent());
        }
        
        return String.join(", ", tags);
    }
            

    /**
     * Creates an Expansion from a manifest dictionary context.
     * 
     * @param doc the XML DOM to read from
     * @return the Expansion
     */    
    public Expansion createExpansionFromManifest(Document doc) throws XPathExpressionException {
        log.debug("createExpansion({})", doc);
        if (doc == null) {
            throw new IllegalArgumentException("doc must not be null");
        }
        
        Element root = doc.getDocumentElement();
        if (root == null) {
            throw new IllegalArgumentException("doc must have root element");
        }
        if (!"plist".equals(root.getNodeName())) {
            throw new IllegalArgumentException("Expected plist to parse");
        }
        if (!"1.0".equals(root.getAttribute(OOLITE_VERSION))) {
            throw new IllegalArgumentException("Expected plist version 1.0");
        }
        
        Expansion result = new Expansion();
        
        XPath xpath = XPathFactory.newInstance().newXPath();
        result.setIdentifier(xpath.evaluate("/plist/dict/key[.='identifier']/following-sibling::string", doc));
        result.setRequiredOoliteVersion(xpath.evaluate("/plist/dict/key[.='required_oolite_version']/following-sibling::string", doc));
        result.setTitle(xpath.evaluate("/plist/dict/key[.='title']/following-sibling::string", doc));
        result.setVersion(xpath.evaluate("/plist/dict/key[.='version']/following-sibling::string", doc));
        result.setCategory(xpath.evaluate("/plist/dict/key[.='category']/following-sibling::string", doc));
        result.setDescription(xpath.evaluate("/plist/dict/key[.='description']/following-sibling::string", doc));
        result.setDownloadUrl(xpath.evaluate("/plist/dict/key[.='download_url']/following-sibling::string", doc));
        result.setAuthor(xpath.evaluate("/plist/dict/key[.='author']/following-sibling::string", doc));
        result.setInformationUrl(xpath.evaluate("/plist/dict/key[.='information_url']/following-sibling::string", doc));
        result.setLicense(xpath.evaluate("/plist/dict/key[.='license']/following-sibling::string", doc));
        result.setMaximumOoliteVersion(xpath.evaluate("/plist/dict/key[.='maximum_oolite_version']/following-sibling::array", doc)); 
        result.setTags(parseTags(doc)); 
        result.setConflictOxps(parseDependencies(doc, "conflict_oxps"));
        result.setRequiresOxps(parseDependencies(doc, "requires_oxps"));
        result.setOptionalOxps(parseDependencies(doc, "optional_oxps"));
        
        return result;
    }
    
    /**
     * Creates an Expansion from a manifest dictionary context.
     * 
     * @param dc the dictionary context to read
     * @return the Expansion
     */    
    public Expansion createExpansionFromManifest(PlistParser.DictionaryContext dc) {
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
                        log.info("NumberFormatException on {} line {}:{}: {}", kvc.start.getTokenSource().getSourceName(), kvc.start.getLine(), kvc.start.getCharPositionInLine(), e.getMessage());
                    }
                    break;
                case OOLITE_VERSION:
                    result.setVersion(value);
                    break;
                default:
                    log.info("unknown {}->{}", key, value);
            }
        }
        return result;
    }
    
    /**
     * Creates an Expansion from a requires dictionary context.
     * 
     * @param dc the dictionary context to read
     * @return the Expansion
     */    
    public Expansion createExpansionFromRequiresPlist(PlistParser.DictionaryContext dc) {
        log.debug("createExpansion({})", dc);
        Expansion result = new Expansion();
        for (PlistParser.KeyvaluepairContext kvc: dc.keyvaluepair()) {
            String key = kvc.STRING().getText();
            String value = kvc.value().getText();
            
            switch (key) {
                case "max_version":
                    result.setMaximumOoliteVersion(value);
                    break;
                case OOLITE_VERSION:
                    result.setRequiredOoliteVersion(value);
                    break;
                default:
                    log.info("unknown {}->{}", key, value);
            }
        }
        return result;
    }
    
    /**
     * Creates an Expansion from a manifest.plist stream.
     * 
     * @param manifest the input stream to read (typically from a file or zipfile).
     * @param sourceName name of the source for the input stream
     * @return the Expansion
     */    
    public Expansion createExpansionFromManifest(InputStream manifest, String sourceName) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.debug("createExpansion({}, {})", manifest, sourceName);
        InputStream in = Util.getBufferedStream(manifest);
        
        // do we have an XML based manifests?
        in.mark(10);

        Scanner sc = new Scanner(in);
        if (OOLITE_XML_HEADER.equals(sc.next())) {
            log.info("XML content found in {}", sourceName);
            in.reset();
            
            Document doc = XmlUtil.parseXmlStream(in);
            return createExpansionFromManifest(doc);
        } else {
            in.reset();

            // parse plist, then create Expansion from that
            PlistParser.DictionaryContext dc = PlistUtil.parsePListDict(in, sourceName);
            return createExpansionFromManifest(dc);
        }
        
    }
    
    /**
     * Creates an Expansion from a requires.plist stream.
     * The stream may be in plist or xml format.
     * 
     * @param requires the input stream to read (typically from a file or zipfile).
     * @param sourceName name of the source for the input stream
     * @return the Expansion
     */    
    public Expansion createExpansionFromRequires(InputStream requires, String sourceName) throws IOException {
        log.debug("createExpansionFromRequiresPlist({}, {})", requires, sourceName);
        String oxp = sourceName;
        if (oxp.endsWith(File.separator + "requires.plist")) {
            oxp = oxp.substring(0, oxp.length()-15);
        }
        String oxpTitle = oxp;
        int idx = oxpTitle.lastIndexOf(File.separator);
        if (idx >= 0) {
            oxpTitle = oxpTitle.substring(idx+1);
        }
        if ( oxpTitle.endsWith(".oxp")) {
            oxpTitle = oxpTitle.substring(0, oxpTitle.length()-4);
        }
                
        if (!requires.markSupported()) {
            requires = new BufferedInputStream(requires);
        }
    
        requires.mark(10);
        Scanner sc = new Scanner(requires);
        if (OOLITE_XML_HEADER.equals(sc.next())) {
            requires.reset();
            try {
                Document doc = XmlUtil.parseXmlStream(requires);
                XPath xpath = XPathFactory.newDefaultInstance().newXPath();

                Expansion expansion = new Expansion();
                expansion.setIdentifier(oxp);
                expansion.setTitle(oxpTitle);
                expansion.setDescription("""
                    This OXP only contains a "requires.plist".
                    These contain not much useful information. Consider adding a "manifest.plist"!
                    More information: https://wiki.alioth.net/index.php/Manifest.plist"""
                );
                expansion.setVersion("0");
                expansion.setRequiredOoliteVersion(xpath.evaluate("/plist/dict/key[.='version']/following-sibling::string", doc));
                expansion.setMaximumOoliteVersion(xpath.evaluate("/plist/dict/key[.='max_version']/following-sibling::string", doc));
                return expansion;
            } catch (IOException | ParserConfigurationException | XPathExpressionException | SAXException e) {
                throw new IOException(String.format("Could not read as XML: %s, see log", sourceName), e);
            }
        } else {
            requires.reset();
            try {
                // parse plist, then create Expansion from that
                PlistParser.DictionaryContext dc = PlistUtil.parsePListDict(requires, sourceName);
                Expansion expansion = createExpansionFromRequiresPlist(dc);
                expansion.setIdentifier(oxp);
                expansion.setTitle(oxpTitle);
                expansion.setDescription(
                        "This is some OXP implementing requires.plist.\n" +
                        "From that file almost no metadata is available. Consider switching to manifest.plist."
                );
                expansion.setVersion("0");
                return expansion;
            } catch (ParseCancellationException e) {
                throw new IOException(String.format("Could not read as Plist: %s, see log", sourceName), e);
            }
        }
    }
    
    /**
     * Returns a list of online and local expansions.
     * 
     * @return the list
     */
    public List<Expansion> getAllExpansions() throws IOException {
        log.debug("getAllExpansions()");
        Instant start = Instant.now();
        
        List<Expansion> resultList = new ArrayList<>();
        List<Expansion> localList = getLocalExpansions();
        List<Expansion> remoteList = getOnlineExpansions();
        
        localList.addAll(remoteList);
        for (Expansion current: localList) {
            if (resultList.contains(current)) {
                int index = resultList.indexOf(current);
                Expansion previous = resultList.get(index);
                
                previous.setOnline( previous.isOnline() || current.isOnline());
                if (current.getLocalFile() != null) {
                    previous.setLocalFile(current.getLocalFile());
                }
                if (current.getDownloadUrl() != null) {
                    previous.setDownloadUrl(current.getDownloadUrl());
                }
            } else {
                resultList.add(current);
            }
        }
        
        validateCompatibility(resultList);
        validateConflicts(resultList);
        validateDependencies2(resultList);
        validateUpdates(resultList);
        
        Collections.sort(resultList);
        
        log.warn("Performed getAllExpansions() on {} expansions in {}", resultList.size(), Duration.between(start, Instant.now()));
        return resultList;
    }

    /**
     * Returns the list of expansion available for download.
     * 
     * @return the list
     */
    public List<Expansion> getOnlineExpansions() throws IOException {
        log.debug("getOnlineExpansion()");
        if (configuration == null) {
            throw new IllegalStateException(OOLITE_CONFIGURATION_MUST_NOT_BE_NULL);
        }

        List<Expansion> result = new ArrayList<>();
        
        for (URL url: configuration.getExpansionManagerURLs()) {
            log.debug("downloading {}", url);

            URLConnection urlconnection = url.openConnection();
            if (urlconnection instanceof HttpURLConnection conn) {
                conn.setReadTimeout(5000);
                int status = conn.getResponseCode();
                log.info("HTTP status for {}: {}", url, status);
                
                int redirectCount = 5;
                while ((status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_MOVED_TEMP) && redirectCount > 0) {
                    redirectCount--;
                    String newUrl = conn.getHeaderField("Location");
                    log.info("Follow redirect to '{}'", newUrl);
                    conn = (HttpURLConnection)new URL(newUrl).openConnection();
                    conn.setReadTimeout(5000);
                    status = conn.getResponseCode();
                    log.info("HTTP status for {}: {}", newUrl, status);
                }
                urlconnection = conn;
            }
            
            try (InputStream in = urlconnection.getInputStream()) {
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
    
    private List<Expansion> getLocalExpansionFromThisFile(File f) {
        List<Expansion> tempResult = new ArrayList<>();

        if (f.isDirectory() && f.getName().toLowerCase().endsWith(".oxp")) {
            tempResult = getLocalExpansions(f);
        }

        if (tempResult.isEmpty()) {
            // no OXPs in subdirectories - then check this one
            try {
                Expansion expansion = getExpansionFrom(f);

                if (expansion != null) {
                    expansion.setOolite(this);
                    expansion.setLocalFile(f);
                    expansion.setFileSize(f.length());
                    tempResult.add(expansion);
                }
            } catch (Exception e) {
                log.warn("Could not read expansion in {}", f, e);
            }
        }
        
        return tempResult;
    }

    /**
     * Scans a directory for expansions.
     * 
     * @param dir
     * @return 
     */
    List<Expansion> getLocalExpansions(File dir) {
        log.debug("scanning {}", dir);
        List<Expansion> result = new ArrayList<>();
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f: files) {
                result.addAll(getLocalExpansionFromThisFile(f));
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

    /**
     * Investigates whether a File holds an expansion and returns it.
     * 
     * @param f the file to investigate
     * @return the expansion found, or null
     */
    protected Expansion getExpansionFrom(File f) throws ParserConfigurationException, SAXException, XPathExpressionException {
        log.debug("getExpansionFrom({})", f);
        try {
            if (f.isDirectory()) {
                // if it is a directory, is it an OXP?
                if (f.getName().toLowerCase().endsWith(".oxp")) {
                    
                    // todo: Here we need to check for more subdirectories
                    
                    return getExpansionFromOxp(f);
                } else {
                    // not a subdirectory, but we do not scan subdirectories
                    
                    // todo: here deactivated addons might be identified
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
    
    /**
     * Checks the given OXP directory for manifest.plist or requires.plist
     * and returns the expansion found. Or null.
     * 
     * @param f the directory to check
     * @return the expansion found, or null
     * @throws IOException something went wrong
     * @throws ParserConfigurationException something went wrong
     * @throws SAXException something went wrong
     * @throws XPathExpressionException something went wrong
     */
    private Expansion getExpansionFromOxp(File f) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.debug("getExpansionsFromOxp({})", f);
        File manifestFile = new File(f, "manifest.plist");
        if (manifestFile.isFile()) {
            InputStream stream = new FileInputStream(manifestFile);
            return createExpansionFromManifest(stream, manifestFile.getAbsolutePath());
        }
        manifestFile = new File(f, "requires.plist");
        if (manifestFile.isFile()) {
            InputStream stream = new FileInputStream(manifestFile);
            return createExpansionFromRequires(stream, manifestFile.getAbsolutePath());
        }
        return null;
    }
    
    private Expansion getExpansionFromOxz(File f) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.debug("getExpansionsfromOxz({})", f);
        
        try (ZipFile zipFile = new ZipFile(f)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if ("manifest.plist".equals(entry.getName())) {
                    InputStream stream = zipFile.getInputStream(entry);
                    return createExpansionFromManifest(stream, f.getAbsolutePath() + "!" + entry.getName());
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
        
        File file = new File(configuration.getManagedAddonsDir(), expansion.getIdentifier() + "@" + expansion.getVersion() + ".oxz");
        HttpUtil.downloadUrl(url, file);
        
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
        if (expansion.getLocalFile().isDirectory()) {
            FileUtils.deleteDirectory(expansion.getLocalFile());
        } else {
            FileUtils.delete(expansion.getLocalFile());
        }
        expansion.setLocalFile(null);
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
        
        return (configuration.getManagedDeactivatedAddonsDir() != null
                && configuration.getManagedDeactivatedAddonsDir().isDirectory()
                && FileUtils.directoryContains(configuration.getManagedDeactivatedAddonsDir(), test))
                || 
                (configuration.getManagedAddonsDir() != null
                && configuration.getManagedAddonsDir().isDirectory()
                && FileUtils.directoryContains(configuration.getManagedAddonsDir(), test));
    }
    
    /**
     * Determines if an Expansion can be found by Oolite.
     * 
     * @param expansion the expansion to test
     * @return true if and only if it is activated
     */
    public boolean isEnabled(Expansion expansion) throws IOException {
        log.debug("isEnabled({})", expansion);
        
        if (expansion == null) {
            throw new IllegalArgumentException(OOLITE_EXPANSION_MUST_NOT_BE_NULL);
        }
        
        File test = expansion.getLocalFile();
        if (test == null)
            return false;
        
        return (configuration.getAddonsDir() != null 
                && configuration.getAddonsDir().isDirectory()
                && FileUtils.directoryContains(configuration.getAddonsDir(), test))
                || 
                (configuration.getManagedAddonsDir()!= null 
                && configuration.getManagedAddonsDir().isDirectory()
                && FileUtils.directoryContains(configuration.getManagedAddonsDir(), test));
    }
    
    /**
     * Determines if an Expansion is part of the deactivated expansions directory.
     * 
     * @param expansion the expansion to test
     * @return true if and only if it is deactivated
     */
    public boolean isDisabled(Expansion expansion) throws IOException {
        log.debug("isDisabled({})", expansion);
        
        if (expansion == null) {
            throw new IllegalArgumentException(OOLITE_EXPANSION_MUST_NOT_BE_NULL);
        }
        return !isEnabled(expansion);
    }
    
    /**
     * Parses the list of expansions from a expansion set xml file
     * and returns a list of expansion references.
     * 
     * @param source the file to read from
     */
    public NodeList parseExpansionSet(File source) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        log.debug("parseExpansionSet({})", source);
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        
        Document doc = XmlUtil.parseXmlFile(source);
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (NodeList)xpath.evaluate("/ExpansionList/Expansion", doc, XPathConstants.NODESET);
    }
    
    /**
     * Prepare list of enabled addons.
     * 
     * @return a map with identifier:version -> downloadurl
     */
    private TreeMap<String, String> prepareEnabledAddonsList(NodeList target) {
        TreeMap<String, String> result = new TreeMap<>();
        for (int i = 0; i < target.getLength(); i++) {
            Element e = (Element)target.item(i);
            result.put(e.getAttribute(OOLITE_IDENTIFIER) + ":" + e.getAttribute(OOLITE_VERSION), e.getAttribute(OOLITE_DOWNLOAD_URL));
        }
        return result;
    }
    
    /**
     * build commands to remove unwanted expansions.
     * 
     * @param enabledAddons
     * @param expansions
     * @return 
     */
    private List<Command> prepareDisableCommands(TreeMap<String, String> enabledAddons, List<Expansion> expansions) {
        List<Command> result = new ArrayList<>();

        for (Expansion expansion: expansions) {
            String i = expansion.getIdentifier() + ":" + expansion.getVersion();
            if (expansion.isLocal() && expansion.isEnabled() && !enabledAddons.containsKey(i)) {
                result.add(new Command(Command.Action.DISABLE, expansion));
            }
        }
        
        return result;
    }
    
    /**
     * Builds a list of commands that resembles the difference between the
     * current status of expansions and the target.
     * 
     * @param expansions where we are
     * @param target where we want to be
     * @return the list of commands to get there
     */
    public List<Command> buildCommandList(List<Expansion> expansions, NodeList target) {
        List<Command> result = new ArrayList<>();

        TreeMap<String, String> enabledAddons = prepareEnabledAddonsList(target);

        result.addAll(prepareDisableCommands(enabledAddons, expansions));

        // now INSTALL what may be MISSING
        // for that we prepare a list from which to pull Expansions
        TreeMap<String, Expansion> expansionMap = new TreeMap<>();
        for (Expansion expansion: expansions) { 
            expansionMap.put(expansion.getIdentifier() + ":" + expansion.getVersion(), expansion);
            expansionMap.put(expansion.getIdentifier(), expansion);
        }
        for (Map.Entry<String, String> entry: enabledAddons.entrySet()) {
            String i = entry.getKey();
            log.debug("checking {}", i);
            Expansion expansion = expansionMap.get(i);
            if (expansion == null) {
                String i2 = i.substring(0, i.indexOf(':'));
                expansion = expansionMap.get(i2);
            }
            if (expansion == null) {
                Expansion e = new Expansion();
                e.setOolite(this);
                e.setTitle(i);
                e.setDownloadUrl(enabledAddons.get(i));
                result.add(new Command(Command.Action.UNKNOWN, e));
                log.info("Trying expansionset download {}", e);
            } else if (expansion.isLocal() && expansion.isEnabled()) {
                // already here - do nothing
                log.info("{} is already installed & enabled - doing nothing", i);
                result.add(new Command(Command.Action.KEEP, expansion));
            } else if (expansion.isLocal() && !expansion.isEnabled()) {
                log.info("{} is already installed but disabled - enabling", i);
                result.add(new Command(Command.Action.ENABLE, expansion));
            } else {
                String i2 = i.substring(i.indexOf(':'));
                if (i2.equals(expansion.getVersion())) {
                    result.add(new Command(Command.Action.INSTALL, expansion));
                } else {
                    result.add(new Command(Command.Action.INSTALL_ALTERNATIVE, expansion));
                }
            }
        }

        return result;
    }
    
    /**
     * Writes the list of currently enabled expansions as xml file.
     * 
     * @param destination the file to write to
     * 
     * @see setEnabledExpansions
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
                element.setAttribute(OOLITE_DOWNLOAD_URL, expansion.getDownloadUrl());
                root.appendChild(element);
            }
        }
        
        TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();
        // to be compliant, prohibit the use of all protocols by external entities:
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer t = factory.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes"); // <xsl:output method="xml" indent="yes"/>
        t.transform(new DOMSource(doc), new StreamResult(destination));
    }
    
    /**
     * Tries to resolve dependencies via the filesystem.
     * @param expansion the expansion whose requirements shall be checked
     * @param result the result list to append our result
     */
    void validateRequirements(Expansion expansion, List<ExpansionReference> result) {
        log.debug("validateRequirements(...)");
        
        if (expansion.getRequiresOxps() != null) {
            for (Expansion.Dependency dependency: expansion.getRequiresOxps()) {
                // for each required expansion...
                ExpansionReference er = getExpansionReference(dependency);
                if (er.getStatus() == ExpansionReference.Status.MISSING) {
                    er.addReason("missed by " + expansion.getIdentifier());
                    result.add(er);
                }
            }
        }
    }
    
    /**
     * 
     * @param expansion
     * @param result
     * @deprecated we will validate on Expansions directly and store that
     *   information in EMStatus
     */
    @Deprecated(since = "21FEB24", forRemoval = true)
    void validateConflicts(Expansion expansion, List<ExpansionReference> result) {
        if (expansion.getConflictOxps() != null) {
            for (Expansion.Dependency dependency: expansion.getConflictOxps()) {
                ExpansionReference er = getExpansionReference(dependency);
                if (er.getStatus() == ExpansionReference.Status.OK) {
                    er.setStatus(ExpansionReference.Status.SURPLUS);
                    er.addReason("conflicting with " + expansion.getIdentifier());
                    result.add(er);
                }
            }
        }
    }
    
    /**
     * Validates whether the list of expansions is satisfied.
     * It checks for unfulfilled requirements and conflicts, but only on the
     * enabled ones.
     * 
     * @param expansions the expansions to check
     * @return the list of discrepancies found
     * 
     * @deprecated use validateDependencies2 instead, which is richer in information
     */
    @Deprecated(since = "21FEB24", forRemoval = true)
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
     * Validates whether the list of expansions is satisfied.
     * It checks for unfulfilled requirements and conflicts, but only on the
     * enabled ones. The result will be stored in the Expansion's EMStatus.
     */
    public void validateDependencies2(List<Expansion> expansions) {
        log.debug("validateDependencies2({})", expansions);
        
        expansions.stream().forEach(expansion -> {
            expansion.getEMStatus().getMissing().removeAll(expansions);
            expansion.getEMStatus().getRequiredBy().removeAll(expansions);
        });

        expansions.stream().forEach(expansion -> {
            List<Expansion.Dependency> deps = expansion.getRequiresOxps();
            if (deps != null) {
                deps.stream().forEach(dependency -> {
                    List<Expansion> ds = getExpansionByReference(dependency, expansions, false);
                    ds.stream().forEach(d -> {
                        if (!d.isEnabled()) {
                            expansion.getEMStatus().getMissing().add(d);
                        }
                        d.getEMStatus().getRequiredBy().add(expansion);
                    });
                });
            }
        });
    }
    
    /**
     * Find updates that are updatable from non-active ones in the list.
     * 
     * @param expansions the list of expansions
     */
    void validateUpdates(List<Expansion> expansions) {
        log.debug("validateUpdates({})", expansions);
        
        expansions.stream()
                .filter(e -> e.getIdentifier() != null)
                .forEach(e -> {

            List<Expansion> ds = getExpansionByReference(new Expansion.Dependency(e.getIdentifier()), expansions, false);
            try {
                if (ds.size() > 1) {
                    // sort backwards (latest is first)
                    Collections.sort(ds, (t, t1) -> {
                        ModuleDescriptor.Version v = parseVersion(t.getVersion());
                        ModuleDescriptor.Version v1 = parseVersion(t1.getVersion());

                        return v1.compareTo(v); 
                    });
                }

                if (e.getVersion().equals(ds.get(0).getVersion())) {
                    log.trace("latest = true on {}", e.getIdentifier());
                    e.getEMStatus().setLatest( true );
                } else {
                    log.trace("latest = false on {}", e.getIdentifier());
                    e.getEMStatus().setLatest( false );
                }
            } catch (Exception ex) {
                log.warn("Could not check updates for {}", e.getIdentifier(), ex);
            }

        });
        
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
    
    /**
     * Returns an ExpansionReference for an enabled expansion.
     * The expansion is searched in the AddonsDir and the ManagedAddonsDir.
     * If a direct match is not found, the version number is stripped off
     * and the search is repeated.
     * 
     * @param dep the dependency
     * @return the reference
     */
    public ExpansionReference getExpansionReference(Expansion.Dependency dep) {
        log.debug("getExpansionReference({})", dep);
        if (configuration == null) {
            throw new IllegalStateException(OOLITE_CONFIGURATION_MUST_NOT_BE_NULL);
        }
        
        return getExpansionReference("" + dep.getIdentifier() + "@" + dep.getVersion());
    }

    /**
     * Returns an ExpansionReference for an enabled expansion.
     * The expansion is searched in the AddonsDir and the ManagedAddonsDir.
     * If a direct match is not found, the version number is stripped off
     * and the search is repeated.
     * 
     * @param dep the name, optionally with '@' &lt;versionnumber&gt;
     * @return the reference
     */
    public ExpansionReference getExpansionReference(String dep) {
        log.debug("getExpansionReference({})", dep);
        if (configuration == null) {
            throw new IllegalStateException(OOLITE_CONFIGURATION_MUST_NOT_BE_NULL);
        }
        
        ExpansionReference result = new ExpansionReference();
        String[] r = dep.split("@|\\.oxz");
        result.setName(String.join(":", r));
        result.setStatus(ExpansionReference.Status.MISSING);
        result.addReason("required but not enabled");
        
        // find a direct match
        if (
                new File(configuration.getAddonsDir(), dep).exists()
            ||
                new File(configuration.getManagedAddonsDir(), dep).exists()
            ) {
            result.setStatus(ExpansionReference.Status.OK);
            result.getReasons().clear();
            return result;
        }
        
        // find some indirect match. First strip off version number, then find substring
        String id = dep;
        int idx = id.lastIndexOf("@");
        if (idx >= 0) {
            id = id.substring(0, idx);
        }
        idx = id.lastIndexOf(":");
        if (idx >= 0) {
            id = id.substring(0, idx);
        }

        File[] files = configuration.getAddonsDir().listFiles();
        for (File f: files) {
            if (f.getName().startsWith(id)) {
                result.setStatus(ExpansionReference.Status.OK);
                return result;
            }
        }
        files = configuration.getManagedAddonsDir().listFiles();
        if (files != null) {
            for (File f: files) {
                if (f.getName().startsWith(id)) {
                    result.setStatus(ExpansionReference.Status.OK);
                    return result;
                }
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
                // we found oolite.exe on WINDOWS!
                return true;
            }
            app = new File(f, "oolite");
            if (app.isFile()) {
                // we found oolite on LINUX!
                return true;
            }
        }
        
        if (Util.isMac() && fname.endsWith(".app")) {
            // check MACOS directory
            File app = new File(f, "Contents/MacOS/Oolite");
            if (app.isFile()) {
                // we found oolite on MACOS
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
            // this works for WINDOWS and LINUX
            return OoliteDirectoryType.HOME_DIR;
        }
        
        if (isOoliteExpansionDirectory(f)) {
            // this works for WINDOWS and LINUX
            return OoliteDirectoryType.EXPANSION_DIR;
        }
        
        if (isOoliteSaveGameDirectory(f)) {
            // this works for WINDOWS and LINUX
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
                return "This is an Oolite home directory.";
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
                if (OOLITE_VERSION.equals(key)) {
                    return value;
                }
            }

            return null;
        }
    }
    
    /**
     * Extracts the Oolite version for this installation.
     * 
     * @param homeDir the installation's home directory
     * @return the version number found
     * @throws ParserConfigurationException something went wrong
     * @throws SAXException something went wrong
     * @throws IOException something went wrong
     * @throws XPathExpressionException something went wrong
     */
    public static String getVersionFromHomeDir(File homeDir) throws IOException {
        switch (oolite.starter.util.Util.getOperatingSystemType()) {
            case LINUX:
                // check Linux
                File releaseTxt = new File(homeDir, "../release.txt");
                return IOUtils.toString(new FileReader(releaseTxt)).trim();

            case MACOS:
                // check MacOS
                releaseTxt = new File(homeDir, "Contents/Info.plist");
                try {
                    return Oolite.getVersionFromInfoPlist(releaseTxt);
                } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
                    throw new IOException("Could not get version from " + releaseTxt.getAbsolutePath());
                }
                
            case WINDOWS, OTHER:
                // check Windows and generic
                releaseTxt = new File(homeDir, "Resources/manifest.plist");
                return Oolite.getVersionFromManifest(releaseTxt);
        }


        File manifest = new File(homeDir, "Resources/manifest.plist");
        if (!manifest.exists()) {
            manifest = new File(homeDir, "Contents/Resources/manifest.plist");
        }
        return getVersionFromManifest(manifest);
    }
    
    /**
     * Returns the reasonable addons directory for a given home directory.
     * 
     * @param homeDir the home directory
     * @return the addons directory, or null if not found
     */
    public static File getAddOnDir(File homeDir) {
        log.debug("getAddOnDir({})", homeDir);

        // check LINUX, WINDOWS
        File d = new File(homeDir, "../AddOns");
        if (d.isDirectory()) {
            return d;
        }

        // check MACOS
        d = new File(new File(System.getProperty(OOLITE_USER_HOME)), "Library/Application Support/Oolite/Addons");
        if (d.isDirectory()) {
            return d;
        }
        
        return null;
    }

    /**
     * Returns the reasonable deactivated addons directory for a given home directory.
     * 
     * @param homeDir the home directory
     * @return the deactivated addons directory
     */
    public static File getDeactivatedAddOnDir(File homeDir) {
        log.debug("getDeactivatedAddOnDir({})", homeDir);

        if (Util.isMac()) {
            return new File(new File(System.getProperty(OOLITE_USER_HOME)), "Library/Application Support/Oolite/DeactivatedAddOns");
        } else {
            return new File(homeDir, "../DeactivatedAddOns");
        }
    }

    /**
     * Returns the reasonable managed addons directory for a given home directory.
     * 
     * @param homeDir the home directory
     * @return the managed addons directory, or null if not found
     */
    public static File getManagedAddOnDir(File homeDir) {
        log.debug("getManagedAddOnDir({})", homeDir);

        switch (Util.getOperatingSystemType()) {
            case MACOS:
                return new File(new File(System.getProperty(OOLITE_USER_HOME)), "Library/Application Support/Oolite/Managed Addons");
            case LINUX:
                return new File(new File(System.getProperty(OOLITE_USER_HOME)), "GNUstep/Library/ApplicationSupport/Oolite/ManagedAddOns");
            case WINDOWS:
                return new File(homeDir, "GNUstep/Library/ApplicationSupport/Oolite/ManagedAddOns");
            default:
                log.warn("Could not find managed addon dir");
                return null;
        }

    }
    
    /**
     * Returns the reasonable managed deactivated addons directory for a given home directory.
     * 
     * @param homeDir the home directory
     * @return the managed deactivated addons directory
     */
    public static File getManagedDeactivatedAddOnDir(File homeDir) {
        return new File(getManagedAddOnDir(homeDir), "../ManagedDeactivatedAddOns");
    }
    
    /**
     * Returns the reasonable executable for a given home directory.
     * 
     * @param homeDir the home directory
     * @return the executable
     */
    public static File getExecutable(File homeDir) {
            File executable = new File(homeDir, "oolite-wrapper");
            if (!executable.exists()) {
                executable = new File(homeDir, "oolite.exe");
            }
            if (!executable.exists()) {
                executable = new File(homeDir, "oolite");
            }
            if (!executable.exists()) {
                executable = new File(homeDir, "Contents/MacOS/Oolite");
            }

            return executable;
    }
    
    /**
     * Returns the reasonable savegame directory for a given home directory.
     * 
     * @param homeDir the home directory
     * @return the savegame directory
     */
    public static File getSavegameDir(File homeDir) {
        File d = null;
        switch (Util.getOperatingSystemType()) {
            case MACOS:
                d = new File(new File(System.getProperty(OOLITE_USER_HOME)), "Documents");
                if (d.isDirectory()) {
                    return d;
                }
                break;
            case LINUX:
                return new File(new File(System.getProperty(OOLITE_USER_HOME)), "oolite-saves");
            case WINDOWS:
                d = new File(homeDir, "oolite-saves");
                if (d.isDirectory()) {
                    return d;
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        log.debug("propertyChange({})", pce);
        if ("activeInstallation".equals(pce.getPropertyName())) {
            Installation i = (Installation)pce.getNewValue();
            fireActivatedInstallation(i);
        }
    }

    /**
     * Populates an Installation with good guesses based on the home directory.
     * 
     * @param homeDir the home directory
     * @return the installation
     */
    public static Installation populateFromHomeDir(File homeDir) {
        log.debug("populateFromHomeDir({})", homeDir);
        
        Installation i = new Installation();
        i.setHomeDir(homeDir.getAbsolutePath());
        log.info(OOLITE_VERSION);
        try {
            i.setVersion(Oolite.getVersionFromHomeDir(homeDir));
        } catch (IOException e) {
            log.warn("Cannot read version for {}", homeDir, e);
        }
        log.info("executable");
        try {
            i.setExcecutable(Oolite.getExecutable(homeDir).getCanonicalPath());
        } catch (IOException e) {
            log.warn("Cannot get executable for {}", homeDir, e);
        }
        log.info("savegamedir");
        try {
            File h = Oolite.getSavegameDir(homeDir);
            if (h != null) {
                i.setSavegameDir(h.getCanonicalPath());
            }
        } catch (IOException e) {
            log.warn("Cannot get savegame dir for {}", homeDir, e);
        }
        log.info("addondir");
        try {
            File a = Oolite.getAddOnDir(homeDir);
            if (a != null) {
                i.setAddonDir(a.getCanonicalPath());
            }
        } catch (IOException e) {
            log.warn("Cannot get AddOns dir for {}", homeDir, e);
        }
        log.info("deactivatedaddondir");
        try {
            i.setDeactivatedAddonDir(Oolite.getDeactivatedAddOnDir(homeDir).getCanonicalPath());
        } catch (IOException e) {
            log.warn("Cannot get Deactivated AddOns dir for {}", homeDir, e);
        }
        log.info("managedaddondir");
        try {
            File m = Oolite.getManagedAddOnDir(homeDir);
            if (m != null) {
                i.setManagedAddonDir(m.getCanonicalPath());
            }
        } catch (IOException e) {
            log.warn("Cannot get Managed AddOns dir for {}", homeDir, e);
        }
        log.info("manageddeactivatedaddondir");
        try {
            i.setManagedDeactivatedAddonDir(Oolite.getManagedDeactivatedAddOnDir(homeDir).getCanonicalPath());
        } catch (IOException e) {
            log.warn("Cannot get Managed Deactivated AddOns dir for {}", homeDir, e);
        }

        log.info("population done");
        return i;
    }
    
    /**
     * Calculate deviations.
     * The result list will contain differences between want and have, including
     * transitive dependencies and conlicts.
     * 
     * Each of the elements in the diff list will have reasons attached.
     * 
     * @param want
     * @param have
     * @return 
     */
    public List<ExpansionReference> diff(List<ExpansionReference> want, List<ExpansionReference> have) {
        log.info("diff({}, {})", want, have);
        if (want == null) {
            throw new IllegalArgumentException("want must not be null");
        }
        if (have == null) {
            throw new IllegalArgumentException("have must not be null");
        }
        
        List<ExpansionReference> result = new ArrayList<>();
        
        List<ExpansionReference> inter = new ArrayList<>(have);
        for (ExpansionReference er: want) {
            inter.remove(er);
        }
        for (ExpansionReference er: inter) {
            er.setStatus(ExpansionReference.Status.SURPLUS);
            result.add(er);
        }
        
        inter = new ArrayList<>(want);
        for (ExpansionReference er: have) {
            inter.remove(er);
        }
        for (ExpansionReference er: inter) {
            er.setStatus(ExpansionReference.Status.MISSING);
            result.add(er);
        }

        // todo: work out transitive requirements (an expansion requires something that requires...)
        // todo: work out conflicts
        
        return result;
    }

    /**
     * Returns the URL to a Wiki page with the given name.
     * Feed it with expansion names (titles, not identifiers) to get
     * the relevant wiki page.
     * 
     * @param name the name of the page
     * @return the url to the page
     */
    public static String getOoliteWikiPageUrl(String name) {
        if (name == null) {
            throw new IllegalArgumentException("parameter must not be null");
        }
        
        final String base = "http://wiki.alioth.net/index.php/";
        /*
         * The URLEncoder uses too much of + escaping
         * so do not use 'base + URLEncoder.encode(name, Charset.forName("utf-8"));'
         */
        
        return base
                + name.replace(" ", "%20")
                        .replace("\"", "%22")
                        .replace("[", "%5B")
                        .replace("]", "%5D");
    }
    
    /**
     * Returns the currently active Installation.
     * 
     * @return the installation
     */
    public Installation getActiveInstallation() {
        return configuration.getActiveInstallation();
    }
}
