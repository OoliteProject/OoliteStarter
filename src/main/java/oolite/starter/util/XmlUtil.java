/*
 */

package oolite.starter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author hiran
 */
public class XmlUtil {
    private static final Logger log = LogManager.getLogger();
    
    /**
     * Do not create instances. We have all static methods.
     */
    private XmlUtil() {
    }

    /**
     * Parses an XML inputstream into a DOM document.
     * 
     * @param in the inputstream to parse
     * @return the DOM Document
     * @throws ParserConfigurationException something went wrong
     * @throws SAXException something went wrong
     * @throws IOException something went wrong
     */
    public static Document parseXmlStream(InputStream in) throws ParserConfigurationException, SAXException, IOException {
        log.debug("parseXmlStream(...)");
        if (in == null) {
            throw new IllegalArgumentException("in must not be null");
        }
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setFeature("http://xml.org/sax/features/namespaces", false);
        dbf.setFeature("http://xml.org/sax/features/validation", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder db = dbf.newDocumentBuilder();
        
        return db.parse(in);
    }
    
    /**
     * Parses an XML file into a DOM document.
     * 
     * @param f the file to parse
     * @return the DOM Document
     * @throws ParserConfigurationException something went wrong
     * @throws SAXException something went wrong
     * @throws IOException something went wrong
     */
    public static Document parseXmlFile(File f) throws ParserConfigurationException, SAXException, IOException {
        log.debug("parseXmlFile({})", f);
        if (f == null) {
            throw new IllegalArgumentException("f must not be null");
        }

        InputStream in = new FileInputStream(f);
        return parseXmlStream(in);
    }    
}
