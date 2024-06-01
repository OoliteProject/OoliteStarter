/*
 */
package oolite.starter.dcp;

import java.util.ArrayList;

/**
 *
 * @author hiran
 */
public abstract class AbstractConnector implements Connector {
    
    private ArrayList<ConnectorStatusListener> listeners = new ArrayList<>();
    
    protected void fireStatusChanged(Status oldStatus, Status newStatus) {
        for (ConnectorStatusListener l: listeners) {
            l.statusChanged(this, oldStatus, newStatus);
        }
    }

    @Override
    public void addConnectorStatusListener(ConnectorStatusListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeConnectorStatusListener(ConnectorStatusListener listener) {
        listeners.remove(listener);
    }
    
}
