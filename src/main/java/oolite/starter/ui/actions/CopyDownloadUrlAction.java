/*
 */

package oolite.starter.ui.actions;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import oolite.starter.model.Expansion;
import oolite.starter.ui.ExpansionReferenceCellRenderer;
import oolite.starter.ui.MrGimlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Copies an expansion's download link to the system clipboard.
 *
 * @author hiran
 */
public class CopyDownloadUrlAction extends AbstractAction {
    private static final Logger log = LogManager.getLogger();

    private static final ImageIcon iiCopy = new ImageIcon(ExpansionReferenceCellRenderer.class.getResource("/icons/content_copy_FILL0_wght400_GRAD0_opsz24.png"));
    
    private transient Expansion expansion;
    private transient Component parent;
    
    /**
     * Creates a new CopyDownloadUrlAction.
     * 
     * @param expansion the expansion whose URL is to be copied
     * @param parent the parent component to give UI feedback on
     */
    public CopyDownloadUrlAction(Expansion expansion, Component parent) {
        super("Copy Download URL", iiCopy);
        log.debug("CopyDownloadUrlAction({}, {})", expansion, parent);
        this.expansion = expansion;
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("actionPerformed({})", ae);
        String s = expansion.getDownloadUrl();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (s != null) {
            StringSelection stringSelection = new StringSelection(s);
            clipboard.setContents(stringSelection, null);
            log.info("Download URL '{}' copied to clipboard", s);

            MrGimlet.showMessage(SwingUtilities.getRootPane(parent), "In your pocket!");
        } else {
            clipboard.setContents(new StringSelection(""), null);
            MrGimlet.showMessage(parent, "There is no URL to copy, son.");
        }
    }
}
