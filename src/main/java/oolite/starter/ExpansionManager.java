/*
 */

package oolite.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import oolite.starter.model.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class ExpansionManager {
    private static final Logger log = LogManager.getLogger();
    
    public enum Status {
        Ready, Processing;
    }
    
    public interface ExpansionManagerListener {
        
        /**
         * Called when the ExpansionManager's status or queue changes.
         * 
         * @param status the status
         * @param queue the queue
         */
        public void updateStatus(Status status, Queue<Command> queue);
    }

    private BlockingQueue<Command> commands;
    private List<ExpansionManagerListener> listeners;
    private Status status;
    private Timer timer;
    private Command activeCommand;
            
    private static ExpansionManager instance;
    
    private ExpansionManager() {
        listeners = new ArrayList<>();
        commands = new LinkedBlockingDeque<>();
        status = Status.Ready;
        
        timer = new Timer(1000, (ae) -> {
            log.trace("ExpansionManager checking queue...");
            
            if (commands.isEmpty()) {
                if (status != status.Ready) {
                    status = Status.Ready;
                    fireUpdateStatus();
                }
            } else {
                synchronized(this) {
                    if (activeCommand == null) {
                        // triggerNext
                        activeCommand = commands.peek();
                        status = Status.Processing;
                        log.warn("Triggering {}", activeCommand);
                        activeCommand.execute();
                        fireUpdateStatus();
                    } else if (activeCommand.getState() != SwingWorker.StateValue.DONE) {
                        log.warn("think this is running: {}", activeCommand);
                    } else {
                        log.warn("command is finished! {}", activeCommand);
                        try {
                            // remove from queue
                            commands.take();
                        } catch (InterruptedException ex) {
                            log.warn("take from queue interrupted", ex);
                        }
                        activeCommand = null;
                    }
                }
            }
        });
        timer.start();
    }
    
    /**
     * Factory method that ensures we have only one instance.
     * 
     * @return the instance
     */
    public static synchronized ExpansionManager getInstance() {
        if (instance == null) {
            instance = new ExpansionManager();
        }
        
        return instance;
    }
    
    /**
     * Registers a listener to receive update events.
     * 
     * @param listener the listener
     */
    public void addExpansionManagerListener(ExpansionManagerListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Unregisters a listener from receiving update events.
     * 
     * @param listener the listener
     */
    public void removeExpansionManagerListener(ExpansionManagerListener listener) {
        listeners.remove(listener);
    }
    
    private void fireUpdateStatus() {
        for (ExpansionManagerListener listener: listeners) {
            listener.updateStatus(status, commands);
        }
    }
    
    /**
     * Adds a command to the queue.
     * 
     * @param command the command to add.
     */
    public void addCommand(Command command) {
        commands.add(command);
        // ensure we have a running thread
        fireUpdateStatus();
    }
    
    /**
     * Adds a list of commands to the queue.
     * 
     * @param commands the commands to add.
     */
    public void addCommands(List<Command> commands) {
        if (commands == null) {
            // nothing to do
            return; 
        }
        
        commands.addAll(commands);
        // ensure we have a running thread
        fireUpdateStatus();
    }
    
    /**
     * Returns the status of this ExpansionManager.
     * 
     * @return the status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Return a copy of the command queue.
     * 
     * @return the list of commands
     */
    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }
}
