/*
 */
package oolite.starter.ui;

import java.awt.Desktop;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Listenr that can action clicks to hyperlinks when attached to JEditorPane.
 *
 * @author hiran
 */
public class HyperLinkListener implements HyperlinkListener {
    private static final Logger log = LogManager.getLogger();

    @Override
    public void hyperlinkUpdate(HyperlinkEvent he) {
        if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                Desktop.getDesktop().browse(he.getURL().toURI());
            } catch (Exception e) {
                log.info("Could not open url {}", he.getURL(), e);
            }
        }
    }
    
}
