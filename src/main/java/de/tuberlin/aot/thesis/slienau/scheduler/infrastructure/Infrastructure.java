package de.tuberlin.aot.thesis.slienau.scheduler.infrastructure;

import de.tuberlin.aot.thesis.slienau.scheduler.SchedulerUtils;

import java.util.*;

public class Infrastructure {
    private final Map<String, FogNode> fogNodes = new HashMap<>(); // <nodeId, FogNode object>

    public void addFogNode(String id, int ramTotal, int storageTotal, int cpuCores, int cpuScoreSingleCore, List<String> connectedHardware) throws IllegalArgumentException {
        this.addFogNode(new FogNode(id, ramTotal, storageTotal, cpuCores, cpuScoreSingleCore, connectedHardware));
    }

    public void addFogNode(FogNode newFogNode) throws IllegalArgumentException {
        if (fogNodes.putIfAbsent(newFogNode.getId(), newFogNode) == null) {
            // fognode added
            System.out.println(String.format("[Infrastructure] Added node '%s'", newFogNode.getId()));
        } else {
            // not added because exists already
            throw new IllegalArgumentException(String.format("Can not add %s to infrastructure because it already exists", newFogNode.getId()));
        }
    }

    public FogNode getFogNode(String fogNodeId) {
        return fogNodes.get(fogNodeId);
    }

    public boolean removeFogNode(String nodeIdToDelete) {
        boolean wasRemoved = (fogNodes.remove(nodeIdToDelete) != null);
        if (wasRemoved)
            System.out.println(String.format("[Infrastructure] %s removed from infrastructure.", nodeIdToDelete));
        else
            System.err.println(String.format("[Infrastructure] %s not removed from infrastructure because it doesn't exist.", nodeIdToDelete));
        return wasRemoved;
    }

    public boolean checkIfFogNodeExists(String fogNodeId) {
        return fogNodes.get(fogNodeId) != null;
    }

    public List<FogNode> getFogNodes() {
        return new ArrayList<>(fogNodes.values());
    }

    /**
     * @param fogNodeA
     * @param fogNodeB
     * @param latency       in milliseconds
     * @param bandwidthAtoB link bandwidth from node A to node B in Mbit/s
     * @param bandwidthBtoA link bandwidth from node B to node A in Mbit/s
     */
    public void addNetworkLink(String fogNodeA, String fogNodeB, int latency, double bandwidthAtoB, double bandwidthBtoA) {
        try {
            long bandwidthAtoB_bitPerSecond = SchedulerUtils.mbitToBit(bandwidthAtoB);
            long bandwidthBtoA_bitPerSecond = SchedulerUtils.mbitToBit(bandwidthBtoA);
            FogNode nodeA = this.getFogNode(fogNodeA);
            FogNode nodeB = this.getFogNode(fogNodeB);
            nodeA.addUplink(new NetworkUplink(nodeA, nodeB, latency, bandwidthAtoB_bitPerSecond)); // A to B
            nodeB.addUplink(new NetworkUplink(nodeB, nodeA, latency, bandwidthBtoA_bitPerSecond)); // B to A
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(String.format("Unable to create uplinks. %s", e.getMessage()));
        }
    }

    public void updateUplinks(String nodeA, String nodeB, int latency, double bandwidthAtoB, double bandwidthBtoA) {
        System.err.println("TODO: updateUplinks(FogNode nodeA, FogNode nodeB, int latency, double bandwidthAtoB, double bandwidthBtoA)");
        throw new RuntimeException("TODO: Implement method.");
    }
}
