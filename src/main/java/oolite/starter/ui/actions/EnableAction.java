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
 * Enables an expansion.
 * 
 * @author hiran
 */
public class EnableAction extends AbstractAction {
    private static final Logger log = LogManager.getLogger();

    private static final ImageIcon iiEnable = new ImageIcon(ExpansionReferenceCellRenderer.class.getResource("/icons/switches_enable_FILL0_wght400_GRAD0_opsz24.png"));
    
    private transient Expansion expansion;

    /**
     * Creates a new EnableAction.
     * 
     * @param expansion the expansion to enable
     */
    public EnableAction(Expansion expansion) {
        super("Enable", iiEnable);
        log.debug("EnableAction({})", expansion);
        this.expansion = expansion;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("actionPerformed({})", ae);
        Command command = new Command(Command.Action.ENABLE, expansion);
        ExpansionManager.getInstance().addCommand(command);
    }
    
    
}
