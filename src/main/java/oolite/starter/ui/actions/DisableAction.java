/*
 */

package oolite.starter.ui.actions;

import java.awt.event.ActionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import oolite.starter.ExpansionManager;
import oolite.starter.model.Command;
import oolite.starter.model.Expansion;
import oolite.starter.ui.ExpansionReferenceCellRenderer;

/**
 * Disables an Expansion.
 *
 * @author hiran
 */
public class DisableAction extends AbstractAction {
    private static final Logger log = LogManager.getLogger();

    private static final ImageIcon iiDisable = new ImageIcon(ExpansionReferenceCellRenderer.class.getResource("/icons/switches_disable_FILL0_wght400_GRAD0_opsz24.png"));
    
    private transient Expansion expansion;

    /**
     * Creates a new DisableAction.
     * 
     * @param expansion the expansion to disable
     */
    public DisableAction(Expansion expansion) {
        super("Disable", iiDisable);
        log.debug("DisableAction({})", expansion);
        this.expansion = expansion;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("actionPerformed({})", ae);
        Command command = new Command(Command.Action.DISABLE, expansion);
        ExpansionManager.getInstance().addCommand(command);                        
    }
}
