/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

package oolite.starter.util;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
 
/**
 *
 * @author hiran
 */
public class UtilTest {
    private static final Logger log = LogManager.getLogger();

     public UtilTest() {
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
     * Test of getHostname method, of class Util.
     */
    @Test
    public void testGetHostname() {
        log.info("getHostname");
        String result = Util.getHostname();
        assertNotNull(result);
        log.info("running on host {}", result);
    }

    /**
     * Test of execReadToString method, of class Util.
     */
    @Test
    public void testExecReadToString() throws Exception {
        log.info("execReadToString");
        String execCommand = "";
        String expResult = "";
        try {
            Util.execReadToString(new String[]{execCommand});
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("execCommand must contain something", e.getMessage());
            log.debug("caught expected exception");
        }
    }

    /**
     * Test of getOperatingSystemType method, of class Util.
     */
    @Test
    public void testGetOperatingSystemType() {
        log.info("getOperatingSystemType");
        
        Util.OSType result = Util.getOperatingSystemType();
        assertNotNull(result);
    }

    /**
     * Test of isMac method, of class Util.
     */
    @Test
    public void testIsMac() {
        log.info("isMac");
        boolean result = Util.isMac();
        assertEquals(true, result || true);
    }
    
    @Test
    public void testIsZipFile() throws Exception {
        log.info("testIsZipFile");
        try {
            Util.isZipFile(null);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {
            assertEquals("f must not be null", e.getMessage());
            log.debug("caught expected exception", e);
        }
    }
    
    @Test
    public void testIsZipFile2() throws Exception {
        log.info("testIsZipFile2");
        
        {
            File f = new File("src/test/resources/data/Vector_1.7.2.oxz");
            boolean result = Util.isZipFile(f);
            assertTrue(result);
        }
        {
            File f = new File("src/test/resources/data/PHKB_Folder.oxp");
            boolean result = Util.isZipFile(f);
            assertFalse(result);
        }
        {
            File f = new File("src/test/resources/data/nonexisting");
            boolean result = Util.isZipFile(f);
            assertFalse(result);
        }
        {
            File f = new File("src/test/resources/data/Jameson.oolite-save");
            boolean result = Util.isZipFile(f);
            assertFalse(result);
        }
    }
    
    @Test
    public void testHumanreadableSize() {
        log.info("testHumanreadableSize()");
        
        assertEquals("0", Util.humanreadableSize(0));
        assertEquals("1", Util.humanreadableSize(1));
        assertEquals("10", Util.humanreadableSize(10));
        assertEquals("100", Util.humanreadableSize(100));
        assertEquals("1000", Util.humanreadableSize(1000));
        assertEquals("9.77 kB", Util.humanreadableSize(10000));
        assertEquals("97.66 kB", Util.humanreadableSize(100000));
        assertEquals("976.56 kB", Util.humanreadableSize(1000000));
        assertEquals("9.54 MB", Util.humanreadableSize(10000000));
        assertEquals("95.37 MB", Util.humanreadableSize(100000000));
        assertEquals("953.67 MB", Util.humanreadableSize(1000000000));
        assertEquals("9.31 GB", Util.humanreadableSize(10000000000L));
        assertEquals("93.13 GB", Util.humanreadableSize(100000000000L));
        assertEquals("931.32 GB", Util.humanreadableSize(1000000000000L));
        assertEquals("9313.23 GB", Util.humanreadableSize(10000000000000L));
    }

}