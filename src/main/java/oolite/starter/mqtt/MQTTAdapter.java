/*
 */
package oolite.starter.mqtt;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import java.util.UUID;
import oolite.starter.dcp.Connector;
import oolite.starter.dcp.PlistListener;
import oolite.starter.dcp.TCPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

/**
 *
 * @author hiran
 */
public class MQTTAdapter implements PlistListener {
    private static final Logger log = LogManager.getLogger();
    
    private IMqttClient mqttClient;
    private TCPServer tcpServer;
    
    /**
     * Creates a new instance.
     */
    public MQTTAdapter() {
        log.debug("MQTTAdapter()");
    }
    
    /**
     * Initializes - which means it connects to the MQTT broker.
     */
    public void init(TCPServer tcpServer, String brokerUrl, String user, char[] password) {
        log.debug("init({})", tcpServer);
        
        this.tcpServer = tcpServer;
        tcpServer.addConnectorStatusListener(new Connector.ConnectorStatusListener() {
            @Override
            public void statusChanged(Connector connector, Connector.Status oldStatus, Connector.Status newStatus) {
                if (newStatus == Connector.Status.connected) {
                    // activate data push
                    TCPServer t = (TCPServer)connector;
                    t.sendCommand("worldScripts[\"oolite-starter-oxp\"].pushdata = true");
                }
            }
        });
        
        String publisherId = getClass().getPackage().getImplementationTitle() + "-" + getClass().getPackage().getImplementationVersion() + "-mqtt-" + UUID.randomUUID().toString();
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(brokerUrl, publisherId, persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            if (user != null) {
                options.setUserName(user);
            }
            if (password != null) {
                options.setPassword(password);
            }
            mqttClient.connect(options);
            
            if (mqttClient.isConnected()) {
                log.info("Connected to {} as {}", brokerUrl, user);
            }
            
            mqttClient.subscribe("oolite/input", new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage mm) throws Exception {
                    log.warn("messageArrived({}, {})", topic, mm);
                    
                    tcpServer.sendCommand("player.ship.speed = 20");
                }
            });
            
            MqttMessage mm = new MqttMessage("Oolite started".getBytes());
            mqttClient.publish("test/topic", mm);
        } catch (Exception e) {
            log.error("Could not connect to MQTT server on {}", brokerUrl, e);
        }
    }

    @Override
    public void receivedConfiguration(NSObject data) {
        log.trace("receivedConfiguration({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        try {
            mqttClient.publish("oolite/configuration", mm);
        } catch (Exception e) {
            log.warn("Could not publish", e);
        }
    }

    @Override
    public void receivedConsoleOutput(NSObject data) {
        log.info("receivedConsoleOutput({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        try {
            mqttClient.publish("test/topic", mm);
        } catch (Exception e) {
            log.warn("Could not publish", e);
        }
    }
    
    protected void sendMqtt(String topic, String message) {
        try {
            MqttMessage mm = new MqttMessage(message.getBytes());
            mqttClient.publish(topic, mm);
        } catch (Exception e) {
            log.warn("Could not publish", e);
        }
    }

    @Override
    public void receivedCommandResult(NSObject dataO) {
        log.warn("receivedCommandResult({})", dataO);

        if (dataO instanceof NSDictionary data) {
            NSObject messageO = (NSObject)data.get("message");
            log.trace("message {} ({})", messageO, messageO.getClass());
            if (messageO instanceof NSString message) {
                // JSON decode this string? Or deliver it as metrics...
                String mstr = message.toString();
                if (!mstr.startsWith("{")) {
                    // no json message?
                    return;
                }
                
                JSONObject jo = new JSONObject(mstr);
                
                String msgType = jo.getString("msgType");
                
                switch(msgType) {
                    case "comms":
                        sendMqtt("oolite/comms", message.toString());
                        break;
                    case "controls":
                        sendMqtt("oolite/controls", message.toString());
                        break;
                    case "alert":
                        sendMqtt("oolite/alert", message.toString());
                        break;
                }
            }
        }
    }

    @Override
    public void receivedCommandAcknowledge(NSObject data) {
        log.warn("receivedCommandAcknowledge({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        try {
            mqttClient.publish("test/topic", mm);
        } catch (Exception e) {
            log.warn("Could not publish", e);
        }
    }

    @Override
    public void receivedLogMessage(NSObject data) {
        log.trace("receivedLogMessage({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        try {
            mqttClient.publish("test/topic", mm);
        } catch (Exception e) {
            log.warn("Could not publish", e);
        }
    }

    @Override
    public void receivedWorldEvent(NSObject data) {
        log.warn("receivedWorldEvent({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        try {
            mqttClient.publish("test/topic", mm);
        } catch (Exception e) {
            log.warn("Could not publish", e);
        }
    }

    @Override
    public void showConsole() {
        log.trace("showConsole()");
        MqttMessage mm = new MqttMessage("showConsole".getBytes());
        try {
            mqttClient.publish("test/topic", mm);
        } catch (Exception e) {
            log.warn("Could not publish", e);
        }
    }

}
