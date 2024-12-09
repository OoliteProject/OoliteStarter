/*
 */
package oolite.starter.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author hiran
 */
public class SaveGameTest {
    private static final Logger log = LogManager.getLogger();
    
    public SaveGameTest() {
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
     * Test of testSetGetName method, of class SaveGame.
     */
    @Test
    public void testSetGetName() {
        log.info("testSetGetName");

        SaveGame sg = new SaveGame();
        assertNull(sg.getName());
        sg.setName("blue");
        assertEquals("blue", sg.getName());
    }

    /**
     * Test of testSetGetFile method, of class SaveGame.
     */
    @Test
    public void testSetGetFile() {
        log.info("testSetGetFile");

        SaveGame sg = new SaveGame();
        assertNull(sg.getFile());
        
        File f = new File("dummy");
        sg.setFile(f);
        assertEquals(f, sg.getFile());
    }

    /**
     * Test of testSetGetPlayerName method, of class SaveGame.
     */
    @Test
    public void testSetGetPlayerName() {
        log.info("testSetGetPlayerName");

        SaveGame sg = new SaveGame();
        assertNull(sg.getPlayerName());
        
        sg.setPlayerName("bart");
        assertEquals("bart", sg.getPlayerName());
    }

    /**
     * Test of testSetGetCredits method, of class SaveGame.
     */
    @Test
    public void testSetGetCredits() {
        log.info("testSetGetCredits");

        SaveGame sg = new SaveGame();
        assertEquals(0, sg.getCredits());
        
        sg.setCredits(1337);
        assertEquals(1337, sg.getCredits());
    }

    /**
     * Test of testSetGetCurrentSystemName method, of class SaveGame.
     */
    @Test
    public void testSetGetCurrentSystemName() {
        log.info("testSetGetCurrentSystemName");

        SaveGame sg = new SaveGame();
        assertEquals(null, sg.getCurrentSystemName());
        
        sg.setCurrentSystemName("baal");
        assertEquals("baal", sg.getCurrentSystemName());
    }

    /**
     * Test of testSetGetOoliteVersion method, of class SaveGame.
     */
    @Test
    public void testSetGetOoliteVersion() {
        log.info("testSetGetOoliteVersion");

        SaveGame sg = new SaveGame();
        assertEquals(null, sg.getOoliteVersion());
        
        sg.setOoliteVersion("baal");
        assertEquals("baal", sg.getOoliteVersion());
    }

    /**
     * Test of testSetGetShipKills method, of class SaveGame.
     */
    @Test
    public void testSetGetShipKills() {
        log.info("testSetGetShipKills");

        SaveGame sg = new SaveGame();
        assertEquals(0, sg.getShipKills());
        
        sg.setShipKills(1335);
        assertEquals(1335, sg.getShipKills());
    }

    /**
     * Test of testSetGetShipClassName method, of class SaveGame.
     */
    @Test
    public void testSetGetShipClassName() {
        log.info("testSetGetShipClassName");

        SaveGame sg = new SaveGame();
        assertEquals(null, sg.getShipClassName());
        
        sg.setShipClassName("marauder");
        assertEquals("marauder", sg.getShipClassName());
    }

    /**
     * Test of testSetGetShipName method, of class SaveGame.
     */
    @Test
    public void testSetGetShipName() {
        log.info("testSetGetShipName");

        SaveGame sg = new SaveGame();
        assertEquals(null, sg.getShipName());
        
        sg.setShipName("marauder");
        assertEquals("marauder", sg.getShipName());
    }

    /**
     * Test of testToString method, of class SaveGame.
     */
    @Test
    public void testToString() {
        log.info("testToString");

        SaveGame sg = new SaveGame();
        assertEquals("SaveGame{file=null, playerName=null, credits=0, currentSystemName=null, ooliteVersion=null, shipKills=0, shipClassName=null, shipName=null}", sg.toString());
        
        sg.setShipName("shipName");
        assertEquals("SaveGame{file=null, playerName=null, credits=0, currentSystemName=null, ooliteVersion=null, shipKills=0, shipClassName=null, shipName=shipName}", sg.toString());
    }
    
    @Test
    public void testHasMissingExpansions() {
        log.info("testHasMissingExpansions");

        SaveGame sg = new SaveGame();
        assertFalse(sg.hasMissingExpansions());
    }
    
    @Test
    public void testHasMissingExpansions2() {
        log.info("testHasMissingExpansions2");

        SaveGame sg = new SaveGame();
        sg.setExpansions(new ArrayList<>());
        assertFalse(sg.hasMissingExpansions());
    }
    
    @Test
    public void testHasMissingExpansions3() {
        log.info("testHasMissingExpansions3");
        
        List<ExpansionReference> expansions = new ArrayList<>();
        expansions.add(new ExpansionReference("nameOk", ExpansionReference.Status.OK));
        expansions.add(new ExpansionReference("nameConflict", ExpansionReference.Status.CONFLICT));
        expansions.add(new ExpansionReference("nameRequiredMissing", ExpansionReference.Status.REQUIRED_MISSING));
        expansions.add(new ExpansionReference("nameSurplus", ExpansionReference.Status.SURPLUS));

        SaveGame sg = new SaveGame();
        sg.setExpansions(expansions);
        assertFalse(sg.hasMissingExpansions());
    }
    
    @Test
    public void testHasMissingExpansions4() {
        log.info("testHasMissingExpansions4");
        
        List<ExpansionReference> expansions = new ArrayList<>();
        expansions.add(new ExpansionReference("nameOk", ExpansionReference.Status.OK));
        expansions.add(new ExpansionReference("nameConflict", ExpansionReference.Status.CONFLICT));
        expansions.add(new ExpansionReference("nameMissing", ExpansionReference.Status.MISSING));
        expansions.add(new ExpansionReference("nameRequiredMissing", ExpansionReference.Status.REQUIRED_MISSING));
        expansions.add(new ExpansionReference("nameSurplus", ExpansionReference.Status.SURPLUS));

        SaveGame sg = new SaveGame();
        sg.setExpansions(expansions);
        assertTrue(sg.hasMissingExpansions());
    }

    
    @Test
    public void testHasTooManyExpansions() {
        log.info("testHasTooManyExpansions");

        SaveGame sg = new SaveGame();
        assertFalse(sg.hasTooManyExpansions());
    }
    
    @Test
    public void testHasTooManyExpansions2() {
        log.info("testHasTooManyExpansions2");

        SaveGame sg = new SaveGame();
        sg.setExpansions(new ArrayList<>());
        assertFalse(sg.hasTooManyExpansions());
    }
    
    @Test
    public void testHasTooManyExpansions3() {
        log.info("testHasTooManyExpansions3");
        
        List<ExpansionReference> expansions = new ArrayList<>();
        expansions.add(new ExpansionReference("nameOk", ExpansionReference.Status.OK));
        expansions.add(new ExpansionReference("nameConflict", ExpansionReference.Status.CONFLICT));
        expansions.add(new ExpansionReference("nameMissing", ExpansionReference.Status.MISSING));
        expansions.add(new ExpansionReference("nameRequiredMissing", ExpansionReference.Status.REQUIRED_MISSING));

        SaveGame sg = new SaveGame();
        sg.setExpansions(expansions);
        assertFalse(sg.hasTooManyExpansions());
    }
    
    @Test
    public void testHasTooManyExpansions4() {
        log.info("testHasTooManyExpansions4");
        
        List<ExpansionReference> expansions = new ArrayList<>();
        expansions.add(new ExpansionReference("nameOk", ExpansionReference.Status.OK));
        expansions.add(new ExpansionReference("nameConflict", ExpansionReference.Status.CONFLICT));
        expansions.add(new ExpansionReference("nameMissing", ExpansionReference.Status.MISSING));
        expansions.add(new ExpansionReference("nameRequiredMissing", ExpansionReference.Status.REQUIRED_MISSING));
        expansions.add(new ExpansionReference("nameSurplus", ExpansionReference.Status.SURPLUS));

        SaveGame sg = new SaveGame();
        sg.setExpansions(expansions);
        assertTrue(sg.hasTooManyExpansions());
    }
}
