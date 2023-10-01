/*
 */

package oolite.starter.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import oolite.starter.ExpansionManager;
import oolite.starter.model.Command;
import oolite.starter.model.Expansion;
import oolite.starter.ui.ExpansionReferenceCellRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Installs an Expansion.
 *
 * @author hiran
 */
public class InstallAction extends AbstractAction {
    private static final Logger log = LogManager.getLogger();

    private static final ImageIcon iiInstall = new ImageIcon(ExpansionReferenceCellRenderer.class.getResource("/icons/download_FILL0_wght400_GRAD0_opsz24.png"));
    
    private transient Expansion expansion;

    /**
     * Creates a new InstallAction.
     * 
     * @param expansion the expansion to install
     */
    public InstallAction(Expansion expansion) {
        super("Install", iiInstall);
        log.debug("InstallAction({})", expansion);
        this.expansion = expansion;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("actionPerformed({})", ae);
        Command command = new Command(Command.Action.INSTALL, expansion);
        ExpansionManager.getInstance().addCommand(command);
    }

}
