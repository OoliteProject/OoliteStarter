/*
 */
package oolite.starter.dcp;

import com.dd.plist.NSObject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 *
 * @author hiran
 */
public class TCPServer extends AbstractConnector {
    private static final Logger log = LogManager.getLogger();
    
    public static final int PORT = 8563;
    
    private IoAcceptor acceptor;
    private DebugConsoleHandler dch;
    private ArrayList<PlistListener> plistListeners = new ArrayList<>();
    
    private Connector.Status connectionStatus = Connector.Status.passive;
    private Thread connectionBuilder;
    
    /**
     * Creates a new TCPServer.
     */
    public TCPServer() {
        log.debug("TCPServer()");
    }

    /**
     * Sends a command to Oolite.
     * 
     * @param command the javascript command
     */
    public void sendCommand(String command) {
        dch.sendCommand(command);
    }
    
    /**
     * Sends a command to Oollite.
     * 
     * @param command the command
     */
    public void sendCommand(OoliteCommand command) {
        dch.sendCommand(command);
    }

    /**
     * Starts the TCP listener, which will bind to a port and handle incoming
     * connections.
     * 
     * @throws IOException something went wrong
     */
    public void startup() throws IOException {
        startup(null);
    }
    
    /**
     * Starts the TCP listener, which will bind to a port and handle incoming
     * connections.
     * 
     * @param listener the listener to be added to the notification list
     * @throws IOException something went wrong
     */
    public void startup(PlistListener listener) throws IOException {
        if (listener != null) {
            addPlistListenerListener(listener);
        }
        
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("logger", new LoggingFilter(TCPServer.class.getName()+"-filter"));
        //acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"), "\n", "\n")));
        //acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineEncoder(), new DomProtocolDecoder()));
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new PlistProtocolEncoder(), new PlistProtocolDecoder()));
        acceptor.addListener(new IoServiceListener() {
            @Override
            public void serviceActivated(IoService service) throws Exception {
                if (hasSession()) {
                    connectionStatus = Status.connected;
                } else {
                    connectionStatus = Status.listening;
                }
            }

            @Override
            public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
                if (hasSession()) {
                    connectionStatus = Status.connected;
                } else {
                    connectionStatus = Status.listening;
                }
            }

            @Override
            public void serviceDeactivated(IoService service) throws Exception {
                connectionStatus = Status.passive;
            }

            @Override
            public void sessionCreated(IoSession session) throws Exception {
                if (hasSession()) {
                    connectionStatus = Status.connected;
                } else {
                    connectionStatus = Status.listening;
                }
            }

            @Override
            public void sessionClosed(IoSession session) throws Exception {
                if (hasSession()) {
                    connectionStatus = Status.connected;
                } else {
                    connectionStatus = Status.listening;
                }
            }

            @Override
            public void sessionDestroyed(IoSession session) throws Exception {
                if (hasSession()) {
                    connectionStatus = Status.connected;
                } else {
                    connectionStatus = Status.listening;
                }
            }
        });
        
        dch = new DebugConsoleHandler();
        dch.addPlistListener(new PlistListener() {
            @Override
            public void receivedConfiguration(NSObject data) {
                for (PlistListener listener: plistListeners) {
                    listener.receivedConfiguration(data);
                }
            }

            @Override
            public void receivedConsoleOutput(NSObject data) {
                for (PlistListener listener: plistListeners) {
                    listener.receivedConsoleOutput(data);
                }
            }

            @Override
            public void receivedCommandResult(NSObject data) {
                for (PlistListener listener: plistListeners) {
                    listener.receivedCommandResult(data);
                }
            }

            @Override
            public void receivedCommandAcknowledge(NSObject data) {
                for (PlistListener listener: plistListeners) {
                    listener.receivedCommandAcknowledge(data);
                }
            }

            @Override
            public void receivedLogMessage(NSObject data) {
                for (PlistListener listener: plistListeners) {
                    listener.receivedLogMessage(data);
                }
            }

            @Override
            public void receivedWorldEvent(NSObject data) {
                for (PlistListener listener: plistListeners) {
                    listener.receivedWorldEvent(data);
                }
            }

            @Override
            public void showConsole() {
                for (PlistListener listener: plistListeners) {
                    listener.showConsole();
                }
            }

            @Override
            public void shutdown() {
                for (PlistListener listener: plistListeners) {
                    listener.shutdown();
                }
            }
        });
        dch.addStatusListener(new DebugConsoleHandler.StatusListener() {
            
            Status translate(DebugConsoleHandler.Status s) {
                switch(s) {
                    case approved:
                        return Status.connected;
                    case connected:
                        return Status.connecting;
                    case listening:
                        return Status.listening;
                    case passive:
                        return Status.passive;
                    default:
                        return Status.error;
                }
            }
            
            @Override
            public void statusChanged(DebugConsoleHandler.Status oldStatus, DebugConsoleHandler.Status newStatus) {
                log.info("statusChanged({}->{})", oldStatus, newStatus);
                fireStatusChanged(translate(oldStatus), translate(newStatus));
            }
        });
        acceptor.setHandler(dch);

        acceptor.getSessionConfig().setReadBufferSize(4096);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 180);
        InetSocketAddress isa = new InetSocketAddress("localhost", PORT);
        acceptor.bind(isa);
        log.info("{} listening on {}", TCPServer.class.getName(), isa);
    }
    
    /**
     * Shuts down all the connections and stops listening on the port.
     */
    public void shutdown() {
        for (PlistListener listener: plistListeners) {
            listener.shutdown();
        }
        acceptor.setCloseOnDeactivation(true);
        acceptor.unbind();
    }

    /**
     * A main mothod allowing to startup a TCPServer and see what happens.
     * This method is for simplified testing only.
     * 
     * @param args the command line args, none of which are used.
     */
    public static void main(String[] args) {
        log.debug("main({})", Arrays.asList(args));
        
        TCPServer server = new TCPServer();
        try {
            server.startup();
        } catch (Exception e) {
            log.error("Problem", e);
            server.shutdown();
            System.exit(1);
        }
    }

    /**
     * Returns true if at least one client is connected.
     * 
     * @return true if at least one client is connected, otherwise false
     */
    public boolean hasSession() {
        return dch.hasSession();
    }

    @Override
    public Status getStatus() {
        return connectionStatus;
    }

    @Override
    public void nudge() {
        log.debug("nudge()");
        connectionStatus = Status.passive;
        
        if (connectionBuilder == null || !connectionBuilder.isAlive()) {
            log.trace("triggering connection builder");
            connectionBuilder = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.warn("getting Configuration");
                        throw new UnsupportedOperationException();
//                        //Configuration.loadConfiguration();
//
//                        log.debug("initializing server...");
//                        startup();
//                        connectionStatus = Status.listening;
//
//                        log.debug("waiting for client connect...");
//                        while (!hasSession()) {
//                            Thread.sleep(500);
//                        }
//                        connectionStatus = Status.connected;
                        
                    } catch (Exception e) {
                        log.error("Could not connect", e);
                        connectionStatus = Status.error;
                    }
                }
            }, "connectionBuilderThread");
            connectionBuilder.start();
        } else {
            log.trace("not triggering - process in progress");
        }
    }
    
    /**
     * Adds a PlistListener to the notification list.
     * 
     * @param listener the listener
     */
    public void addPlistListenerListener(PlistListener listener) {
        plistListeners.add(listener);
    }
}
