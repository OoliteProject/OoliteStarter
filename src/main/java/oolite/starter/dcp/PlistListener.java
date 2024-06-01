/*
 */
package oolite.starter.dcp;

import com.dd.plist.NSObject;

/**
 *
 * @author hiran
 */
public interface PlistListener {

    /**
     * Invoked when a Configuration packet is received.
     * 
     * @param data the packet
     */
    public void receivedConfiguration(NSObject data);

    /**
     * Invoked when a ConsoleOutput packet is received.
     * 
     * @param data the packet
     */
    public void receivedConsoleOutput(NSObject data);
    
    /**
     * Invoked when a CommandResult packet is received.
     * 
     * @param data the packet
     */
    public void receivedCommandResult(NSObject data);
    
    /**
     * Invoked when a CommandAcknowledge packet is received.
     * 
     * @param data the packet
     */
    public void receivedCommandAcknowledge(NSObject data);
    
    /**
     * Invoked when a LogMessage packet is received.
     * 
     * @param data the packet
     */
    public void receivedLogMessage(NSObject data);
    
    /**
     * Invoked when a LogMessage packet from the Nexus OXP is received.
     * The Nexus OXP will forward all world events.
     * 
     * @param data the packet
     */
    public void receivedWorldEvent(NSObject data);
    
    /**
     * Invoked when a ShowConsole packet is received.
     * 
     * @param data the packet
     */
    public void showConsole();
}
