/*
 */
package oolite.starter.dcp;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author hiran
 */
public class Util {
    private static final Logger log = LogManager.getLogger();
    
    /** Parses an XML document.
     * 
     * @param in the InputStream to read from
     * @return the parsed DOM node
     * @throws ParserConfigurationException something went wrong
     * @throws IOException something went wrong
     * @throws SAXException something went wrong
     */
    public static Document parse(InputStream in) throws ParserConfigurationException, IOException, SAXException {
//        XMLReader reader = XMLReaderFactory.createXMLReader();
//        reader.setEntityResolver(new EntityResolver() {
//            @Override
//            public InputSource resolveEntity(String string, String string1) throws SAXException, IOException {
//                return new InputSource(new StringReader(""));
//            }
//        });
//        DomContentHandler dch = new DomContentHandler();
//        reader.setContentHandler(dch);
//        reader.parse(new InputSource(in));
//        return dch.getDocument();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        return doc;
    }

    /** Serializes a DOM node to XML.
     * 
     * @param n the node to serialize
     * @return the XML string
     * @throws TransformerConfigurationException something went wrong
     * @throws TransformerException something went wrong
     */
    public static String serialize(Node n) throws TransformerConfigurationException, TransformerException {
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.transform(new DOMSource(n), new StreamResult(sw));
        return sw.toString();
    }
    
    /** Parses the JSON describing the ship manifest.
     * It can look like this:
     * <pre>
     * {
     *     list: [{
     *         unit: "t",
     *         displayName: "Narcotics",
     *         commodity: "narcotics",
     *         containers: 0,
     *         quantity: 10
     *     }, {
     *         unit: "t",
     *         displayName: "Firearms",
     *         commodity: "firearms",
     *         containers: 0,
     *         quantity: 5
     *     }]
     * }
     * </pre>
     * 
     * @param json
     * @return 
     */
    public static Object parseManifest(String json) {
        log.info("parseManifest({})", json);
        
        JSONObject manifest = new JSONObject(json);
        JSONArray list = manifest.getJSONArray("list");
        log.debug("list={}", list);
        
        return null;
    }
    
    /**
     * Intends to detect if at some binary path the debug version is installed
     * This assumes wrongly the AddOns folder is always close to the binary.
     * This also assumes wrongly the debug OXP is always named 'Basic-debug.oxp'.
     * 
     * @param ooliteBinaryPath
     * @return 
     * @deprecated
     */
    @Deprecated
    public static boolean isOoliteDebugVersion(String ooliteBinaryPath) {
        File ooliteBinary = new File(ooliteBinaryPath);
        if (!ooliteBinary.canExecute()) {
            log.warn("Oolite binary not found. The path is probably not configured. Skipping further tests.");
            
            throw new IllegalArgumentException(String.format("Not existing path %s", ooliteBinaryPath));
        }
        
        if (ooliteBinaryPath.endsWith("Oolite/oolite")) { // Linux or version
            File appDir = new File(ooliteBinaryPath).getParentFile();
            log.info("appDir={}", appDir.getAbsolutePath());
            
            File debugConsole = new File(appDir, "AddOns/Basic-debug.oxp");
            if (debugConsole.isDirectory()) {
                log.debug("Found DebugConsole in folder {}", debugConsole.getAbsolutePath());
                return true;
            } else {
                log.debug("Expected DebugConsole in folder {} but nothing there?", debugConsole.getAbsolutePath());
                return false;
            }
        } else {
            throw new IllegalArgumentException(String.format("Unknown path %s", ooliteBinaryPath));
        }
    }
    
    /**
     * From a list of AddOns folders (Oolite can use multiple paths) find the
     * OXP containing the marker file.
     * 
     * @param addonsFolders
     * @return The folder of the debug OXP
     * @throws IOException something went wrong
     */
    private static File findDebugOxp(List<File> addonsFolders) throws IOException {
        for (File folder: addonsFolders) {
            if (folder.exists()) {
                for (File oxp: folder.listFiles()) {
                    File debugMarkerFile = new File(oxp, "DebugOXPLocatorBeacon.magic");
                    log.debug("checking {}", debugMarkerFile.getCanonicalPath());
                    if (debugMarkerFile.exists()) {
                        log.info("found Debug OXP at {}", oxp.getCanonicalPath());
                        return oxp;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Checks a given Oolite installation path whether it is executable,
     * whether the debug OXP exists and is configured correctly.
     * 
     * @param oolitePath the path to the installation
     * @throws Exception In case an issue is found
     */
    public static void checkOolitePath(String oolitePath) throws Exception {
        File ooliteExecutable = new File(oolitePath);
        if (!ooliteExecutable.exists()) {
            log.debug("ooliteExecutable expected at {}", ooliteExecutable.getCanonicalPath());
            throw new Exception("Executable not found.");
        }

        // http://wiki.alioth.net/index.php/OXP#Locating_your_AddOns_folder
        List<File> addonsFolders = new ArrayList<>();
        switch (oolite.starter.util.Util.getOperatingSystemType()) {
            case LINUX:
                addonsFolders.add(new File(System.getProperty("user.home")+"/GNUstep/Applications/Oolite/AddOns"));
                addonsFolders.add(new File(System.getProperty("user.home")+"/.Oolite/Add-ons"));
                addonsFolders.add(new File("/opt/Oolite/AddOns"));
                break;
            case MACOS:
                addonsFolders.add(new File(ooliteExecutable.getParentFile(), "AddOns"));
                addonsFolders.add(new File(System.getProperty("user.home")+"/Library/Application Support/Oolite/AddOns"));
                break;
            case WINDOWS:
                addonsFolders.add(new File(ooliteExecutable.getParentFile(), "AddOns"));
                break;
            case OTHER:
                break;
        }
        File debugOxp = findDebugOxp(addonsFolders);

        if (debugOxp == null) {
            log.debug("Neither Basic-Debug.oxp nor Debug.oxp found in AddOns folders {}", addonsFolders);
            throw new Exception("Debug OXP not found. Is this a developer release?");
        }

        // check debug configuration
        File debugConfig = new File(debugOxp, "Config/debugConfig.plist");
        NSDictionary root = (NSDictionary)PropertyListParser.parse(debugConfig);
        if (oolite.starter.util.Util.isMac()) {
            // Macintosh Oolite uses per default the builtin debug console
            // so it must be configured
            if(!root.containsKey("console-host")) {
                throw new Exception("Debug Console not configured for external connection.");
            }
        }
        // if configured, then please connect to 127.0.0.1
        if(root.containsKey("console-host")) {
            if (!"127.0.0.1".equals(String.valueOf(root.get("console-host")))) {
                throw new Exception("Debug Console must connect to to 127.0.0.1.");
            }
        }
    }
}
