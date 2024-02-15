/*
 */

package oolite.starter.ui2;

import java.awt.Component;
import javax.swing.SwingWorker;
import oolite.starter.Oolite2;
import oolite.starter.model.Expansion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class DeleteSwingWorker extends SwingWorker<Void, Void> {
    private static final Logger log = LogManager.getLogger();
    
    private Expansion expansion;
    //private Oolite2.OoliteExpansionListModel a;
    private Oolite2 oolite;
    
    /**
     * Creates a new DeleteSwingWorker.
     * 
     * @param expansion the expansion to delete
     */
    public DeleteSwingWorker(Component parent, Expansion expansion, Oolite2 oolite) {
        log.debug("DeleteSwingWorker(...)");
        this.expansion = expansion;
        this.oolite = oolite;
    }

    @Override
    protected Void doInBackground() throws Exception {
        log.debug("doInBackground()");
        
        expansion.remove();
        return null;
    }

    @Override
    protected void done() {
        log.debug("done()");
        
        oolite.fire();
    }

}
