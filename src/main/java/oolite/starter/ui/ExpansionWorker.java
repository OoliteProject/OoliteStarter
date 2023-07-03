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
        INSTALL,
        REMOVE,
        ENABLE,
        DISABLE
    }
    
    private List<Expansion> expansions;
    
    private Action action;
    
    private JComponent component;
    
    /**
     * Creates a new InstallWorker thread.
     * 
     * @param expansion the expansion to INSTALL
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
     * @param expansions the expansions to INSTALL
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
                    case INSTALL:
                        expansion.install();
                        break;
                    case REMOVE:
                        expansion.remove();
                        break;
                    case DISABLE:
                        expansion.disable();
                        break;
                    case ENABLE:
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
        
        /*
        TODO: We are not fully decided: How is the user informed about ongoing background activity?
        JOptionPane to showMessageDialog(component, "Finished " + expansions.size() + "");
         */
        if (component instanceof ExpansionsPanel ep) {
            ep.update();
        }
    }

}
