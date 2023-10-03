/*
 */

package oolite.starter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.positioners.RightBelowPositioner;
import net.java.balloontip.styles.EdgedBalloonStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to display tutor messages for the user.
 * 
 * @author hiran
 */
public class MrGimlet {
    private static final Logger log = LogManager.getLogger();

    private static ImageIcon ii = null;
    
    static {
        try {
            ii = new ImageIcon(MrGimlet.class.getResource("/images/Mr_Gimlet.png"));
            int oldwidth = ii.getIconWidth();
            int oldheight = ii.getIconHeight();
            ii = new ImageIcon(ii.getImage().getScaledInstance(oldwidth/2, oldheight/2, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            log.warn("Could not load image", e);
        }
    }
        
    
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
        showMessage(parentComponent, message, 4000);
    }
    
    /**
     * Shows a message in Mr Gimlet style.
     * 
     * @param parentComponent the parent window that should be blocked by this modal dialog
     * @param message The message to show
     */
    public static void showMessage(Component parentComponent, String message, int fadeMillis) {
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
        jep.setBackground(new Color(0,0,0, 0));

        if (parentComponent instanceof JComponent jc) {
            JRootPane rootpane = SwingUtilities.getRootPane(parentComponent);
            final JPanel glasspane = (JPanel)rootpane.getGlassPane();
            if (!(glasspane.getLayout() instanceof GridBagLayout)) {
                glasspane.setLayout(new GridBagLayout());
            }
            
            JPanel payload = new JPanel();
            payload.add(new JLabel(ii));
            payload.add(jep);
            payload.setBackground(new Color(0,0,0, 0));
            
            final BalloonTip bt = new BalloonTip(jc, payload, new EdgedBalloonStyle(new Color(20, 20, 50, 210), Color.lightGray), true);
            bt.setPositioner(new RightBelowPositioner(0, 0));
            
            glasspane.add(bt, new GridBagConstraints(1, glasspane.getComponentCount(), 1, 1, 0, 1, GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(5,5,5,5), 0, 0));
            glasspane.setVisible(true);

            if (fadeMillis > 0) {
                Util.fadeOutBalloon(bt, ae -> {
                    log.debug("actionPerformed(...)");
                    SwingUtilities.invokeLater(() -> {
                        glasspane.remove(bt);
                        glasspane.validate();
                        glasspane.repaint();
                    });
                }, fadeMillis, 30);
            }
        } else {
            JOptionPane.showMessageDialog(parentComponent, jep, "Message from Mr Gimlet", JOptionPane.INFORMATION_MESSAGE, ii);
        }
    }
    
    
    /**
     * Shows a confirmation (yes/no/cancel) in Mr Gimlet style.
     * 
     * @param parentComponent the parent window that should be blocked by this modal dialog
     * @param message The message to show
     */
    public static int showConfirmation(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message);
    }
}
