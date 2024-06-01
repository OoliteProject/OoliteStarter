/*
 */
package oolite.starter.dcp;

/**
 *
 * @author hiran
 */
public interface Connector {
    
    public static enum Status {
        passive,     // this connection is doing nothing
        connecting,
        connected,
        error,       // this connection is doing nothing after having error
        listening    // this connection is doing all it can do
    }
    
    /**
     * This interface informs about connection status updates.
     */
    public interface ConnectorStatusListener {
        
        /**
         * Called whenever the connection status changes.
         * 
         * @param connector the connector that changed
         * @param oldStatus the previous status
         * @param newStatus the new status
         */
        public void statusChanged(Connector connector, Status oldStatus, Status newStatus);
    }
    
    /** Return the current status for this connector.
     * 
     * @return 
     */
    public Status getStatus();

    /** Reset the connector and trigger a connect. This method may help when
     * connectors ran into error conditions and shall be nudged to reconnect,
     * especially with a fresh set of configuration.
     * 
     * As this method shall never block it is recommended to fire a background
     * thread to establish the connection.
     */
    public void nudge();
    
    /**
     * Adds a status listener to the list of listeners.
     * 
     * @param listener 
     */
    public void addConnectorStatusListener(ConnectorStatusListener listener);

    /**
     * Removes a status listener from the list of listeners.
     * 
     * @param listener 
     */
    public void removeConnectorStatusListener(ConnectorStatusListener listener);
}
