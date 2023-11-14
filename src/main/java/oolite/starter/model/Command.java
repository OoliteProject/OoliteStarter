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
        // INSTALL the expansion
        INSTALL, 
        // INSTALL the alternative expansion
        INSTALL_ALTERNATIVE, 
        // DELETE the expansion
        DELETE, 
        // ENABLE the expansion
        ENABLE, 
        // DISABLE the expansion
        DISABLE, 
        // we cannot resolve the expansion
        UNKNOWN,
        // we already have the expansion
        KEEP;
    }
    
    public enum Result {
        SUCCESS, FAILURE;
    }
    
    private Action action;
    private Expansion expansion;
    private Exception exception;

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
    
    /**
     * Returns this command's exception (in case it failed).
     * 
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }
    
    @Override
    protected Result doInBackground() throws Exception {
        log.debug("doInBackground()");
        
        try {
            switch (action) {
                case DELETE:
                    expansion.remove();
                    break;
                case DISABLE:
                    expansion.disable();
                    break;
                case ENABLE:
                    expansion.enable();
                    break;
                case INSTALL, INSTALL_ALTERNATIVE:
                    expansion.install();
                    break;
                case UNKNOWN:
                    throw new UnsupportedOperationException("Ran out of options");
                case KEEP:
                    // nothing to do
                    break;
                default:
                    throw new IllegalStateException(String.format("Unknown action %s", action));
            }
            
            return Result.SUCCESS;
            
        } catch (Exception e) {
            log.warn("Could not {} {}", action, expansion.getIdentifier(), e);
            this.exception = e;
            return Result.FAILURE;
        } finally {
            log.debug("doInBackground terminated");
        }
    }

    @Override
    public String toString() {
        return "Command{" + "action=" + action + ", expansion=" + expansion + ", status=" + getState() + '}';
    }

}
