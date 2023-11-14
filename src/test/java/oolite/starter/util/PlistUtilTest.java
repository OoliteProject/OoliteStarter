/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

package oolite.starter.util;

import com.chaudhuri.plist.PlistParser;
import java.io.InputStream;
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
public class PlistUtilTest {
    private static final Logger log = LogManager.getLogger();

     public PlistUtilTest() {
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
     * Test of parsePListList method, of class PlistUtil.
     */
    @Test
    public void testParsePListList() throws Exception {
        log.info("parsePListList");
        InputStream in = null;
        String sourceName = "";
        PlistParser.ListContext expResult = null;
        try {
            PlistUtil.parsePListList(in, sourceName);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("in must not be null", e.getMessage());
            log.debug("caught expected exception");
        }
    }

    /**
     * Test of parsePListDict method, of class PlistUtil.
     */
    @Test
    public void testParsePListDict() throws Exception {
        log.info("parsePListDict");
        InputStream in = null;
        String sourceName = "";
        PlistParser.DictionaryContext expResult = null;
        try {
            PlistUtil.parsePListDict(in, sourceName);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("in must not be null", e.getMessage());
            log.debug("caught expected exception");
        }
    }

}