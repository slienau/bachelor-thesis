package de.tuberlin.aot.thesis.slienau.orchestrator.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedOrchestrator;
import de.tuberlin.aot.thesis.slienau.orchestrator.models.resultstats.ResultStats;
import de.tuberlin.aot.thesis.slienau.scheduler.application.AppLoop;
import de.tuberlin.aot.thesis.slienau.scheduler.application.Application;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.NetworkUplink;
import de.tuberlin.aot.thesis.slienau.utils.MqttUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;

public class QoSMonitor implements Runnable {

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
            System.out.println("[QoSMonitor] Subscribed to topic " + topic);

            new DeploymentMonitor().start();

        } catch (MqttException me) {
            MqttUtils.printMqttException(me);
        }
    }

    static class ResultMqttCallback implements MqttCallback {
        private NodeRedOrchestrator orchestrator = NodeRedOrchestrator.getInstance();

        @Override
        public void connectionLost(Throwable cause) {
            System.err.println("[QoSMonitor] Connection to MQTT broker lost!");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            try {
                ResultStats stats = MAPPER.readValue(message.getPayload(), ResultStats.class);

                if (stats == null)
                    return;

                int totalLatency = stats.getTotalTime();
                Application application = orchestrator.getApplication();
                // Check if the QoS requirements could be satisfied
                for (AppLoop loop : application.getLoops()) {
                    if (totalLatency > loop.getMaxLatency()) {
                        // max latency exceeded --> find new deployment strategy

                        System.out.println(String.format("[QoSMonitor] maxLatency of %s ms exceeded in application '%s' (loop '%s')! Actual latency was %s ms", loop.getMaxLatency(), application.getName(), loop.getLoopName(), totalLatency));

                        // Update uplinks based on the "transfers" array of the statistics object
                        stats.getTransfers().forEach(transferStat -> {
                            FogNode sourceNode = orchestrator.getInfrastructure().getFogNode(transferStat.getSourceNode());
                            if (sourceNode == null)
                                return;
                            NetworkUplink uplink = sourceNode.getUplinkTo(transferStat.getDestinationNode());
                            if (uplink == null)
                                return;
                            double oldSpeed = uplink.getMBitPerSecond();
                            double newSpeed = transferStat.getMbitPerSecond();
                            uplink.setMbitPerSecond(newSpeed);
                            System.out.println(String.format("[QoSMonitor] Updated uplink speed from %s to %s. Old speed: %s Mbit/s; New Speed: %s Mbit/s", sourceNode.getId(), uplink.getDestination().getId(), oldSpeed, newSpeed));
                        });

                        // check and deploy new strategy
                        orchestrator.checkForNewOptimalDeployment();
                    }
                }


            } catch (Exception e) {
                System.err.println("FAILED to map stats message to ResultStats object. " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }

    /**
     * Checks every X seconds if there is a better deployment strategy which is possible in case of environmental changes
     */
    static class DeploymentMonitor extends Thread {
        private final static int INTERVAL = 10; // check every 10 seconds

        @Override
        public void run() {
            while (true) {
                try {
                    // System.out.println("[QoSMonitor] Checking if there is a new optimal deployment");
                    NodeRedOrchestrator.getInstance().checkForNewOptimalDeployment();
                    Thread.sleep(INTERVAL * 1000);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
