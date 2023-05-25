/*
 */
package oolite.starter;

import com.chaudhuri.plist.PlistParser;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import oolite.starter.model.Expansion;
import oolite.starter.model.SaveGame;
import org.antlr.v4.runtime.misc.ParseCancellationException;
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

    @Test
    public void testCreateExpansion_Dictionary() throws IOException {
        log.info("testCreateExpansion_Dictionary");
        
        URL url = getClass().getResource("/data/expansion.plist");
        
        Oolite oolite = new Oolite();
        PlistParser.DictionaryContext dc = PlistUtil.parsePListDict(url.openStream(), url.toString());
        Expansion expansion = oolite.createExpansion(dc);
        
        assertEquals("oolite.oxp.cim.camera-drones", expansion.getIdentifier());
        assertEquals("1.4", expansion.getVersion());
    }
    
    @Test
    public void testCreateExpansion_InputStream() throws IOException {
        log.info("testCreateExpansion_InputStream");
        
        //URL url = getClass().getResource("/data/Jameson.oolite-save");
        URL url = new URL("http://localhost:3/nononono");
        Oolite oolite = new Oolite();
        
        try {
            oolite.createExpansion(url.openStream(), url.toString());
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
        Expansion expansion = oolite.createExpansion(url.openStream(), url.toString());
        
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
    public void testGetOnlineExpansions() throws MalformedURLException {
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
    public void testGetOnlineExpansions2() throws MalformedURLException {
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

        File dir = new File("src/test/resources/data");
        List<File> dirs = new ArrayList<>();
        dirs.add(dir);
        
        Oolite oolite = new Oolite();
        Configuration configuration = Mockito.mock(Configuration.class);
        Mockito.when(configuration.getDeactivatedAddonsDir()).thenReturn(dir);
        Mockito.when(configuration.getAddonDirs()).thenReturn(dirs);
        oolite.setConfiguration(configuration);
        
        List<Expansion> expansions = oolite.getLocalExpansions();
        assertEquals(1, expansions.size());
    }

}
