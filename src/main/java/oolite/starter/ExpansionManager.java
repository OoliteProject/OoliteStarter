/*
 */

package oolite.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
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
    
    public enum Activity {
        PROCESSING, IDLE, ERRORS;
    }
    
    public record Status (int queueSize, int processing, int failed, Activity activity) {
    }
    
    public interface ExpansionManagerListener {
        
        /**
         * Called when the ExpansionManager's status or queue changes.
         * 
         * @param status the status
         * @param queue the queue
         */
        public void updateStatus(Status status, List<Command> queue);
    }

    private List<Command> commands;
    private List<ExpansionManagerListener> listeners;
    private Activity activity;
    private Timer timer;
    private int parallelThreads = 10;
            
    private static ExpansionManager instance;
    
    private ExpansionManager() {
        listeners = new ArrayList<>();
        commands = new ArrayList<>();
        activity = Activity.IDLE;
        
        timer = new Timer(1000, (ae) -> {
            log.trace("ExpansionManager checking queue...");
            
            if (commands.isEmpty()) {
                if (activity != Activity.IDLE) {
                    activity = Activity.IDLE;
                    fireUpdateStatus(new Status(0, 0, 0, activity));
                }
            } else {
                int commandCount0 = commands.size();
                if (getFailedCount() == commandCount0) {
                    if (activity != Activity.ERRORS) {
                        activity = Activity.ERRORS;
                        fireUpdateStatus(new Status(0, 0, 0, activity));
                    }
                } else {
                    synchronized(this) {
                        // remove done commands
                        List<Command> done = commands
                                .stream()
                                .filter((t) -> t.getState() == SwingWorker.StateValue.DONE)
                                .filter((t) -> {
                                    try {
                                        return t.get() == Command.Result.SUCCESS;
                                    } catch (InterruptedException | ExecutionException e) {
                                        Thread.currentThread().interrupt();
                                        return false;
                                    }
                                })
                                .collect(Collectors.toList());
                        commands.removeAll(done);

                        // trigger up to parallelTreads
                        commands
                                .stream()
                                //.filter(t -> t.getAction() != Command.Action.UNKNOWN)
                                .filter(t -> t.getState() != SwingWorker.StateValue.DONE)
                                .limit(parallelThreads)
                                .filter(t -> t.getState() == SwingWorker.StateValue.PENDING)
                                .forEach((t) -> {
                                    t.execute();
                                });
                    }
                    int commandCount1 = commands.size();
                    if (activity == activity.IDLE || (commandCount1 - commandCount0) != 0) {
                        activity = Activity.PROCESSING;
                        fireUpdateStatus();
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
        long processing = commands
                .stream()
                .filter((t) -> t.getState() == SwingWorker.StateValue.STARTED)
                .count();
        long failed = commands
                .stream()
                .filter((t) -> {
                    try {
                        return t.getState() == SwingWorker.StateValue.DONE && t.get() == Command.Result.FAILURE;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

        fireUpdateStatus(getStatus());
    }
    
    private void fireUpdateStatus(Status status) {
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
        
        for (Command c: commands) {
            if (c.getAction() != Command.Action.KEEP) {
                this.commands.add(c);
            }
        }
        
        // ensure we have a running thread
        fireUpdateStatus();
    }
    
    private long getFailedCount() {
        return commands.stream()
            .filter((t) -> {
                try {
                    return t.getState() == SwingWorker.StateValue.DONE && t.get() == Command.Result.FAILURE;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                } catch (Exception e) {
                    return false;
                }
            })
            .count();
    }
    
    /**
     * Returns the status of this ExpansionManager.
     * 
     * @return the status
     */
    public Status getStatus() {
        long processing = commands
                .stream()
                .filter((t) -> t.getState() == SwingWorker.StateValue.STARTED)
                .count();
        long failed = getFailedCount();

        return new Status(commands.size(), (int)processing, (int)failed, activity);
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
