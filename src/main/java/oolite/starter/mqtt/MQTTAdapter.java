/*
 */
package oolite.starter.mqtt;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import oolite.starter.dcp.Connector;
import oolite.starter.dcp.PlistListener;
import oolite.starter.dcp.TCPServer;
import oolite.starter.model.Installation;
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
    
    private String TOPIC_OOLITE_STARTER = "oolite/starter";
    private String TOPIC_OOLITE_INPUT = "oolite/input";
    private String TOPIC_OOLITE_CONFIGURATION = "oolite/configuration";
    private String TOPIC_OOLITE_CONSOLE = "oolite/console";
    private String TOPIC_OOLITE_COMMS = "oolite/comms";
    private String TOPIC_OOLITE_CONTROLS = "oolite/controls";
    private String TOPIC_OOLITE_ALERT = "oolite/alert";
    private String TOPIC_OOLITE_UNKNOWN = "oolite/unknown";
    private String TOPIC_OOLITE_COMMANDACKNOWLEDGE = "oolite/commandAcknowledge";
    private String TOPIC_OOLITE_LOG = "oolite/log";
    private String TOPIC_OOLITE_WORLDEVENT = "oolite/worldEvent";
    private String TOPIC_OOLITE_SHOWCONSOLE = "oolite/showConsole";
    private String TOPIC_OOLITE_ERROR = "oolite/starter";
    
    /**
     * Creates a new instance.
     */
    public MQTTAdapter() {
        log.debug("MQTTAdapter()");
    }
    
    /**
     * Initializes - which means it connects to the MQTT broker.
     */
    public void init(TCPServer tcpServer, Installation.Mqtt mqtt) {
        log.debug("init({}, {})", tcpServer, mqtt);
        
        
        String prefix = mqtt.getPrefix();
        TOPIC_OOLITE_STARTER = MqttUtil.getTopic(prefix,  "oolite/starter");
        TOPIC_OOLITE_INPUT = MqttUtil.getTopic(prefix, "oolite/input");
        TOPIC_OOLITE_CONFIGURATION = MqttUtil.getTopic(prefix, "oolite/configuration");
        TOPIC_OOLITE_CONSOLE = MqttUtil.getTopic(prefix, "oolite/console");
        TOPIC_OOLITE_COMMS = MqttUtil.getTopic(prefix, "oolite/comms");
        TOPIC_OOLITE_CONTROLS = MqttUtil.getTopic(prefix, "oolite/controls");
        TOPIC_OOLITE_ALERT = MqttUtil.getTopic(prefix, "oolite/alert");
        TOPIC_OOLITE_UNKNOWN = MqttUtil.getTopic(prefix, "oolite/unknown");
        TOPIC_OOLITE_COMMANDACKNOWLEDGE = MqttUtil.getTopic(prefix, "oolite/commandAcknowledge");
        TOPIC_OOLITE_LOG = MqttUtil.getTopic(prefix, "oolite/log");
        TOPIC_OOLITE_WORLDEVENT = MqttUtil.getTopic(prefix, "oolite/worldEvent");
        TOPIC_OOLITE_SHOWCONSOLE = MqttUtil.getTopic(prefix, "oolite/showConsole");
        TOPIC_OOLITE_ERROR = MqttUtil.getTopic(prefix, "oolite/starter");
        
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
        
        String publisherId = MqttUtil.getPublisherId();
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(mqtt.getBrokerUrl(), publisherId, persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            if (mqtt.getUser() != null) {
                options.setUserName(mqtt.getUser());
            }
            if (mqtt.getPassword() != null) {
                options.setPassword(mqtt.getPassword());
            }
            mqttClient.connect(options);
            
            mqttClient.publish(TOPIC_OOLITE_STARTER, new MqttMessage("started".getBytes()));
            
            if (mqttClient.isConnected()) {
                log.info("Connected to {} as {}", mqtt.getBrokerUrl(), mqtt.getUser());
            }
            
            mqttClient.subscribe(TOPIC_OOLITE_INPUT, new IMqttMessageListener() {
                
                /**
                 * Expect JSON messages so we can extend them in the future.
                 * Only the command part will be transmitted to Oolite.
                 * <p>
                 * With that a message can look like this:
                 * <pre>
                 *   { 'command'="player.ship.speed=4800" }
                 * </pre>
                 */
                @Override
                public void messageArrived(String topic, MqttMessage mm) throws Exception {
                    log.debug("messageArrived({}, {})", topic, mm);
                    
                    try {
                        JSONObject jo = new JSONObject(new String(mm.getPayload()));
                        if (jo.has("command")) {
                            tcpServer.sendCommand(jo.getString("command"));
                        }
                    } catch (Exception e) {
                        log.error("Could not consume message {}", mm);
                        sendMqtt(TOPIC_OOLITE_ERROR, String.format("Could not consume message %s", String.valueOf(mm)));
                    }
                }
            });
            
            MqttMessage mm = new MqttMessage("Oolite started".getBytes());
            mqttClient.publish(TOPIC_OOLITE_STARTER, mm);
        } catch (Exception e) {
            log.error("Could not connect to MQTT server on {} as {}", mqtt.getBrokerUrl(), mqtt.getUser(), e);
        }
    }
    
    /**
     * Disconnects this MQTT client.
     */
    public void shutdown()  {
        try {
            mqttClient.publish(TOPIC_OOLITE_STARTER, new MqttMessage("Oolite stopped".getBytes()));
            mqttClient.disconnect();
        } catch (Exception e) {
            log.error("could not post shutdown message");
        }
    }

    @Override
    public void receivedConfiguration(NSObject data) {
        log.trace("receivedConfiguration({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        sendMqtt(TOPIC_OOLITE_CONFIGURATION, mm.toString());
    }

    @Override
    public void receivedConsoleOutput(NSObject data) {
        log.info("receivedConsoleOutput({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        sendMqtt(TOPIC_OOLITE_CONSOLE, mm.toString());
    }
    
    protected void sendMqtt(String topic, String message) {
        try {
            MqttMessage mm = new MqttMessage(message.getBytes());
            mqttClient.publish(topic, mm);
        } catch (Exception e) {
            log.warn("Could not publish to {}/{}", mqttClient.getServerURI(), topic, e);
        }
    }

    @Override
    public void receivedCommandResult(NSObject dataO) {
        log.debug("receivedCommandResult({})", dataO);

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
                        sendMqtt(TOPIC_OOLITE_COMMS, message.toString());
                        break;
                    case "controls":
                        sendMqtt(TOPIC_OOLITE_CONTROLS, message.toString());
                        break;
                    case "alert":
                        sendMqtt(TOPIC_OOLITE_ALERT, message.toString());
                        break;
                    default:
                        sendMqtt(TOPIC_OOLITE_UNKNOWN, message.toString());
                        break;
                }
            } else {
                sendMqtt(TOPIC_OOLITE_UNKNOWN, messageO.toString());
            }
        } else {
            sendMqtt(TOPIC_OOLITE_UNKNOWN, dataO.toString());
        }
    }

    @Override
    public void receivedCommandAcknowledge(NSObject data) {
        log.debug("receivedCommandAcknowledge({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        sendMqtt(TOPIC_OOLITE_COMMANDACKNOWLEDGE, mm.toString());
    }

    @Override
    public void receivedLogMessage(NSObject data) {
        log.trace("receivedLogMessage({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        sendMqtt(TOPIC_OOLITE_LOG, mm.toString());
    }

    @Override
    public void receivedWorldEvent(NSObject data) {
        log.debug("receivedWorldEvent({})", data);
        MqttMessage mm = new MqttMessage(data.toXMLPropertyList().getBytes());
        sendMqtt(TOPIC_OOLITE_WORLDEVENT, mm.toString());
    }

    @Override
    public void showConsole() {
        log.trace("showConsole()");
        MqttMessage mm = new MqttMessage("showConsole".getBytes());
        sendMqtt(TOPIC_OOLITE_SHOWCONSOLE, mm.toString());
    }

}
