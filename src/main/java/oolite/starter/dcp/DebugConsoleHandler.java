/*
 */
package oolite.starter.dcp;

import com.dd.plist.ASCIIPropertyListWriter;
import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.w3c.dom.Document;

/**
 *
 * @author hiran
 */
public class DebugConsoleHandler extends IoHandlerAdapter {
    private static final Logger log = LogManager.getLogger();
    
    public enum Status {
        passive, listening, connected, approved
    }
    
    public interface StatusListener {
        /**
         * This method gets fired on every status transition.
         * 
         * @param oldStatus the status we had until now
         * @param newStatus the status we have now
         */
        public void statusChanged(Status oldStatus, Status newStatus);
    }
    
    private List<PlistListener> listeners = new ArrayList<>();
    private List<IoSession> sessions = new ArrayList<>();
    private ConcurrentLinkedDeque<OoliteCommand> commandQueue;
    private Status status = Status.passive;
    private List<StatusListener> statusListener = new ArrayList<>();
    
    /** 
     * Creates a new DebugConsoleHandler.
     */
    public DebugConsoleHandler() {
        commandQueue = new ConcurrentLinkedDeque<>();
    }
    
    /**
     * Sends a command object to Oolite. The command is added to the queue of
     * commands.
     * 
     * @param command 
     */
    public void sendCommand(OoliteCommand command) {
        if (sessions.isEmpty()) {
            throw new IllegalStateException("No client connected");
        }
        
        commandQueue.add(command);
        sendCommand(command.getCommand());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        log.error("problem", cause);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        log.debug("messageReceived({}, {})", session, message);
        if (message instanceof Document) {
            Document doc = (Document)message;
            log.trace("DOM {}", Util.serialize(doc));
        }
        if(message instanceof NSObject) {
            log.trace("NSObject {}", ((NSObject)message).toXMLPropertyList());
        }
        
        if (message instanceof NSDictionary) {
            NSDictionary plist = (NSDictionary)message;
            
            String packetType = String.valueOf(plist.get("packet type"));
            String colorkey = String.valueOf(plist.get("color key"));
            
            switch(packetType) {
                case "Note Configuration":
                    fireConfiguration(plist.get("configuration"));
                    break;
                case "Console Output":
                    switch (colorkey) {
                        case "command-result":
                        case "dumpObject":
                            fireCommandResult(plist);
                            break;
                        case "command":
                            fireCommandAcknowledge(plist);
                            break;
                        case "exception":
                            fireCommandFailed(plist);
                            break;
                        case "log":
                            fireLogMessage(plist);
                            break;
                        case "Nexus":
                            fireWorldEvent(plist);
                            break;
                        default:
                            log.debug("we have NSDictionary packet_type={} colorkey={}", packetType, colorkey);
                            fireConsoleOutput(plist);
                    }
                    break;
                case "Close Connection":
                    log.info("Close Connection: {}", ((NSDictionary) message).get("message"));
                    setStatus(Status.listening);
                    break;
                case "Request Connection":
                    log.debug("Request Connection: {}", message);
                    // send back acknowledge connection
                    sendApproveConnection();
                    setStatus(Status.approved);
                    break;
                case "Show Console":
                    fireShowConsole();
                    break;
                default: {
                    File outFile = File.createTempFile("oolite", ".plist");
                    log.warn("We have Plist {} Yay! See {}", packetType, outFile);
                    ASCIIPropertyListWriter.writeGnuStep(plist, outFile);
                }
            }
        }
        if (message instanceof NSArray) {
            File outFile = File.createTempFile("oolite", ".plist");
            log.debug("We have Plist array. Yay! {}", outFile);
            NSArray plist = (NSArray)message;
            
            ASCIIPropertyListWriter.writeGnuStep(plist, outFile);
            
            log.debug("count: {}", plist.count());
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        // log.debug("{} IDLE {}", getContextInfo(session), session.getIdleCount(status));
    }

    private String getContextInfo(IoSession session) {
        return Thread.currentThread().getName() + " [" + session.getId() + "] ";
    }
    
    /**
     * Adds a PlistListener to the notification list.
     * 
     * @param listener the listener to add
     */
    public void addPlistListener(PlistListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    private void fireConfiguration(NSObject data) {
        for (PlistListener listener: listeners) {
            listener.receivedConfiguration(data);
        }
    }
    
    private void fireConsoleOutput(NSObject data) {
        for (PlistListener listener: listeners) {
            listener.receivedConsoleOutput(data);
        }
    }

    private void fireCommandResult(NSObject data) {
        // first check whether a command is waiting
        if (!commandQueue.isEmpty()) {
            for (OoliteCommand command: commandQueue) {
                log.debug(" queued: {}", command);
                if (OoliteCommand.Status.wait_result == command.getStatus()) {
                    command.setResult(data);
                    
                    // message is consumed, command is processed
                    commandQueue.remove(command);
                    return;
                }
                if (command.getStatus() == OoliteCommand.Status.failed) {
                    commandQueue.remove();
                    continue;
                }
            }
        }

        // then send to queue
        for (PlistListener listener: listeners) {
            listener.receivedCommandResult(data);
        }
    }
    
    private void fireCommandFailed(NSObject data) {
        // first check whether a command is waiting
        if (!commandQueue.isEmpty()) {
            for (OoliteCommand command: commandQueue) {
                log.debug(" queued: {}", command);
                if (OoliteCommand.Status.wait_result == command.getStatus()) {
                    command.setFailure(data);
                    
                    // message is consumed, command is processed
                    commandQueue.remove(command);
                    return;
                }
                if (command.getStatus() == OoliteCommand.Status.failed) {
                    commandQueue.remove();
                    continue;
                }
            }
        }

        // then send to queue
        for (PlistListener listener: listeners) {
            listener.receivedCommandResult(data);
        }
    }
    
    private void fireCommandAcknowledge(NSObject data) {
        // first check whether a command is waiting
        if (!commandQueue.isEmpty()) {
            for (OoliteCommand command: commandQueue) {
                log.debug(" queued: {}", command);
                if (OoliteCommand.Status.wait_ack == command.getStatus()) {
                    command.setAck(data);
                    
                    // message is consumed
                    return;
                }
                if (command.getStatus() == OoliteCommand.Status.failed) {
                    commandQueue.remove();
                    continue;
                }
            }
        }

        // then send to queue
        for (PlistListener listener: listeners) {
            listener.receivedCommandAcknowledge(data);
        }
    }

    private void fireLogMessage(NSObject data) {
        for (PlistListener listener: listeners) {
            listener.receivedLogMessage(data);
        }
    }

    private void fireWorldEvent(NSObject data) {
        for (PlistListener listener: listeners) {
            listener.receivedWorldEvent(data);
        }
    }
    
    private void fireShowConsole() {
        for (PlistListener listener: listeners) {
            listener.showConsole();
        }
    }
    
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session); //To change body of generated methods, choose Tools | Templates.
        if(!sessions.contains(session)) {
            sessions.add(session);
        }
        if (status != Status.approved) {
            setStatus(Status.connected);
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session); //To change body of generated methods, choose Tools | Templates.
        sessions.remove(session);
        setStatus(Status.listening);
    }
    
    /**
     * Sends an <tt>Approve Connection</tt> packet to Oolite.
     * Should be used after having received a <tt>Request Connection</tt> packet.
     */
    public void sendApproveConnection() {
        log.debug("sendApproveConnection()");
        NSDictionary plist = new NSDictionary();
        plist.put("packet type", new NSString("Approve Connection"));
        plist.put("console identity", new NSString(getClass().getPackage().getImplementationTitle() + getClass().getPackage().getImplementationVersion()));
        for (IoSession session: sessions) {
            session.write(plist);
        }
    }
    
    /**
     * Sends a command string to Oolite. This method returns immediately.
     * 
     * @param command the command to send
     */
    public void sendCommand(String command) {
        log.debug("sendcommand({})", command);
        
        NSDictionary plist = new NSDictionary();
        plist.put("packet type", new NSString("Perform Command"));
        plist.put("message", new NSString(command));
        
        for (IoSession session: sessions) {
            session.write(plist);
        }
        
        log.debug("Sent to {} sessions", sessions.size());
    }
    
    /**
     * Check whether we have (at least one) session.
     * 
     * @return true if there is at least one session
     */
    public boolean hasSession() {
        return !sessions.isEmpty();
    }
    
    private void setStatus(Status status) {
        if (this.status == status) {
            return;
        }
        
        Status oldStatus = this.status;
        this.status = status;
        fireStatusChanged(oldStatus, status);
    }
    
    private void fireStatusChanged(Status oldStatus, Status newStatus) {
        log.debug("fireStatusChanged({}, {})", oldStatus, newStatus);
        for (StatusListener listener: statusListener) {
            listener.statusChanged(oldStatus, newStatus);
        }
    }
    
    /**
     * Add a StatusListener to the notification list.
     * 
     * @param listener the listener to add
     */
    public void addStatusListener(StatusListener listener) {
        statusListener.add(listener);
    }
}
