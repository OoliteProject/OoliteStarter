/*
 */

package oolite.starter.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import oolite.starter.model.Expansion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ExpansionWorker extends SwingWorker<Object, Void> { // first is return object
    private static final Logger log = LogManager.getLogger();
    
    public enum Action {
        install,
        remove,
        enable,
        disable
    }
    
    private List<Expansion> expansions;
    
    private Action action;
    
    private JComponent component;
    
    /**
     * Creates a new InstallWorker thread.
     * 
     * @param expansion the expansion to install
     */
    public ExpansionWorker(Expansion expansion, Action action, JComponent component) {
        this.expansions = new ArrayList<>();
        expansions.add(expansion);
        this.action = action;
        this.component = component;
    }
    
    /**
     * Creates a new InstallWorker thread.
     * 
     * @param expansions the expansions to install
     */
    public ExpansionWorker(List<Expansion> expansions, Action action) {
        this.expansions = expansions;
        this.action = action;
    }

    @Override
    protected Object doInBackground() throws Exception {
        log.debug("doInBackground()");
        try {
            for (Expansion expansion: expansions) {
                log.debug("executing {} on {}:{} ...", action, expansion.getIdentifier(), expansion.getVersion());
                switch (action) {
                    case install:
                        expansion.install();
                        break;
                    case remove:
                        expansion.remove();
                        break;
                    case disable:
                        expansion.disable();
                        break;
                    case enable:
                        expansion.enable();
                        break;
                }
            }
            log.debug("doInBackground finished");
            return null;
        } catch (Exception e) {
            log.error("could not work", e);
            throw e;
        }
    }

    @Override
    protected void done() {
        log.debug("done()");
        //JOptionPane.showMessageDialog(component, "Finished " + expansions.size() + "");
        if (component instanceof ExpansionsPanel ep) {
            ep.update();
        }
    }

}
