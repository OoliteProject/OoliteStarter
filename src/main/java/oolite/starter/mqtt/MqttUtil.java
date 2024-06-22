/*
 */
package oolite.starter.mqtt;

import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Utility methods for MQTT.
 *
 * @author hiran
 */
public class MqttUtil {
    private static final Logger log = LogManager.getLogger();
    
    private static String publisherId = 
            MqttUtil.class.getPackage().getImplementationTitle() + "-" + 
            MqttUtil.class.getPackage().getImplementationVersion() + "-mqtt-" + 
            UUID.randomUUID().toString();

    /**
     * Returns this MQTT publisher ID.
     * 
     * @return the ID
     */
    public static String getPublisherId() {
        return publisherId;
    }
    
    /**
     * Returns the topic OoliteStarter will use for testing MQTT functionality.
     * 
     * @param prefix the user-defined prefix
     * @return the full topic name
     */
    public static String getTestTopic(String prefix) {
        return getTopic(prefix, "oolite/starter");
    }
    
    /**
     * Returns the prefixed topic.
     * 
     * @param prefix the user-defined prefix
     * @param bareTopic the topic name
     * @return the prefixed topic
     */
    public static String getTopic(String prefix, String bareTopic) {
        return prefix + bareTopic;
    }
    
    /**
     * Sends a test MQTT message or throws an exception.
     */
    public static String testConnection(String brokerUrl, String user, char[] password, String prefix) throws MqttException { 
        log.warn("testConnection({}, {}, ..., {})", brokerUrl, user, prefix);
        
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqttClient = new MqttClient(brokerUrl, getPublisherId(), persistence);

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
            
        String topic = getTestTopic(prefix);
        mqttClient.publish(topic, new MqttMessage("connection test".getBytes()));
            
        if (mqttClient.isConnected()) {
            log.info("Connected to {} as {}", brokerUrl, user);
        }

        mqttClient.disconnect();
        
        return "Sent test message to broker " + brokerUrl + " topic " + topic;
    }
}
