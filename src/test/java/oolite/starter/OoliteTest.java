/*
 */
package oolite.starter;

import oolite.starter.util.PlistUtil;
import com.chaudhuri.plist.PlistParser;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import oolite.starter.model.Command;
import oolite.starter.model.Expansion;
import oolite.starter.model.ExpansionReference;
import oolite.starter.model.Installation;
import oolite.starter.model.ProcessData;
import oolite.starter.model.SaveGame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author hiran
 */
public class OoliteTest {
    private final static Logger log = LogManager.getLogger();
    
    public OoliteTest() {
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
     * Test of setConfiguration method, of class Oolite.
     */
    @Test
    public void testSetConfiguration() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.info("testSetConfiguration");

        Oolite instance = new Oolite();
        try {
            instance.getSaveGames();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("configuration must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    /**
     * Test of setConfiguration method, of class Oolite.
     */
    @Test
    public void testSetConfiguration2() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.info("testSetConfiguration2");

        Oolite instance = new Oolite();
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getSaveGameDir()).thenReturn(new File("target/test/savegames"));
        instance.setConfiguration(configuration);

        List<SaveGame> sgs = instance.getSaveGames();
        assertEquals(0, sgs.size());
    }

    /**
     * Test of setConfiguration method, of class Oolite.
     */
    @Test
    public void testSetConfiguration3() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.info("testSetConfiguration3");

        Oolite instance = new Oolite();
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getSaveGameDir()).thenReturn(new File("target/test/savegames"));
        instance.setConfiguration(configuration);

        List<SaveGame> sgs = instance.getSaveGames();
        assertEquals(0, sgs.size());
        
        instance.setConfiguration(null);
        try {
            instance.getSaveGames();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("configuration must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    @Test
    public void testCreateExpansionFromManifest_Dictionary() throws IOException {
        log.info("testCreateExpansionFromManifest_Dictionary");
        
        URL url = getClass().getResource("/data/expansion.plist");
        
        Oolite oolite = new Oolite();
        PlistParser.DictionaryContext dc = PlistUtil.parsePListDict(url.openStream(), url.toString());
        Expansion expansion = oolite.createExpansionFromManifest(dc);
        
        assertEquals("oolite.oxp.cim.camera-drones", expansion.getIdentifier());
        assertEquals("1.4", expansion.getVersion());
    }
    
    @Test
    public void testCreateExpansionFromRequires_Dictionary() throws IOException {
        log.info("testCreateExpansionFromRequires_Dictionary");
        
        URL url = getClass().getResource("/data/PHKB_Folder.oxp/Asteroids3D1.2.oxp/requires.plist");
        
        Oolite oolite = new Oolite();
        PlistParser.DictionaryContext dc = PlistUtil.parsePListDict(url.openStream(), url.toString());
        Expansion expansion = oolite.createExpansionFromRequiresPlist(dc);
        
        assertEquals(null, expansion.getIdentifier());
        assertEquals(null, expansion.getVersion());
        assertEquals("1.76", expansion.getRequiredOoliteVersion());
        assertEquals("1.90", expansion.getMaximumOoliteVersion());
    }
    
    @Test
    public void testCreateExpansion_InputStream() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.info("testCreateExpansion_InputStream");
        
        //URL url = getClass().getResource("/data/Jameson.oolite-save");
        URL url = new URL("http://localhost:3/nononono");
        Oolite oolite = new Oolite();
        
        try {
            oolite.createExpansionFromManifest(url.openStream(), url.toString());
            fail("expected exception");
        } catch (ConnectException e) {
            assertEquals("Connection refused", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testCreateExpansion_InputStream2() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.info("testCreateExpansion_InputStream2");
        
        URL url = getClass().getResource("/data/expansion.plist");
        
        Oolite oolite = new Oolite();
        Expansion expansion = oolite.createExpansionFromManifest(url.openStream(), url.toString());
        
        assertEquals("oolite.oxp.cim.camera-drones", expansion.getIdentifier());
        assertEquals("1.4", expansion.getVersion());
    }
    
    @Test
    public void testCreateExpansion_InputStream3() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.info("testCreateExpansion_InputStream3");
        
        URL url = getClass().getResource("/data/manifest.xml");
        
        Oolite oolite = new Oolite();
        Expansion expansion = oolite.createExpansionFromManifest(url.openStream(), url.toString());
        
        assertEquals("oolite.oxp.EricWalch.PirateCove", expansion.getIdentifier());
        assertEquals("1.4.2", expansion.getVersion());
    }
    
    @Test
    public void testCreateSaveGame() throws IOException {
        log.info("testCreateSaveGame");

        File sgFile = new File("src/test/resources/data/expansion.plist");
        Oolite oolite = new Oolite();
        try {
            SaveGame sg = oolite.createSaveGame(sgFile);
            fail("expected exception");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Could not parse "));
            assertTrue(e.getMessage().contains("src/test/resources/data/expansion.plist"));
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testCreateSaveGame2() throws IOException {
        log.info("testCreateSaveGame2");
        
        File sgFile = new File("src/test/resources/data/Jameson.oolite-save");
        
        Oolite oolite = new Oolite();
        SaveGame sg = oolite.createSaveGame(sgFile);
        assertEquals("Jameson", sg.getName());
        assertEquals("Lave", sg.getCurrentSystemName());
        assertEquals("1.90", sg.getOoliteVersion());
        assertEquals("Jameson", sg.getPlayerName());
        assertEquals("Cobra Mark III", sg.getShipClassName());
        assertEquals(1000, sg.getCredits());
    }

    /**
     * Test with a savegame including resource paths.
     * @throws IOException 
     */
    @Test
    public void testCreateSaveGame3() throws IOException {
        log.info("testCreateSaveGame3");
        
        File sgFile = new File("src/test/resources/data/Jaeger3.oolite-save");
        
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Mockito.when(configuration.getManagedAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Oolite oolite = new Oolite();
        oolite.setConfiguration(configuration);
        SaveGame sg = oolite.createSaveGame(sgFile);
        assertEquals("Jaeger3", sg.getName());
        assertEquals("Riinus", sg.getCurrentSystemName());
        assertEquals("1.91", sg.getOoliteVersion());
        assertEquals("Bright", sg.getPlayerName());
        assertEquals("Imperial Courier", sg.getShipClassName());
        assertEquals(999999986792078L, sg.getCredits());
        assertEquals(100, sg.getExpansions().size());
    }
    
    @Test
    public void testGetSaveGames() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.info("testGetSaveGames");
        
        Oolite oolite = new Oolite();
        try {
            oolite.getSaveGames();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("configuration must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testGetSaveGames2() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        log.info("testGetSaveGames2");
        
        File sgDir = new File("src/test/resources/data");

        Oolite oolite = new Oolite();
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getSaveGameDir()).thenReturn(sgDir);
        oolite.setConfiguration(configuration);
        
        List<SaveGame> saveGames = oolite.getSaveGames();
        assertEquals(1, saveGames.size());
    }
    
    @Test
    public void testGetOnlineExpansions() throws MalformedURLException, IOException {
        log.debug("testGetOnlineExpansions");
        
        Oolite oolite = new Oolite();
        try {
            oolite.getOnlineExpansions();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("configuration must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    @Test
    public void testGetOnlineExpansions2() throws MalformedURLException, IOException {
        log.info("testGetOnlineExpansions2");
        
        Oolite oolite = new Oolite();
        Configuration configuration = Mockito.mock(Configuration.class);
        List<URL> urls = new ArrayList<>();
        urls.add(new File("src/test/resources/data/expansions.plist").toURI().toURL());
        Mockito.when(configuration.getExpansionManagerURLs()).thenReturn(urls);
        oolite.setConfiguration(configuration);
        
        log.debug("expansion manager urls: {}", configuration.getExpansionManagerURLs());

        List<Expansion> expansions = oolite.getOnlineExpansions();
        assertEquals(2, expansions.size());
    }

    @Test
    public void testGetLocalExpansions() throws MalformedURLException {
        log.debug("testGetLocalExpansions");
        
        Oolite oolite = new Oolite();
        try {
            oolite.getLocalExpansions();
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("configuration must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    @Test
    public void testGetLocalExpansions2() throws MalformedURLException {
        log.debug("testGetLocalExpansions2");

        File dir = new File("src/test/resources/data/PHKB_Folder.oxp");
        List<File> dirs = new ArrayList<>();
        dirs.add(dir);
        
        Oolite oolite = new Oolite();
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getDeactivatedAddonsDir()).thenReturn(dir);
        Mockito.when(configuration.getAddonDirs()).thenReturn(dirs);
        oolite.setConfiguration(configuration);
        
        List<Expansion> expansions = oolite.getLocalExpansions();
        assertEquals(2, expansions.size());
        assertTrue(String.valueOf(expansions.get(0).getIdentifier()).endsWith("Asteroids3D1.2.oxp"));
        assertTrue(String.valueOf(expansions.get(1).getIdentifier()).endsWith("Galactic_Navy 5.4.3.oxp"));
    }
    
    @Test
    public void testGetLocalExpansions3() throws MalformedURLException {
        log.debug("testGetLocalExpansions3");

        File dir = new File("src/test/resources/data");
        List<File> dirs = new ArrayList<>();
        dirs.add(dir);
        
        Oolite oolite = new Oolite();
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getDeactivatedAddonsDir()).thenReturn(dir);
        Mockito.when(configuration.getAddonDirs()).thenReturn(dirs);
        oolite.setConfiguration(configuration);
        
        List<Expansion> expansions = oolite.getLocalExpansions();
        assertEquals(3, expansions.size());
        assertTrue(String.valueOf(expansions.get(0).getIdentifier()).endsWith("Asteroids3D1.2.oxp"));
        assertTrue(String.valueOf(expansions.get(1).getIdentifier()).endsWith("Galactic_Navy 5.4.3.oxp"));
        assertEquals("oolite.oxp.Norby.Addons_for_Beginners", expansions.get(2).getIdentifier());
    }
    
    @Test
    public void testOoliteListener() throws IOException, InterruptedException {
        log.info("testOoliteListener");
 
        Oolite.OoliteListener listener = Mockito.mock(Oolite.OoliteListener.class);
        Oolite oolite = new Oolite();
        
        List<String> command = new ArrayList<>();
        ProcessData pd = new ProcessData(new File("."), command, 123);
        
        Mockito.verify(listener, Mockito.times(0)).launched(Mockito.any());
        Mockito.verify(listener, Mockito.times(0)).terminated();
        
        oolite.fireLaunched(pd);
        Mockito.verify(listener, Mockito.times(0)).launched(Mockito.any());
        Mockito.verify(listener, Mockito.times(0)).terminated();

        oolite.fireTerminated();
        Mockito.verify(listener, Mockito.times(0)).launched(Mockito.any());
        Mockito.verify(listener, Mockito.times(0)).terminated();

        oolite.addOoliteListener(listener);

        oolite.fireLaunched(pd);
        Mockito.verify(listener, Mockito.times(1)).launched(Mockito.any());
        Mockito.verify(listener, Mockito.times(0)).terminated();

        oolite.fireTerminated();
        Mockito.verify(listener, Mockito.times(1)).launched(Mockito.any());
        Mockito.verify(listener, Mockito.times(1)).terminated();

        oolite.removeOoliteListener(listener);
        oolite.fireLaunched(pd);
        Mockito.verify(listener, Mockito.times(1)).launched(Mockito.any());
        Mockito.verify(listener, Mockito.times(1)).terminated();

        oolite.fireTerminated();
        Mockito.verify(listener, Mockito.times(1)).launched(Mockito.any());
        Mockito.verify(listener, Mockito.times(1)).terminated();
    }
    
    @Test
    public void testGetVersionFromInfoPlist() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        log.info("testGetVersionFromInfoPlist");
        
        File infoPlist = new File("src/test/resources/data/MacOS/Info.plist");
        log.warn("reading {}", infoPlist);
        
        String v = Oolite.getVersionFromInfoPlist(infoPlist);
        
        assertEquals("1.90", v);
    }
    
    @Test
    public void testGetVersionFromManifest() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        log.info("getVersionFromManifest");
        
        File infoPlist = new File("src/test/resources/data/Windows/manifest.plist");
        log.warn("reading {}", infoPlist);
        
        String v = Oolite.getVersionFromManifest(infoPlist);
        
        assertEquals("1.90", v);
    }

    @Test
    public void testRun() throws IOException, InterruptedException, ProcessRunException {
        log.info("testRun()");
        
        List<String> command = new ArrayList<>();
        command.add("/usr/bin/ls");
        
        Oolite oolite = new Oolite();
        oolite.run(command, new File("."));
        assertTrue(true);
    }
    
    @Test
    public void testDiff() {
        log.info("testDiff");
        
        Oolite oolite = new Oolite();
        try {
            oolite.diff(null, null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("want must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testDiff2() {
        log.info("testDiff2");
        
        Oolite oolite = new Oolite();
        List<ExpansionReference> want = new ArrayList<>();
        try {
            oolite.diff(want, null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("have must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testDiff3() {
        log.info("testDiff3");
        
        List<ExpansionReference> want = new ArrayList<>();
        List<ExpansionReference> have = new ArrayList<>();
        
        Oolite oolite = new Oolite();
        List<ExpansionReference> result = oolite.diff(want, have);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testDiff4() {
        log.info("testDiff4");

        Oolite oolite = new Oolite();
        
        List<ExpansionReference> want = new ArrayList<>();
        want.add(new ExpansionReference("A"));
        List<ExpansionReference> have = new ArrayList<>();
        
        List<ExpansionReference> result = oolite.diff(want, have);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getName());
        assertEquals(ExpansionReference.Status.MISSING, result.get(0).getStatus());
    }

    @Test
    public void testDiff5() {
        log.info("testDiff5");

        Oolite oolite = new Oolite();
        
        List<ExpansionReference> want = new ArrayList<>();
        List<ExpansionReference> have = new ArrayList<>();
        have.add(new ExpansionReference("B"));
        
        List<ExpansionReference> result = oolite.diff(want, have);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("B", result.get(0).getName());
        assertEquals(ExpansionReference.Status.SURPLUS, result.get(0).getStatus());
    }

    @Test
    public void testDiff6() {
        log.info("testDiff6");

        Oolite oolite = new Oolite();
        
        List<ExpansionReference> want = new ArrayList<>();
        want.add(new ExpansionReference("A"));
        List<ExpansionReference> have = new ArrayList<>();
        have.add(new ExpansionReference("B"));
        
        List<ExpansionReference> result = oolite.diff(want, have);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("B", result.get(0).getName());
        assertEquals(ExpansionReference.Status.SURPLUS, result.get(0).getStatus());
        assertEquals("A", result.get(1).getName());
        assertEquals(ExpansionReference.Status.MISSING, result.get(1).getStatus());
    }

    @Test
    public void testDiff7() {
        log.info("testDiff7");

        Oolite oolite = new Oolite();
        
        List<ExpansionReference> want = new ArrayList<>();
        want.add(new ExpansionReference("A"));
        List<ExpansionReference> have = new ArrayList<>();
        have.add(new ExpansionReference("A"));
        
        List<ExpansionReference> result = oolite.diff(want, have);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testDiff8() {
        log.info("testDiff8");

        Oolite oolite = new Oolite();
        
        List<ExpansionReference> want = new ArrayList<>();
        want.add(new ExpansionReference("A"));
        want.add(new ExpansionReference("B"));
        List<ExpansionReference> have = new ArrayList<>();
        have.add(new ExpansionReference("B"));
        have.add(new ExpansionReference("C"));
        
        List<ExpansionReference> result = oolite.diff(want, have);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("C", result.get(0).getName());
        assertEquals(ExpansionReference.Status.SURPLUS, result.get(0).getStatus());
        assertEquals("A", result.get(1).getName());
        assertEquals(ExpansionReference.Status.MISSING, result.get(1).getStatus());
    }
    
    @Test
    public void testContains() {
        log.info("testContains");
        
        Oolite instance = new Oolite();
        try {
            instance.contains(null, null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("list must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testContains2() {
        log.info("testContains2");
        
        Oolite instance = new Oolite();
        List<ExpansionReference> list = new ArrayList<>();
        list.add(new ExpansionReference("myexpansion"));
        try {
            instance.contains(list, null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("expansion must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testContains3() {
        log.info("testContains3");
        
        Oolite instance = new Oolite();
        List<ExpansionReference> list = new ArrayList<>();
        list.add(new ExpansionReference("myexpansion"));
        Expansion expansion = new Expansion();
        expansion.setLocalFile(new File("other"));
        assertFalse(instance.contains(list, expansion));
    }
    
    @Test
    public void testContains4() {
        log.info("testContains4");
        
        Oolite instance = new Oolite();
        List<ExpansionReference> list = new ArrayList<>();
        list.add(new ExpansionReference("myexpansion"));
        Expansion expansion = new Expansion();
        expansion.setLocalFile(new File("myexpansion"));
        assertTrue(instance.contains(list, expansion));
    }
    
    @Test
    public void testCreateExpansionFromManifest() throws XPathExpressionException {
        log.info("testCreateExpansionFromManifest");
        
        Oolite instance = new Oolite();
        try {
            instance.createExpansionFromManifest((Document)null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("doc must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testCreateExpansionFromManifest2() throws XPathExpressionException, ParserConfigurationException {
        log.info("testCreateExpansionFromManifest2");
        
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.newDocument();
        Oolite instance = new Oolite();
        
        
        try {
            instance.createExpansionFromManifest(doc);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("doc must have root element", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testCreateExpansionFromManifest3() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        log.info("testCreateExpansionFromManifest3");
        
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new File("src/test/resources/data/manifest.xml"));
        Oolite instance = new Oolite();
        
        Expansion result = instance.createExpansionFromManifest(doc);
        
        assertNotNull(result);
        assertEquals(2, result.getConflictOxps().size());
        assertEquals("Dependency{identifier=oolite.conflictoxp.exp1, version=1.0, description=, maximumVersion=}", String.valueOf(result.getConflictOxps().get(0)));
        assertEquals("Dependency{identifier=oolite.conflictoxp.exp2, version=1.0, description=, maximumVersion=}", String.valueOf(result.getConflictOxps().get(1)));
        assertEquals(3, result.getRequiresOxps().size());
        assertEquals("Dependency{identifier=oolite.requiredoxp.exp1, version=1.0, description=, maximumVersion=}", String.valueOf(result.getRequiresOxps().get(0)));
        assertEquals("Dependency{identifier=oolite.requiredoxp.exp2, version=1.0, description=, maximumVersion=}", String.valueOf(result.getRequiresOxps().get(1)));
        assertEquals("Dependency{identifier=oolite.requiredoxp.exp3, version=1.0, description=, maximumVersion=}", String.valueOf(result.getRequiresOxps().get(2)));
        assertEquals(4, result.getOptionalOxps().size());
        assertEquals("Dependency{identifier=oolite.optionaloxp.exp1, version=1.0, description=, maximumVersion=}", String.valueOf(result.getOptionalOxps().get(0)));
        assertEquals("Dependency{identifier=oolite.optionaloxp.exp2, version=1.0, description=, maximumVersion=}", String.valueOf(result.getOptionalOxps().get(1)));
        assertEquals("Dependency{identifier=oolite.optionaloxp.exp3, version=1.0, description=, maximumVersion=}", String.valueOf(result.getOptionalOxps().get(2)));
        assertEquals("Dependency{identifier=oolite.optionaloxp.exp4, version=1.0, description=, maximumVersion=}", String.valueOf(result.getOptionalOxps().get(3)));
        
        assertEquals("Hermits, Pirates", result.getTags());
    }
    
    @Test
    public void testGetExpansionByReference() {
        log.info("testGetExpansionByReference()");

        Oolite instance = new Oolite();
        try {
            instance.getExpansionByReference(null, null, false);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("reference must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testGetExpansionByReference2() {
        log.info("testGetExpansionByReference2()");

        Oolite instance = new Oolite();
        String reference = "";
        try {
            instance.getExpansionByReference(reference, null, false);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("expansions must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testGetExpansionByReference3() {
        log.info("testGetExpansionByReference3()");

        Oolite instance = new Oolite();
        String reference = "";
        List<Expansion> expansions = new ArrayList<>();
        List<Expansion> result = instance.getExpansionByReference(reference, expansions, false);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testGetExpansionByReference4() {
        log.info("testGetExpansionByReference4()");

        Oolite instance = new Oolite();
        String reference = "foolike";
        Expansion e1 = new Expansion();
        e1.setIdentifier("foobar");
        Expansion e2 = new Expansion();
        e2.setIdentifier("foolike");
        List<Expansion> expansions = new ArrayList<>();
        expansions.add(e1);
        expansions.add(e2);
        
        List<Expansion> result = instance.getExpansionByReference(reference, expansions, false);
        assertNotNull(result);
        assertEquals(1, result.size());
    }
    
    @Test
    public void testGetExpansionByReference5() {
        log.info("testGetExpansionByReference5()");

        Oolite instance = new Oolite();
        String reference = "foolike";
        Expansion e1 = new Expansion();
        e1.setIdentifier("foobar");
        e1.setOolite(instance);
        Expansion e2 = new Expansion();
        e2.setIdentifier("foolike");
        e2.setOolite(instance);
        List<Expansion> expansions = new ArrayList<>();
        expansions.add(e1);
        expansions.add(e2);
        
        List<Expansion> result = instance.getExpansionByReference(reference, expansions, true);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testGetExpansionByReference6() {
        log.info("testGetExpansionByReference6()");

        Oolite instance = new Oolite();
        String reference = "foolike:1";
        Expansion e1 = new Expansion();
        e1.setIdentifier("foobar");
        Expansion e2 = new Expansion();
        e2.setIdentifier("foolike");
        List<Expansion> expansions = new ArrayList<>();
        expansions.add(e1);
        expansions.add(e2);
        
        List<Expansion> result = instance.getExpansionByReference(reference, expansions, false);
        assertNotNull(result);
        assertEquals(1, result.size());
    }
    
    @Test
    public void testGetExpansionByReference7() {
        log.info("testGetExpansionByReference7()");

        Oolite instance = new Oolite();
        String reference = "foolike:1";
        Expansion e1 = new Expansion();
        e1.setIdentifier("foobar");
        e1.setOolite(instance);
        Expansion e2 = new Expansion();
        e2.setIdentifier("foolike");
        e2.setOolite(instance);
        List<Expansion> expansions = new ArrayList<>();
        expansions.add(e1);
        expansions.add(e2);
        
        List<Expansion> result = instance.getExpansionByReference(reference, expansions, true);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testValidateConflicts() {
        log.info("testValidateConflicts()");

        Oolite instance = new Oolite();
        try {
            instance.validateConflicts(null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("expansions must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testValidateConflicts2() {
        log.info("testValidateConflicts2()");

        Oolite instance = new Oolite();
        Expansion e1 = Mockito.mock(Expansion.class);
        Expansion.EMStatus ems1 = new Expansion.EMStatus();
        Mockito.when(e1.getEMStatus()).thenReturn(ems1);
        e1.setIdentifier("e1");
        Mockito.when(e1.getConflictOxps()).thenReturn(Arrays.asList(new Expansion.Dependency("e2", "1"), new Expansion.Dependency("e3", "1")));
        Mockito.when(e1.isEnabled()).thenReturn(true);
        //e1.setOolite(instance);
        Expansion e2 = Mockito.mock(Expansion.class);
        Expansion.EMStatus ems2 = new Expansion.EMStatus();
        Mockito.when(e2.getEMStatus()).thenReturn(ems2);
        Mockito.when(e2.getIdentifier()).thenReturn("e2");
        Mockito.when(e2.isEnabled()).thenReturn(true);
        //e2.setOolite(instance);
        Expansion e3 = Mockito.mock(Expansion.class);
        Expansion.EMStatus ems3 = new Expansion.EMStatus();
        Mockito.when(e3.getEMStatus()).thenReturn(ems3);
        Mockito.when(e3.getIdentifier()).thenReturn("e3");
        Mockito.when(e3.isEnabled()).thenReturn(true);
        //e3.setOolite(instance);
        Expansion e4 = Mockito.mock(Expansion.class);
        Expansion.EMStatus ems4 = new Expansion.EMStatus();
        Mockito.when(e4.getEMStatus()).thenReturn(ems4);
        Mockito.when(e4.getIdentifier()).thenReturn("e4");
        Mockito.when(e4.isEnabled()).thenReturn(true);
        //e4.setOolite(instance);
        List<Expansion> expansions = new ArrayList<>();
        expansions.add(e1);
        expansions.add(e2);
        expansions.add(e3);
        expansions.add(e4);
        
        instance.validateConflicts(expansions);
        
        assertEquals(true, e1.getEMStatus().isConflicting());
        assertEquals(true, e2.getEMStatus().isConflicting());
        assertEquals(true, e3.getEMStatus().isConflicting());
        assertEquals(false, e4.getEMStatus().isConflicting());
    }
    
    @Test
    public void testCheckForUpdates() {
        log.info("testCheckForUpdates");
        
        Oolite instance = new Oolite();
        try {
            instance.checkForUpdates(null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("expansions must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testCheckForUpdates2() {
        log.info("testCheckForUpdates2");
        
        Oolite instance = new Oolite();
        List<Expansion> expansions = new ArrayList<>();
        List<Command> result = instance.checkForUpdates(expansions);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testCheckForUpdates3() {
        log.info("testCheckForUpdates3");
        
        Oolite instance = new Oolite();
        Expansion e1 = Mockito.mock(Expansion.class);
        Expansion.EMStatus ems1 = new Expansion.EMStatus();
        Mockito.when(e1.getEMStatus()).thenReturn(ems1);
        Mockito.when(e1.isEnabled()).thenReturn(true);
        Mockito.when(e1.getIdentifier()).thenReturn("expansion");
        Mockito.when(e1.getVersion()).thenReturn("1.0");

        Expansion e2 = Mockito.mock(Expansion.class);
        Expansion.EMStatus ems2 = new Expansion.EMStatus();
        Mockito.when(e2.getEMStatus()).thenReturn(ems2);
        Mockito.when(e2.isEnabled()).thenReturn(false);
        Mockito.when(e2.getIdentifier()).thenReturn("expansion");
        Mockito.when(e2.getVersion()).thenReturn("1.1");
        
        List<Expansion> expansions = new ArrayList<>();
        expansions.add(e1);
        expansions.add(e2);
        
        List<Command> result = instance.checkForUpdates(expansions);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Command.Action.DELETE, result.get(0).getAction());
        assertEquals(e1, result.get(0).getExpansion());
        assertEquals(Command.Action.INSTALL, result.get(1).getAction());
        assertEquals(e2, result.get(1).getExpansion());
    }
    
    @Test
    public void testCheckSurplusExpansions() {
        log.info("testCheckSurplusExpansions");

        Oolite instance = new Oolite();
        try {
            instance.checkSurplusExpansions(null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("references must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testCheckSurplusExpansions2() {
        log.info("testCheckSurplusExpansions2");

        File dir = new File("src/test/resources/data");
        List<File> dirs = new ArrayList<>();
        dirs.add(dir);
        
        Oolite instance = new Oolite();
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getDeactivatedAddonsDir()).thenReturn(dir);
        Mockito.when(configuration.getAddonDirs()).thenReturn(dirs);
        instance.setConfiguration(configuration);
        
        instance.setConfiguration(configuration);
        List<ExpansionReference> references = new ArrayList<>();
        references.add(new ExpansionReference("oolite.oxp.Norby.Addons_for_Beginners:1.5"));
        
        instance.checkSurplusExpansions(references);
        
        assertEquals(4, references.size());
        assertEquals("oolite.oxp.Norby.Addons_for_Beginners:1.5", references.get(0).getName());
        assertTrue(references.get(1).getName().endsWith("Asteroids3D1.2.oxp@0"));
        assertTrue(references.get(2).getName().endsWith("Galactic_Navy 5.4.3.oxp@0"));
        assertEquals("oolite.oxp.Norby.Addons_for_Beginners@1.5", references.get(3).getName());
    }
    
    @Test
    public void testParseVersion() {
        log.info("testParseVersion");

        Oolite instance = new Oolite();
        try {
            instance.parseVersion(null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("version must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testParseVersion2() {
        log.info("testParseVersion2");

        Oolite instance = new Oolite();
        assertEquals("1.2-blah.3", instance.parseVersion("1.2-blah.3").toString());
    }
    
    @Test
    public void testParseVersion3() {
        log.info("testParseVersion3");

        Oolite instance = new Oolite();
        assertEquals("1.2", instance.parseVersion("v1.2").toString());
    }
    
    @Test
    public void testGetAllExpansions() throws IOException {
        log.info("testGetAllExpansions");

        File dir = new File("src/test/resources/data");
        List<File> dirs = new ArrayList<>();
        dirs.add(dir);
        
        Installation installation = Mockito.mock(Installation.class);
        Mockito.when(installation.getVersion()).thenReturn("1.90");
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getActiveInstallation()).thenReturn(installation);
        Mockito.when(configuration.getDeactivatedAddonsDir()).thenReturn(dir);
        Mockito.when(configuration.getAddonsDir()).thenReturn(dir);
        Mockito.when(configuration.getAddonDirs()).thenReturn(dirs);
        Oolite instance = new Oolite();
        instance.setConfiguration(configuration);
        
        List<Expansion> result = instance.getAllExpansions();
        
        assertNotNull(result);
    }
    
    @Test
    public void testParseExpansionSet() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        log.info("testParseExpansionSet");
        
        Oolite instance = new Oolite();
        try {
            instance.parseExpansionSet(null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("source must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testParseExpansionSet2() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        log.info("testParseExpansionSet2");
        
        Oolite instance = new Oolite();
        File file = new File("src/test/resources/data/Arquebus X Expansion Set.oolite-es");

        NodeList nl = instance.parseExpansionSet(file);
        assertNotNull(nl);
        assertEquals(374, nl.getLength());
    }

    @Test
    public void testValidateCompatibility() {
        log.info("testValidateCompatibility");

        Oolite instance = new Oolite();
        try {
            instance.validateCompatibility(null);
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("configuration must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    @Test
    public void testValidateCompatibility2() {
        log.info("testValidateCompatibility2");

        Installation installation = Mockito.mock(Installation.class);
        Mockito.when(installation.getVersion()).thenReturn("1.90");
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getActiveInstallation()).thenReturn(installation);
        Oolite instance = new Oolite();
        instance.setConfiguration(configuration);
        try {
            instance.validateCompatibility(null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("expansions must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }


    /**
     * e1 has no requirements
     */
    @Test
    public void testValidateCompatibility3() {
        log.info("testValidateCompatibility3");

        Installation installation = Mockito.mock(Installation.class);
        Mockito.when(installation.getVersion()).thenReturn("1.90");
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getActiveInstallation()).thenReturn(installation);
        Oolite instance = new Oolite();
        instance.setConfiguration(configuration);
        
        List<Expansion> expansions = new ArrayList<>();
        Expansion e1 = new Expansion();
        expansions.add(e1);
        Expansion e2 = new Expansion();
        e2.setRequiredOoliteVersion("1.88");
        expansions.add(e2);
        Expansion e3 = new Expansion();
        e3.setRequiredOoliteVersion("1.92");
        expansions.add(e3);
        Expansion e4 = new Expansion();
        e4.setRequiredOoliteVersion("1.92");
        expansions.add(e4);
        Expansion e5 = new Expansion();
        e5.setMaximumOoliteVersion("1.92");
        expansions.add(e5);
        Expansion e6 = new Expansion();
        e6.setMaximumOoliteVersion("1.88");
        expansions.add(e6);

        instance.validateCompatibility(expansions);
        
        assertEquals(6, expansions.size());
        assertEquals(false, e1.getEMStatus().isIncompatible());
        assertEquals(false, e2.getEMStatus().isIncompatible());
        assertEquals(true, e3.getEMStatus().isIncompatible());
        assertEquals(true, e4.getEMStatus().isIncompatible());
        assertEquals(false, e5.getEMStatus().isIncompatible());
        assertEquals(true, e6.getEMStatus().isIncompatible());
    }
    
    @Test
    public void testParseDepencencyList() {
        log.info("testParseDepencencyList");

        Oolite instance = new Oolite();
        try {
            instance.parseDependencyList(null);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("vc must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    @Test
    public void testGetExpansionReference() {
        log.info("testGetExpansionReference");
        Expansion.Dependency dep = new Expansion.Dependency("org.oolite.Two", "1.0");

        Oolite instance = new Oolite();
        
        try {
            instance.getExpansionReference(dep);
            fail("expected exception");
        } catch (IllegalStateException e) {
            assertEquals("configuration must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }

    @Test
    public void testGetExpansionReference2() {
        log.info("testGetExpansionReference2");
        Expansion.Dependency dep = new Expansion.Dependency("org.oolite.Two", "1.0");

        Installation installation = Mockito.mock(Installation.class);
        Mockito.when(installation.getVersion()).thenReturn("1.90");
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getActiveInstallation()).thenReturn(installation);
        Mockito.when(configuration.getAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Mockito.when(configuration.getManagedAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Oolite instance = new Oolite();
        instance.setConfiguration(configuration);
        
        ExpansionReference ref = instance.getExpansionReference(dep);
        assertEquals("org.oolite.Two:1.0", ref.getName());
        assertEquals(ExpansionReference.Status.MISSING, ref.getStatus());
    }

    @Test
    public void testGetExpansionReference3() {
        log.info("testGetExpansionReference3");
        String dep = "org.oolite.Two";
        
        Installation installation = Mockito.mock(Installation.class);
        Mockito.when(installation.getVersion()).thenReturn("1.90");
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getActiveInstallation()).thenReturn(installation);
        Mockito.when(configuration.getAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Mockito.when(configuration.getManagedAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Oolite instance = new Oolite();
        instance.setConfiguration(configuration);

        ExpansionReference ref = instance.getExpansionReference(dep);
        assertEquals("org.oolite.Two", ref.getName());
        assertEquals(ExpansionReference.Status.MISSING, ref.getStatus());
    }

    @Test
    public void testGetExpansionReference4() {
        log.info("testGetExpansionReference4");
        String dep = "org.oolite.Two@1.0";
        
        Installation installation = Mockito.mock(Installation.class);
        Mockito.when(installation.getVersion()).thenReturn("1.90");
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getActiveInstallation()).thenReturn(installation);
        Mockito.when(configuration.getAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Mockito.when(configuration.getManagedAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Oolite instance = new Oolite();
        instance.setConfiguration(configuration);

        ExpansionReference ref = instance.getExpansionReference(dep);
        assertEquals("org.oolite.Two:1.0", ref.getName());
        assertEquals(ExpansionReference.Status.MISSING, ref.getStatus());
    }

    @Test
    public void testGetExpansionReference5() {
        log.info("testGetExpansionReference5");
        String dep = "org.oolite.Two:1.0";
        
        Installation installation = Mockito.mock(Installation.class);
        Mockito.when(installation.getVersion()).thenReturn("1.90");
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getActiveInstallation()).thenReturn(installation);
        Mockito.when(configuration.getAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Mockito.when(configuration.getManagedAddonsDir()).thenReturn(new File("src/test/resources/data"));
        Oolite instance = new Oolite();
        instance.setConfiguration(configuration);

        ExpansionReference ref = instance.getExpansionReference(dep);
        assertEquals("org.oolite.Two:1.0", ref.getName());
        assertEquals(ExpansionReference.Status.MISSING, ref.getStatus());
    }
    
}
