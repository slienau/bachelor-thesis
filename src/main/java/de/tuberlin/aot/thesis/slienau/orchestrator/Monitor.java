package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tuberlin.aot.thesis.slienau.orchestrator.models.Heartbeat;
import de.tuberlin.aot.thesis.slienau.utils.MqttUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.HashMap;

public class Monitor implements Runnable {

    final String broker;
    final String topic;
    final MemoryPersistence persistence;
    final MqttConnectOptions connOpts;
    final HashMap<String, Heartbeat> nodes;

    public Monitor(String broker, String topic, HashMap<String, Heartbeat> nodes) {
        this.broker = broker;
        this.topic = topic;
        this.persistence = new MemoryPersistence();
        this.nodes = nodes;
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
    }

    public void run() {
        try {
            MqttClient client = new MqttClient(broker, MqttClient.generateClientId(), persistence);
            client.setCallback(new MonitorMqttCallback());
            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);
            System.out.println("Connected");
            client.subscribe(topic);
            System.out.println("Subscribed to " + topic);
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    class MonitorMqttCallback implements MqttCallback {

        public void connectionLost(Throwable throwable) {
            System.out.println("Connection to MQTT broker lost!");
        }

        public void messageArrived(String s, MqttMessage mqttMessage) {
            String jsonIn = new String(mqttMessage.getPayload());
            ObjectMapper mapper = new ObjectMapper();
            try {
                Heartbeat incomingHeartbeat = mapper.readValue(jsonIn, Heartbeat.class);
                nodes.put(incomingHeartbeat.getDeviceName(), incomingHeartbeat);
                System.out.println("Incoming Heartbeat:\t" + incomingHeartbeat);
            } catch (IOException e) {
                System.err.println(String.format("Error mapping incoming JSON string '%s' to Heartbeat object", jsonIn));
            }
        }

        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            // not used in this example
        }
    }

}
