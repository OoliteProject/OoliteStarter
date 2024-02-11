/*
 */

package oolite.starter.ui2;

import java.awt.Component;
import javax.swing.SwingWorker;
import oolite.starter.model.Expansion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class InstallSwingWorker extends SwingWorker<Void, Void> {
    private static final Logger log = LogManager.getLogger();
    
    private Expansion expansion;
    private ExpansionListModel a;
    private ExpansionListModel b;
    
    /**
     * Creates a new InstallSwingWorker.
     * 
     * @param expansion the expansion to install
     */
    public InstallSwingWorker(Component parent, Expansion expansion, ExpansionListModel a, ExpansionListModel b) {
        log.debug("InstallSwingWorker(...)");
        this.expansion = expansion;
        this.a = a;
        this.b = b;
    }

    @Override
    protected Void doInBackground() throws Exception {
        log.debug("doInBackground()");
        
        expansion.install();
        return null;
    }

    @Override
    protected void done() {
        log.debug("done()");
        a.remove(expansion);
        b.add(expansion);
    }

}
