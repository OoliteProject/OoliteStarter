/*
 */

package oolite.starter.model;

import java.io.File;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Transports data about a launched process within the application.
 *
 * @author hiran
 */
public class ProcessData {
    private static final Logger log = LogManager.getLogger();

    /**
     * The current working directory.
     */
    private File cwd;
    
    /**
     * The command line launched.
     */
    private List<String> command;
    
    /**
     * The pid of the process.
     */
    private long pid;

    /**
     * Creates a new ProcessData instance.
     * 
     * @param cwd the current working directory
     * @param command the command line
     * @param pid the pid
     */
    public ProcessData(File cwd, List<String> command, long pid) {
        log.debug("ProcessData({}, {}, {})", cwd, command, pid);
        
        this.cwd = cwd;
        this.command = command;
        this.pid = pid;
    }

    /**
     * Returns the process working directory.
     * 
     * @return the directory
     */
    public File getCwd() {
        return cwd;
    }

    /**
     * Returns the process pid.
     * 
     * @return the pid
     */
    public long getPid() {
        return pid;
    }

    /**
     * Returns the command launched.
     * 
     * @return the command
     */
    public List<String> getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "ProcessData{" + "cwd=" + cwd + ", command=" + command + ", pid=" + pid + '}';
    }
    
}
