/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

package oolite.starter.util;

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
            Util.execReadToString(execCommand);
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

}