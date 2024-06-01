/*
 */
package oolite.starter.dcp;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A lot of Oolite commands do expect a response. Sometimes the respnose is not
 * immediate, yet our code is multithreaded and needs to synchronize on a connection
 * to be allowed sending a command and waiting for a response. Finally the response
 * has to be matched back to the command it was received for.
 * 
 * This command class shall resemble a command including the response. We can build
 * a queue of commands that gets worked off by an OoliteConnection. Thus the synchroniyation
 * and waiting happens inside tha OoliteConnection only.
 *
 * @author hiran
 */
public class OoliteCommand {
    private static final Logger log = LogManager.getLogger();
    
    private Duration timeout = Duration.of(1, ChronoUnit.SECONDS);
    
    public enum Status {
        created, wait_ack, wait_result, done,
        failed
    }

    private String command;
    private Status status;
    private NSObject ack;
    private NSObject result;

    /** Application creates a command.
     * 
     * @param command 
     */
    public OoliteCommand(String command) {
        log.debug("OoliteCommand({})", command);
        this.command = command;
        this.status = Status.created;
    }

    /** Return the command that was created.
     * 
     * @return 
     */
    public String getCommand() {
        log.debug("getCommand()");
        return command;
    }

    /** Return the status of the command execution.
     * 
     * @return 
     */
    public Status getStatus() {
        return status;
    }

    private void setStatus(Status status) {
        this.status = status;
    }

    /** Return the acknowledgement we received.
     * 
     * @return 
     */
    public NSObject getAck() {
        return ack;
    }

    /** TCPServer to set acknowledge message once it is received.
     * 
     * @param ack 
     */
    public void setAck(NSObject ack) {
        if (status != Status.wait_ack) {
            throw new IllegalStateException();
        }
        this.ack = ack;
        status = Status.wait_result;
    }
    
    /** 
     * Returns the Oolite response or null if not yet received.
     * 
     * @return the response
     */
    public NSObject getResultMessage() {
        if (result == null) {
            throw new IllegalStateException("cannot retrieve message for command '"+command+"' in status "+status);
        }
        return ((NSDictionary)result).get("message");
    }

    /** Application to get the result once it is provided.
     * 
     * @return 
     */
    public NSObject getResult() {
        return result;
    }

    /** TCPServer to set result message once it is received.
     * 
     * @param result
     */
    public void setResult(NSObject result) {
        if (status != Status.wait_result) {
            throw new IllegalStateException();
        }
        this.result = result;
        status = Status.done;
    }

    /** TCPServer to set result message once it is received.
     * 
     * @param result
     */
    public void setFailure(NSObject result) {
        if (status != Status.wait_result) {
            throw new IllegalStateException();
        }
        this.result = result;
        status = Status.failed;
    }

    /** Application to send the command via TCPServer.
     * 
     * @param server 
     */
    public void sendCommand(TCPServer server) {
        if (status != Status.created) {
            throw new IllegalStateException();
        }
        
        server.sendCommand(this);
        setStatus(Status.wait_ack);
    }
    
    /**
     * Sends a command and waits for the Oolite response or a timeout.
     * 
     * @param server the server used to send the command
     * @return Oolite's response
     */
    public NSObject sendCommandAndWait(TCPServer server) {
        sendCommand(server);
        
        Instant threshold = Instant.now().plus(timeout);
        
        while(status != OoliteCommand.Status.done && status != OoliteCommand.Status.failed) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                // empty on purpose
            }
            
            if (Instant.now().isAfter(threshold)) {
                status = Status.failed;
                break;
            }
        }
        
        return getResultMessage();
    }
    
    /**
     * Serializes this object to String.
     * 
     * @return the String
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("OoliteCommand(");
        sb.append("status=").append(status);
        sb.append(", command=").append(command);
        sb.append(")");
        return sb.toString();
    }
}
