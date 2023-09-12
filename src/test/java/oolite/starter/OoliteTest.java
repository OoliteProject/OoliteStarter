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
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import oolite.starter.model.Expansion;
import oolite.starter.model.ExpansionReference;
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
        assertEquals(null, expansion.getMaximumOoliteVersion());
    }
    
    @Test
    public void testCreateExpansion_InputStream() throws IOException {
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
    public void testCreateExpansion_InputStream2() throws IOException {
        log.info("testCreateExpansion_InputStream2");
        
        URL url = getClass().getResource("/data/expansion.plist");
        
        Oolite oolite = new Oolite();
        Expansion expansion = oolite.createExpansionFromManifest(url.openStream(), url.toString());
        
        assertEquals("oolite.oxp.cim.camera-drones", expansion.getIdentifier());
        assertEquals("1.4", expansion.getVersion());
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
}
