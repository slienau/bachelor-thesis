package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tuberlin.aot.thesis.slienau.models.resultstats.ResultStats;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedOrchestrator;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.NetworkUplink;
import de.tuberlin.aot.thesis.slienau.utils.MqttUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ResultsMonitor implements Runnable {

    private final static ObjectMapper MAPPER = new ObjectMapper();
    private final static String topic = "/results/#";

    @Override
    public void run() {
        try {
            MqttClient client = new MqttClient(NodeRedOrchestrator.MQTT_BROKER, MqttClient.generateClientId(), new MemoryPersistence());
            client.setCallback(new ResultMqttCallback());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            client.subscribe(topic);
            System.out.println("[ResultsMonitor] Subscribed to topic " + topic);
        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }

    }

    static class ResultMqttCallback implements MqttCallback {
        private static final int MIN_MESSAGE_SIZE = 500; // in KByte. smaller messages are not taken into account for link updates
        private NodeRedOrchestrator orchestrator = NodeRedOrchestrator.getInstance();

        @Override
        public void connectionLost(Throwable cause) {
            System.err.println("[ResultsMonitor] Connection to MQTT broker lost!");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            ResultStats stats = null;
            try {
                stats = MAPPER.readValue(message.getPayload(), ResultStats.class);

                if (stats == null)
                    return;

                stats.getTransfers().forEach(transferStat -> {
                    if (transferStat.getSizeInKByte() < MIN_MESSAGE_SIZE) {
                        return;
                    }
                    FogNode sourceNode = orchestrator.getInfrastructure().getFogNode(transferStat.getSourceNode());
                    if (sourceNode == null)
                        return;
                    NetworkUplink uplink = sourceNode.getUplinkTo(transferStat.getDestinationNode());
                    if (uplink == null)
                        return;
                    double oldSpeed = uplink.getMBitPerSecond();
                    double newSpeed = transferStat.getMbitPerSecond();
                    uplink.setMbitPerSecond(newSpeed);
                    System.out.println(String.format("[ResultsMonitor] Updated uplink speed from %s to %s. Old speed: %s Mbit/s; New Speed: %s Mbit/s", sourceNode.getId(), uplink.getDestination().getId(), oldSpeed, newSpeed));
                });
            } catch (Exception e) {
                System.err.println("FAILED to map stats message to ResultStats object. " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }

}
