package de.tuberlin.aot.thesis.slienau.examples;

import de.tuberlin.aot.thesis.slienau.orchestrator.NodeRedController;

public class NodeRedControllerExample {
    public static void main(String[] args) throws Exception {
        NodeRedController nrc = new NodeRedController("127.0.0.1");

        System.out.println("MQTT Broker node:\t" + nrc.getNodesByType("mqtt-broker"));
        System.out.println("Heartbeat flow:\t" + nrc.getFlowByName("Heartbeat"));
        nrc.changeHeartbeatFrequency("1");
        Thread.sleep(5 * 1000);
        nrc.changeHeartbeatFrequency("5");
    }
}
