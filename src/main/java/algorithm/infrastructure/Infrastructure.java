package algorithm.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Infrastructure {
    private final Map<String, FogNode> fogNodes = new HashMap<>(); // <nodeId, node object>
    private final List<NetworkUplink> networkUplinks = new ArrayList<>();

    public void createFogNode(String id, int ramTotal, int storageTotal, int cpuCores, int cpuScoreSingleCore) throws IllegalArgumentException {
        this.addFogNode(new FogNode(id, ramTotal, storageTotal, cpuCores, cpuScoreSingleCore, this));
    }

    private void addFogNode(FogNode newFogNode) throws IllegalArgumentException {
        if (fogNodes.putIfAbsent(newFogNode.getId(), newFogNode) == null) {
            // fognode added
            System.out.println(String.format("[Infrastructure] Added %s", newFogNode));
            this.addUplink(new NetworkUplink(newFogNode, newFogNode, 0, Double.MAX_VALUE));
        } else {
            // not added because exists already
            throw new IllegalArgumentException(String.format("Can not add %s to infrastructure because it already exists", newFogNode.getId()));
        }
    }

    public FogNode getFogNodeById(String fogNodeId) throws NullPointerException {
        FogNode result = fogNodes.get(fogNodeId);
        if (result != null)
            return result;
        throw new NullPointerException(String.format("Unable to find fog node with id '%s'", fogNodeId));
    }

    public boolean removeFogNode(String nodeIdToDelete) {
        boolean wasRemoved = (fogNodes.remove(nodeIdToDelete) != null);
        if (wasRemoved)
            System.out.println(String.format("[Infrastructure] %s removed from infrastructure.", nodeIdToDelete));
        else
            System.err.println(String.format("[Infrastructure] %s not removed from infrastructure because it doesn't exist.", nodeIdToDelete));
        return wasRemoved;
    }

    private void addUplink(NetworkUplink uplink) throws NullPointerException {
        // check if nodes exist
        if ((!this.fogNodes.containsKey(uplink.getSource().getId())) || (!this.fogNodes.containsKey(uplink.getDestination().getId()))) {
            throw new NullPointerException(String.format(
                    "Couldn't add %s to infrastructure because either source or destination doesn't exist", uplink
            ));
        }

        // check if uplink already exists
        for (NetworkUplink existingUplink : this.networkUplinks) {
            if (existingUplink.getSource().getId().equals(uplink.getSource().getId())
                    && existingUplink.getDestination().getId().equals(uplink.getDestination().getId())) {
                throw new IllegalArgumentException(String.format("Unable to add %s because it already exists.", uplink));
            }
        }

        // add uplink to infrastructure
        networkUplinks.add(uplink);
        System.out.println(String.format("[Infrastructure] Added %s", uplink));
    }

    public List<FogNode> getFogNodes() {
        return new ArrayList<>(fogNodes.values());
    }

    NetworkUplink getUplink(String source, String destination) throws NullPointerException {
        for (NetworkUplink uplink : this.networkUplinks) {
            if (uplink.getSource().getId().equals(source) && uplink.getDestination().getId().equals(destination))
                return uplink;
        }
        throw new NullPointerException(String.format("Uplink form %s to %s not found.", source, destination));
    }

    public void updateUplinks(FogNode nodeA, FogNode nodeB, int latency, double bandwidthAtoB, double bandwidthBtoA) {
        // TODO
        System.err.println("TODO: updateUplinks(FogNode nodeA, FogNode nodeB, int latency, double bandwidthAtoB, double bandwidthBtoA)");
    }

    public void createUplinks(String fogNodeA, String fogNodeB, int latency, double bandwidthAtoB, double bandwidthBtoA) throws NullPointerException {
        try {
            FogNode nodeA = this.getFogNodeById(fogNodeA);
            FogNode nodeB = this.getFogNodeById(fogNodeB);
            this.addUplink(new NetworkUplink(nodeA, nodeB, latency, bandwidthAtoB)); // A to B
            this.addUplink(new NetworkUplink(nodeB, nodeA, latency, bandwidthBtoA)); // B to A
        } catch (NullPointerException e) {
            throw new NullPointerException(String.format("Unable to create uplinks. %s", e.getMessage()));
        }
    }
}
