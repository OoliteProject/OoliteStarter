/*
 */

package oolite.starter.model;

import javax.swing.SwingWorker;
import oolite.starter.model.Command.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A command to handle an expansion.
 * @author hiran
 */
public class Command extends SwingWorker<Result, Object> {
    private static final Logger log = LogManager.getLogger();

    public enum Action {
        // install the expansion
        install, 
        // delete the expansion
        delete, 
        // enable the expansion
        enable, 
        // disable the expansion
        disable, 
        // we cannot resolve the expansion
        unknown,
        // we alreadz have the expansion
        keep;
    }
    
    public enum Result {
        success, failure;
    }
    
    private Action action;
    private Expansion expansion;

    /**
     * Creates a new command.
     * 
     * @param action The action to execute
     * @param expansion The expansion to work on
     */
    public Command(Action action, Expansion expansion) {
        this.action = action;
        this.expansion = expansion;
    }

    /**
     * Returns this command's action.
     * 
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns this command's expansion.
     * 
     * @return the expansion
     */
    public Expansion getExpansion() {
        return expansion;
    }
    
    @Override
    protected Result doInBackground() throws Exception {
        log.debug("doInBackground()");
        
        try {
            switch (action) {
                case delete:
                    expansion.remove();
                    break;
                case disable:
                    expansion.disable();
                    break;
                case enable:
                    expansion.enable();
                    break;
                case install:
                    expansion.install();
                    break;
                case unknown:
                    throw new UnsupportedOperationException("Cannot handle unknown action");
                case keep:
                    // nothing to do
                    break;
                default:
                    throw new IllegalStateException(String.format("Unknown action %s", action));
            }
            
            return Result.success;
            
        } catch (Exception e) {
            log.error("Could not {}", action, e);
            return Result.failure;
        } finally {
            log.debug("doInBackground terminated");
        }
    }

    @Override
    public String toString() {
        return "Command{" + "action=" + action + ", expansion=" + expansion + ", status=" + getState() + '}';
    }

}
