package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;

import java.util.List;

public class NodeRedFogNode extends FogNode {

    private final NodeRedController nodeRedController;

    public NodeRedFogNode(String id, String address, int ramTotal, int storageTotal, int cpuCores, int cpuInstructionsPerSecond, List<String> connectedHardware) {
        super(id, ramTotal, storageTotal, cpuCores, cpuInstructionsPerSecond, connectedHardware);
        nodeRedController = new NodeRedController(id, address);
    }

    public NodeRedController getNodeRedController() {
        return nodeRedController;
    }

    public String getAddress() {
        return nodeRedController.getNodeRedAddress();
    }

    @Override
    public String toString() {
        return "NodeRedFogNode{" +
                "nodeRedController=" + nodeRedController +
                ", fogNode=" + super.toString() +
                '}';
    }
}
