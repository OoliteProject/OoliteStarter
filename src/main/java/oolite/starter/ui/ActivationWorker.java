/*
 */

package oolite.starter.ui;

import java.io.File;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import oolite.starter.Oolite;
import oolite.starter.model.Expansion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ActivationWorker extends SwingWorker<Object, Void> {
    private static final Logger log = LogManager.getLogger();
    
    private Oolite oolite;
    private File file;
    private List<Expansion> allExpansions;
    private JComponent component;
    
    /**
     * Creates a new ActivationWorker instance.
     * 
     * @param oolite the configured oolite worker
     * @param file the file to activate
     */
    public ActivationWorker(Oolite oolite, List<Expansion> allExpansions, File file, JComponent component) {
        this.file = file;
        this.oolite = oolite;
        this.allExpansions = allExpansions;
        this.component = component;
    }

    @Override
    protected Object doInBackground() throws Exception {
        ProgressMonitor pm = new ProgressMonitor(component, "activating " + file.getName(), "", 0, 10);
        try {
            oolite.setEnabledExpansions(file, allExpansions, pm);
            return null;
        } catch (Exception e) {
            log.error("doInBackground threw exception", e);
            throw e;
        } finally {
            pm.close();
        }
    }

    @Override
    protected void done() {
        log.info("done()");
        /*
         We are not fully decided: How is the user informed about ongoing background activity?
        JOptionPane.showMessageDialog(component, "Finished " + expansions.size() + "");
         */
        if (component instanceof ExpansionsPanel ep) {
            ep.update();
        }
    }

    
}
