/*
 */
package oolite.starter.dcp;

import java.util.Arrays;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Implements a SAX interface to parse PList documents.
 * 
 * @author hiran
 */
public class DomContentHandler implements ContentHandler {
    private static final Logger log = LogManager.getLogger();
    
    private DocumentBuilder db;
    private Document doc;
    private Node cursor;
    
    private Locator documentLocator;
    
    /**
     * Creates a new DomContentHandler.
     * 
     * @throws ParserConfigurationException something went wrong.
     */
    public DomContentHandler() throws ParserConfigurationException {
        log.debug("DomContentHandler()");
        db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    
    /**
     * Returns the parsed document.
     * 
     * @return the DOM node
     */
    public Document getDocument() {
        log.debug("getDocument()");
        return doc;
    }

    @Override
    public void startDocument() throws SAXException {
        log.debug("startDocument()");
        doc = db.newDocument();
        cursor = doc;
    }

    @Override
    public void endDocument() throws SAXException {
        log.debug("endDocument()");
    }

    @Override
    public void startElement(String uri, String localName, String qname, Attributes attributes) throws SAXException {
        log.debug("startElement({}, {}, {}, {})", uri, localName, qname, attributes);
        Element element = doc.createElement(localName);
        cursor.appendChild(element);
        cursor = element;
    }

    @Override
    public void endElement(String url, String localName, String qname) throws SAXException {
        log.debug("endElement({}, {}, {})", url, localName, qname);
        cursor = cursor.getParentNode();
    }

    @Override
    public void startPrefixMapping(String string, String string1) throws SAXException {
        log.info("startPrefixMapping(...)");
    }

    @Override
    public void endPrefixMapping(String string) throws SAXException {
        log.info("endPrefixMapping(...)");
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        log.debug("setDocumentLocator({})", locator);
        this.documentLocator = locator;
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        log.debug("characters({}, {}, {})", chars, start, length);
        String s = new String(Arrays.copyOfRange(chars, start, start+length));
        log.debug("characters: {}", s);
        Text text = doc.createTextNode(s);
        cursor.appendChild(text);
    }

    @Override
    public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
        log.info("ignorableWhitespace(...)");
    }

    @Override
    public void processingInstruction(String string, String string1) throws SAXException {
        log.info("processingInstruction(...)");
    }

    @Override
    public void skippedEntity(String string) throws SAXException {
        log.info("skippedEntity(...)");
    }
}
