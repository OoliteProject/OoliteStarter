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
        install, delete, enable, disable;
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
    
    @Override
    protected Result doInBackground() throws Exception {
        log.warn("doInBackground()");
        
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
            default:
                throw new IllegalStateException(String.format("Unknown action %s", action));
        }
        
        log.warn("doInBackground terminated");
        return Result.success;
    }

    @Override
    public String toString() {
        return "Command{" + "action=" + action + ", expansion=" + expansion + ", status=" + getState() + '}';
    }

}
