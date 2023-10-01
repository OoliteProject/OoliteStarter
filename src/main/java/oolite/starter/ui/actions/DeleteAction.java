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
 *
 * @author hiran
 */
public class DeleteAction extends AbstractAction {
    private static final Logger log = LogManager.getLogger();

    private static final ImageIcon iiDelete = new ImageIcon(ExpansionReferenceCellRenderer.class.getResource("/icons/delete_forever_FILL0_wght400_GRAD0_opsz24.png"));
    
    private transient Expansion expansion;

    /**
     * Creates a new DeleteAction.
     * 
     * @param expansion the expansion to delete
     */
    public DeleteAction(Expansion expansion) {
        super("Delete", iiDelete);
        log.debug("DeleteAction({})", expansion);
        this.expansion = expansion;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        log.debug("actionPerformed({})", ae);
        Command command = new Command(Command.Action.DELETE, expansion);
        ExpansionManager.getInstance().addCommand(command);
    }
}
