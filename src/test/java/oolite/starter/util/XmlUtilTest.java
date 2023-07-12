/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

package oolite.starter.util;

import java.io.File;
import java.io.InputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.w3c.dom.Document;
 
/**
 *
 * @author hiran
 */
public class XmlUtilTest {
    private static final Logger log = LogManager.getLogger();

     public XmlUtilTest() {
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
     * Test of parseXmlStream method, of class XmlUtil.
     */
    @Test
    public void testParseXmlStream() throws Exception {
        System.out.println("parseXmlStream");
        InputStream in = null;
        Document expResult = null;
        try {
            XmlUtil.parseXmlStream(in);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("in must not be null", e.getMessage());
            log.debug("caught expected exception");
        }
    }

    /**
     * Test of parseXmlFile method, of class XmlUtil.
     */
    @Test
    public void testParseXmlFile() throws Exception {
        System.out.println("parseXmlFile");
        File f = null;
        Document expResult = null;
        try {
            XmlUtil.parseXmlFile(f);
            fail("expected exception");
        } catch (IllegalArgumentException e) {
            assertEquals("f must not be null", e.getMessage());
            log.debug("caught expected exception");
        }
    }

}