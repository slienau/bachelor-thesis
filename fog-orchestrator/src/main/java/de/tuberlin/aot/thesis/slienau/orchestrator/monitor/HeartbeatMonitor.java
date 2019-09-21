package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedOrchestrator;
import de.tuberlin.aot.thesis.slienau.orchestrator.models.Heartbeat;
import de.tuberlin.aot.thesis.slienau.utils.MqttUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.Queue;

public class HeartbeatMonitor implements Runnable {

    private final String broker;
    private final String topic;
    private final MemoryPersistence persistence;
    private final MqttConnectOptions connOpts;

    public HeartbeatMonitor() {
        this.broker = NodeRedOrchestrator.MQTT_BROKER;
        this.topic = "/heartbeats/#";
        this.persistence = new MemoryPersistence();
        connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
    }

    public void run() {
        try {
            MqttClient client = new MqttClient(broker, MqttClient.generateClientId(), persistence);
            client.setCallback(new MonitorMqttCallback());
            System.out.println("[HeartbeatMonitor] Connecting to MQTT broker: " + broker);
            client.connect(connOpts);
            System.out.println("[HeartbeatMonitor] Connected");
            client.subscribe(topic);
            System.out.println("[HeartbeatMonitor] Subscribed to topic " + topic);
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    static class MonitorMqttCallback implements MqttCallback {
        final Queue<Heartbeat> heartbeatQueue = NodeRedOrchestrator.getInstance().getHeartbeatQueue();

        public void connectionLost(Throwable throwable) {
            System.out.println("[HeartbeatMonitor] Connection to MQTT broker lost!");
        }

        public void messageArrived(String topic, MqttMessage mqttMessage) {
            String jsonIn = new String(mqttMessage.getPayload());
            ObjectMapper mapper = new ObjectMapper();
            try {
                Heartbeat incomingHeartbeat = mapper.readValue(jsonIn, Heartbeat.class);
                synchronized (heartbeatQueue) {
                    heartbeatQueue.add(incomingHeartbeat);
                    heartbeatQueue.notifyAll();
                }

            } catch (IOException e) {
                System.err.println(String.format("[HeartbeatMonitor] Error mapping incoming JSON string '%s' to Heartbeat object", jsonIn));
            }
        }

        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            // not used in this example
        }
    }

}