/*
 */
package oolite.starter.dcp;

import com.dd.plist.NSObject;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author hiran
 */
public class OoliteConnector extends AbstractConnector implements Connector, PlistListener {
    private static final Logger log = LogManager.getLogger();

    public static interface OoliteStatusEventListener {
        
        /**
         * Invoked whenever the status of the given connector changes.
         * 
         * @param connector the connector changing it's status
         */
        public void statusUpdated(OoliteConnector connector);
        
    }
    
    private TCPServer tcpserver;
    private Thread statusPoller;
    private int checkOXPsDivider;
    private ArrayList<OoliteStatusEventListener> statusEventListeners = new ArrayList<>();
    private ArrayList<PlistListener> pListListeners = new ArrayList<>();
    
    private String commander;
    private String shipName;
    private String system;
    private String galaxy;
    // http://wiki.alioth.net/index.php/Oolite_JavaScript_Reference:_Player#alertCondition
    private String alertCondition;
    private String station;
    private String cargoSpaceAvailable;
    private String cargoSpaceCapacity;

    private String position;
    private String orientation;
    private String velocity;
    private String roll;
    private String pitch;
    private String yaw;
    
    /**
     * Creates a new OoliteConnector.
     */
    public OoliteConnector() {
        log.debug("OoliteConnector()");
        
        statusPoller = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Thread.interrupted()) {
                        break;
                    }
                        
                    try {
                        boolean changed = false;
                        if (tcpserver != null && TCPServer.Status.connected == tcpserver.getStatus()) {
                            log.debug("Polling Oolite...");
                            if(checkOXPsDivider <= 0) {
                                checkOXPsDivider = 150;
                                try {
                                    // http://aegidian.org/bb/viewtopic.php?p=277378#p277378
                                    String resourcePaths = pollString("oolite.resourcePaths");
                                    log.debug("oolite.resourcePaths={}", resourcePaths);
                                } catch(IllegalStateException e) {
                                    log.error("Could not check installed OXPs", e);
                                }
                            } else {
                                checkOXPsDivider--;
                            }
                            try {
                                String newPosition = pollString("player.ship.position");
                                if (!newPosition.equals(position)) {
                                    position = newPosition;
                                    changed |= true;
                                }
                            } catch(IllegalStateException e) {
                                log.error("Could not retrieve position", e);
                            }
                            try {
                                String newOrientation = pollString("player.ship.orientation");
                                if (!newOrientation.equals(orientation)) {
                                    orientation = newOrientation;
                                    changed |= true;
                                }
                            } catch(IllegalStateException e) {
                                log.error("Could not retrieve orientation", e);
                            }
                            try {
                                String newPlayerName = pollString("player.name");
                                if (!newPlayerName.equals(commander)) {
                                    commander = newPlayerName;
                                    changed |= true;
                                }
                            } catch(IllegalStateException e) {
                                log.error("Could not retrieve player name", e);
                            }
                            try {
                                String newShipName = pollString("player.ship.shipUniqueName");
                                if (!newShipName.equals(shipName)) {
                                    shipName = newShipName;
                                    changed |= true;
                                }
                            } catch(IllegalStateException e) {
                                log.error("Could not retrieve ship name", e);
                            }
                            try {
                                String newSystem = pollString("system.name");
                                if (!newSystem.equals(system)) {
                                    system = newSystem;
                                    changed |= true;
                                }
                            } catch(IllegalStateException e) {
                                log.error("Could not retrieve system name", e);
                            }
                            try {
                                String newGalaxy = pollString("galaxyNumber");
                                if (!newGalaxy.equals(galaxy)) {
                                    galaxy = newGalaxy;
                                    changed |= true;
                                }
                            } catch(IllegalStateException e) {
                                log.error("Could not retrieve galaxy", e);
                            }
                            try {
                                // http://wiki.alioth.net/index.php/Oolite_JavaScript_Reference:_Player#alertCondition
                                String newAlertConditionString = pollString("player.alertCondition");
                                if (!newAlertConditionString.equals(alertCondition)) {
                                    alertCondition = newAlertConditionString;
                                    changed |= true;
                                }
                            } catch(IllegalStateException e) {
                                log.error("Could not retrieve alert condition", e);
                            }
                            if ("0".equals(alertCondition)) {
                                try {
                                    String newStation = pollString("player.ship.dockedStation.name");
                                    if (!newStation.equals(station)) {
                                        station = newStation;
                                        changed |= true;
                                    }
                                } catch(IllegalStateException e) {
                                    log.error("Could not retrieve docked station", e);
                                }
                            } else {
                                if (station != null) {
                                    station = null;
                                    changed |= true;
                                }
                            }
                            try {
                                String newCargoSpaceAvailable = pollString("player.ship.cargoSpaceAvailable");
                                if (!newCargoSpaceAvailable.equals(cargoSpaceAvailable)) {
                                    cargoSpaceAvailable = newCargoSpaceAvailable;
                                    changed |= true;
                                }
                            } catch(IllegalStateException e) {
                                log.error("Could not retrieve cargoSpaceAvailable", e);
                            }
                            try {
                                String newCargoSpaceCapacity = pollString("player.ship.cargoSpaceCapacity");
                                if (!newCargoSpaceCapacity.equals(cargoSpaceCapacity)) {
                                    cargoSpaceCapacity = newCargoSpaceCapacity;
                                    changed |= true;
                                }
                            } catch(IllegalStateException e) {
                                log.error("Could not retrieve cargoSpaceCapacity", e);
                            }
                        }
                        
                        if (changed) {
                            log.debug("informing observers");
                            for (OoliteStatusEventListener listener: statusEventListeners) {
                                listener.statusUpdated(OoliteConnector.this);
                            }
                        }
                        Thread.sleep(20);
                    } catch (Throwable t) {
                        log.info("Could not poll", t);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                }
            }
        }, "OolitePoller");
        
        statusPoller.start();
        log.debug("statusPoller thread started");
    }

    /** Returns the current TCP server.
     * 
     * @return the current TCP server
     */
    public TCPServer getTcpserver() {
        return tcpserver;
    }

    /**
     * Set a new TCP server to be used.
     * 
     * @param tcpserver the new TCP server
     */
    public void setTcpserver(TCPServer tcpserver) {
        this.tcpserver = tcpserver;
        tcpserver.addPlistListenerListener(this);
    }

    @Override
    public Status getStatus() {
        return tcpserver.getStatus();
    }

    /**
     * Nudge the TCP server to listen for incoming connections.
     */
    @Override
    public void nudge() {
        tcpserver.nudge();
    }

    /**
     * Shutdown all TCP connections and the TCP listener.
     */
    public void shutdown() {
        tcpserver.shutdown();
    }
    
    /**
     * Add OoliteStatusEventListener to notification list.
     * 
     * @param listener the listener to add
     */
    public void addOoliteStatusEventListener(OoliteStatusEventListener listener) {
        statusEventListeners.add(listener);
    }
    
    /**
     * Add PlistListener to notification list.
     * 
     * @param listener the listener to add
     */
    public void addPlistListener(PlistListener listener) {
        pListListeners.add(listener);
    }
    
    /**
     * Retrieves a simple object from Oolite and returns it's structure.
     * This method will internally invoke the Debug Console Macro
     * dumpObject, so make sure to specify the blank object.
     * 
     * @param expression the object specifier
     * @return the simple structure: a string
     */
    public String pollString(String expression) {
        log.debug("pollString({})", expression);

        synchronized (this) {
            String result = String.valueOf(new OoliteCommand("dumpObject(eval(\""+expression+"\"))").sendCommandAndWait(tcpserver));
            return result;
        }
    }

    /**
     * Retrieves a complex object from Oolite and returns it's structure.
     * This method will internally invoke the Debug Console Macro
     * dumpObject, so make sure to specify the blank object.
     * 
     * @param expression the object specifier
     * @return the complex structure
     */
    public NSObject pollObject(String expression) {
        log.debug("pollObject({})", expression);
        
        synchronized (this) {
            return new OoliteCommand("dumpObject(eval(\""+expression+"\"))").sendCommandAndWait(tcpserver);
        }
    }
    
    /** 
     * Sends a command to Oolite and returns the response.
     * 
     * @param expression the command to send
     * @return the response
     */
    public NSObject sendCommand(String expression) {
        log.debug("sendCommand({})", expression);
        
        synchronized (this) {
            NSObject result = new OoliteCommand(expression).sendCommandAndWait(tcpserver);
            return result;
        }
    }

    /**
     * Return the position of the ship.
     * 
     * @return the position
     */
    public String getPosition() {
        return position;
    }

    /**
     * Return the orientation of the ship.
     * 
     * @return the orientation
     */
    public String getOrientation() {
        return orientation;
    }

    /**
     * Return the velocity of the ship.
     * 
     * @return the velocity
     */
    public String getVelocity() {
        return velocity;
    }

    /**
     * Return the roll of the ship.
     * 
     * @return the roll
     */
    public String getRoll() {
        return roll;
    }

    /**
     * Return the pitch of the ship.
     * 
     * @return the pitch
     */
    public String getPitch() {
        return pitch;
    }

    /**
     * Return the yaw of the ship.
     * 
     * @return the yaw
     */
    public String getYaw() {
        return yaw;
    }

    /**
     * Returns the cached value of the commander name.
     * 
     * @return the commander name
     */
    public String getCommander() {
        return commander;
    }
    
    /**
     * Returns the cached value of the ship name.
     * 
     * @return the ship name
     */
    public String getShipName() {
        return shipName;
    }

    /**
     * Returns the cached value of the system.
     * 
     * @return the system
     */
    public String getSystem() {
        return system;
    }

    /**
     * Returns the cached value of the galaxy.
     * 
     * @return the galaxy
     */
    public String getGalaxy() {
        return galaxy;
    }

    /**
     * Returns the cached value of the alert condition.
     * 
     * @return the alert condition
     */
    public String getAlertCondition() {
        return alertCondition;
    }

    /**
     * Returns the cached value of the currently docked station.
     * 
     * @return the station as string
     */
    public String getStation() {
        return station;
    }
    
    /**
     * Retrieves the ship manifest from Oolite and returns it.
     * 
     * @return the manifest
     */
    public NSObject getShipManifest() {
        return pollObject("player.ship.manifest");
    }

    /**
     * Returns the cached value of the cargo space available.
     * 
     * @return the cargo space available as string
     */
    public String getCargoSpaceAvailable() {
        return cargoSpaceAvailable;
    }

    /**
     * Returns the cached value of the cargo space capacity.
     * 
     * @return the cargo space capacity as string
     */
    public String getCargoSpaceCapacity() {
        return cargoSpaceCapacity;
    }

    @Override
    public void receivedConfiguration(NSObject data) {
        if (pListListeners.isEmpty()) {
            log.debug("ReceivedConfiguration({})", data);
        }
        for (PlistListener listener: pListListeners) {
            listener.receivedConfiguration(data);
        }
    }

    @Override
    public void receivedConsoleOutput(NSObject data) {
        if (pListListeners.isEmpty()) {
            log.debug("receivedConsoleOutput({})", data);
        }
        for (PlistListener listener: pListListeners) {
            listener.receivedConsoleOutput(data);
        }
    }

    @Override
    public void receivedCommandResult(NSObject data) {
        if (pListListeners.isEmpty()) {
            log.debug("receivedCommandResult({})", data);
        }
        for (PlistListener listener: pListListeners) {
            listener.receivedCommandResult(data);
        }
    }

    @Override
    public void receivedCommandAcknowledge(NSObject data) {
        if (pListListeners.isEmpty()) {
            log.debug("receivedCommandAcknowledge({})", data);
        }
        for (PlistListener listener: pListListeners) {
            listener.receivedCommandAcknowledge(data);
        }
    }

    @Override
    public void receivedLogMessage(NSObject data) {
        if (pListListeners.isEmpty()) {
            log.info("receivedLogMessage({})", data);
        }
        for (PlistListener listener: pListListeners) {
            listener.receivedLogMessage(data);
        }
    }

    @Override
    public void receivedWorldEvent(NSObject data) {
        if (pListListeners.isEmpty()) {
            log.debug("receivedWorldEvent({})", data.toXMLPropertyList());
        }
        for (PlistListener listener: pListListeners) {
            listener.receivedWorldEvent(data);
        }
    }
    
    @Override
    public void showConsole() {
        if (pListListeners.isEmpty()) {
            log.debug("showConsole()");
        }
        for (PlistListener listener: pListListeners) {
            listener.showConsole();
        }
    }

}
