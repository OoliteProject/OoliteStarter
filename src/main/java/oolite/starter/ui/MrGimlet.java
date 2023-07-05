/*
 */

package oolite.starter.ui;

import java.awt.Component;
import java.awt.Desktop;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to display tutor messages for the user.
 * 
 * @author hiran
 */
public class MrGimlet {
    private static final Logger log = LogManager.getLogger();

    /**
     * Prevent instances from being created.
     */
    private MrGimlet() {
        
    }
    
    /**
     * Shows a message in Mr Gimlet style.
     * 
     * @param parentComponent the parent window that should be blocked by this modal dialog
     * @param message The message to show
     */
    public static void showMessage(Component parentComponent, String message) {
        JEditorPane jep = new JEditorPane("text/html", message);
        jep.setEditable(false);
        jep.addHyperlinkListener(he-> {
            if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(he.getURL().toURI());
                } catch (Exception e) {
                    log.info("Could not open url {}", he.getURL(), e);
                }
            }
        });

        ImageIcon ii = null;
        try {
            ii = new ImageIcon(MrGimlet.class.getResource("/images/Mr_Gimlet.png"));
        } catch (Exception e) {
            log.warn("Could not load image", e);
        }
        JOptionPane.showMessageDialog(parentComponent, jep, "Message from Mr Gimlet", JOptionPane.INFORMATION_MESSAGE, ii);
    }
}
