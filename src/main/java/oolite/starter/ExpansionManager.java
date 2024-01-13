/*
 */

package oolite.starter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
        /**
         * The expansion manager is processing the queue.
         */
        PROCESSING, 
        
        /**
         * The expansion manager is idle - the queue is empty.
         */
        IDLE, 
        
        /**
         * The expansion manager encountered errors.
         */
        ERRORS, 
        
        /**
         * The expansion manager is stopped.
         */
        STOPPED;
    }
    
    /**
     * A type holding the expansion manager's status.
     */
    public static record Status (int queueSize, int processing, int failed, Activity activity) {
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

    /**
     * The queue of commands to process.
     */
    private List<Command> commands;
    
    /**
     * List of listeners that have subscribed to expansion manager events.
     */
    private List<ExpansionManagerListener> listeners;
    
    /**
     * The activity status of this expansion manager.
     */
    private Activity activity;
    
    /**
     * The timer that makes the expansion manager regularly check it's threadpool vs the command queue.
     */
    private Timer timer;
    
    /**
     * Amount of threads to use for processing the command queue.
     */
    private int parallelThreads = 10;
            
    private static class SingletonHelper {
        
        /**
         * The one single instance.
         */
        private static ExpansionManager instance = new ExpansionManager();
    }
    
    private class EMAL implements ActionListener {
        
        private void triggerWorkers() {
            synchronized(this) {
                // remove done commands
                List<Command> done = commands
                        .stream()
                        .filter(t -> t.getState() == SwingWorker.StateValue.DONE)
                        .filter(t -> {
                            try {
                                return t.get() == Command.Result.SUCCESS;
                            } catch (InterruptedException | ExecutionException e) {
                                Thread.currentThread().interrupt();
                                return false;
                            }
                        })
                        .toList();
                commands.removeAll(done);

                // trigger up to parallelTreads
                commands
                        .stream()
                        //.filter(t -> t.getAction() != Command.Action.UNKNOWN)
                        .filter(t -> t.getState() != SwingWorker.StateValue.DONE)
                        .limit(parallelThreads)
                        .filter(t -> t.getState() == SwingWorker.StateValue.PENDING)
                        .forEach(t -> t.execute() );
            }
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
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
                    triggerWorkers();
                    int commandCount1 = commands.size();
                    if (activity == Activity.IDLE || (commandCount1 - commandCount0) != 0) {
                        activity = Activity.PROCESSING;
                        fireUpdateStatus();
                    }
                }
            }
        }
        
    }
    
    private ExpansionManager() {
        listeners = new ArrayList<>();
        commands = new ArrayList<>();
        activity = Activity.STOPPED;
        
        timer = new Timer(1000, new EMAL());
    }
    
    /**
     * Resets the ExpansionManager. Old errors or running state will be
     * forgotten. This method mainly supports unit testing.
     */
    public void reset() {
        timer.stop();
        commands = new ArrayList<>();
        activity = Activity.STOPPED;
    }
    
    /**
     * Factory method that ensures we have only one instance.
     * 
     * @return the instance
     */
    public static ExpansionManager getInstance() {
        return SingletonHelper.instance;
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
            .filter(t -> {
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
                .filter(t -> t.getState() == SwingWorker.StateValue.STARTED)
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
    
    /**
     * Starts the background thread for ExpansionManager.
     */
    public void start() {
        timer.start();
    }

    
    /**
     * Stops the background thread for ExpansionManager.
     */
    public void stop() {
        timer.stop();
    }
}
