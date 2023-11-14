/*
 */

package oolite.starter.ui.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import oolite.starter.model.Expansion;
import oolite.starter.ui.ExpansionReferenceCellRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Opens a file system browser for a specific expansion.
 *
 * @author hiran
 */
public class ShowInFilesystemAction extends AbstractAction {
    private static final Logger log = LogManager.getLogger();

    private static final ImageIcon iiBrowse = new ImageIcon(ExpansionReferenceCellRenderer.class.getResource("/icons/folder_open_FILL0_wght400_GRAD0_opsz24.png"));
    
    private transient Expansion expansion;
 
    /**
     * Creates a new ShowInFilesystemAction.
     * 
     * @param expansion the expansion to show
     */
    public ShowInFilesystemAction(Expansion expansion) {
        super("Show in FileSystem", iiBrowse);
        log.debug("ShowInFilesystemAction({})", expansion);
        this.expansion = expansion;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("actionPerformed({})", ae);
        try {
            Desktop.getDesktop().browseFileDirectory(expansion.getLocalFile());
            return;
        } catch (UnsupportedOperationException e) {
            log.warn("Could not open file", e);
        }
        try {
            Desktop.getDesktop().open(expansion.getLocalFile());
        } catch (Exception e) {
            log.warn("Could not open file", e);
        }
    }

}
