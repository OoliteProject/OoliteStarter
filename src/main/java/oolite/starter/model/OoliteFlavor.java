/*
 */

package oolite.starter.model;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

/**
 * Represents a flavor for Oolite that references an ExpansionSet.
 * 
 * @author hiran
 */
public class OoliteFlavor {
    private static final Logger log = LogManager.getLogger();
    
    private String name;
    private String description;
    private URL imageUrl;
    private URL expansionSetUrl;

    /**
     * Creates a new OoliteFlavor.
     * 
     * @param name the name of the flavor
     * @param description the decription of the flavor
     * @param expansionSetUrl the URL to the expansion set file
     */
    public OoliteFlavor(String name, String description, URL imageUrl, URL expansionSetUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.expansionSetUrl = expansionSetUrl;
    }

    /**
     * Returns the name of the flavor.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the flavor.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the URL for the image.
     * 
     * @return the url
     */
    public URL getImageUrl() {
        return imageUrl;
    }

    /**
     * Returns the URL to the expansion set for the flavor.
     * 
     * @return the expansion set url
     */
    public URL getExpansionSetUrl() {
        return expansionSetUrl;
    }

    @Override
    public String toString() {
        return "OoliteFlavor{" + "name=" + name + '}';
    }

    /**
     * Loads a flavor from DOM element.
     * 
     * <pre>&gt;?xml version="1.0"?&lt;
     * &gt;flavors&lt;
     *     &gt;flavor&lt;
     *         &gt;name&lt;Vanilla&gt;/name&lt;
     *         &gt;description&lt;Play Oolite as close as possible to th original Elite.&gt;/description&lt;
     *         &gt;icon&lt;/i/i/extension_FILL0_wght400_GRAD0_opsz48.png&gt;/icon&lt;
     *         &gt;expansion-set&lt;Vanilla.oolite-es&gt;/expansion-set&lt;
     *     &gt;/flavor&lt;
     *     ...
     * &gt;/flavors&lt;</pre>
      * 
     * @param baseUrl the url where this flavor was downloaded from
     * @param element the element to read
     * @return the read flavor
     */
    public static OoliteFlavor buildFrom(URL baseUrl, Element element) throws XPathExpressionException, MalformedURLException {
        log.debug("buildFrom({})", element);
        XPath xpath = XPathFactory.newInstance().newXPath();
        String name = xpath.evaluate("name", element);
        String description = xpath.evaluate("description", element);
        String imgUrl = xpath.evaluate("icon", element);
        String esUrl = xpath.evaluate("expansion-set", element);
        
        return new OoliteFlavor(name, description, new URL(baseUrl, imgUrl), new URL(baseUrl, esUrl));
    }
}
