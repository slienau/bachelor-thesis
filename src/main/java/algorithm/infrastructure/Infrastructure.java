package algorithm.infrastructure;

import java.util.*;

public class Infrastructure {
    private final Map<String, FogNode> fogNodes = new HashMap<>(); // <nodeId, FogNode object>
    private final Map<String, NetworkUplink> networkUplinks = new HashMap<>(); // <source->destination, NetworkUplink object>

    private static String uplinkKey(NetworkUplink uplink) {
        return uplinkKey(uplink.getSource(), uplink.getDestination());
    }

    private static String uplinkKey(FogNode source, FogNode destination) {
        return uplinkKey(source.getId(), destination.getId());
    }

    private static String uplinkKey(String source, String destination) {
        return String.format("%s->%s", source, destination);
    }

    public void createFogNode(String id, int ramTotal, int storageTotal, int cpuCores, int cpuScoreSingleCore) throws IllegalArgumentException {
        this.addFogNode(new FogNode(id, ramTotal, storageTotal, cpuCores, cpuScoreSingleCore, this));
    }

    private void addFogNode(FogNode newFogNode) throws IllegalArgumentException {
        if (fogNodes.putIfAbsent(newFogNode.getId(), newFogNode) == null) {
            // fognode added
            System.out.println(String.format("[Infrastructure] Added %s", newFogNode));
            this.addUplink(new NetworkUplink(newFogNode, newFogNode, 0, Long.MAX_VALUE));
        } else {
            // not added because exists already
            throw new IllegalArgumentException(String.format("Can not add %s to infrastructure because it already exists", newFogNode.getId()));
        }
    }

    public FogNode getFogNodeById(String fogNodeId) throws NoSuchElementException {
        FogNode result = fogNodes.get(fogNodeId);
        if (result != null)
            return result;
        throw new NoSuchElementException(String.format("Unable to find fog node with id '%s'", fogNodeId));
    }

    public boolean removeFogNode(String nodeIdToDelete) {
        boolean wasRemoved = (fogNodes.remove(nodeIdToDelete) != null);
        if (wasRemoved)
            System.out.println(String.format("[Infrastructure] %s removed from infrastructure.", nodeIdToDelete));
        else
            System.err.println(String.format("[Infrastructure] %s not removed from infrastructure because it doesn't exist.", nodeIdToDelete));
        return wasRemoved;
    }

    private void addUplink(NetworkUplink uplink) throws NoSuchElementException {
        // check if nodes exist
        if ((!this.fogNodes.containsKey(uplink.getSource().getId())) || (!this.fogNodes.containsKey(uplink.getDestination().getId()))) {
            throw new NoSuchElementException(String.format(
                    "Couldn't add %s to infrastructure because either source or destination doesn't exist", uplink
            ));
        }
        // add uplink to infrastructure if it doesn't exist
        if (networkUplinks.putIfAbsent(uplinkKey(uplink), uplink) != null) {
            throw new IllegalArgumentException(String.format("Unable to add %s because it already exists.", uplink));
        }
        System.out.println(String.format("[Infrastructure] Added %s", uplink));
    }

    public List<FogNode> getFogNodes() {
        return new ArrayList<>(fogNodes.values());
    }

    NetworkUplink getUplink(String source, String destination) throws NoSuchElementException {
        NetworkUplink uplink = networkUplinks.get(uplinkKey(source, destination));
        if (uplink == null)
            throw new NoSuchElementException(String.format("Uplink form %s to %s not found.", source, destination));
        return uplink;
    }

    public void updateUplinks(FogNode nodeA, FogNode nodeB, int latency, double bandwidthAtoB, double bandwidthBtoA) {
        // TODO
        System.err.println("TODO: updateUplinks(FogNode nodeA, FogNode nodeB, int latency, double bandwidthAtoB, double bandwidthBtoA)");
    }

    /**
     * @param fogNodeA
     * @param fogNodeB
     * @param latency       in milliseconds
     * @param bandwidthAtoB link bandwidth from node A to node B in Mbit/s
     * @param bandwidthBtoA link bandwidth from node B to node A in Mbit/s
     * @throws NoSuchElementException
     */
    public void createUplinks(String fogNodeA, String fogNodeB, int latency, double bandwidthAtoB, double bandwidthBtoA) throws NoSuchElementException {
        try {
            FogNode nodeA = this.getFogNodeById(fogNodeA);
            FogNode nodeB = this.getFogNodeById(fogNodeB);
            int bandwidthAtoB_bitsPerSecond = (int) (bandwidthAtoB * Math.pow(10, 6)); // Mbit/s -> bit/s
            int bandwidthBtoA_bitsPerSecond = (int) (bandwidthBtoA * Math.pow(10, 6)); // Mbit/s -> bit/s
            this.addUplink(new NetworkUplink(nodeA, nodeB, latency, bandwidthAtoB_bitsPerSecond)); // A to B
            this.addUplink(new NetworkUplink(nodeB, nodeA, latency, bandwidthBtoA_bitsPerSecond)); // B to A
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(String.format("Unable to create uplinks. %s", e.getMessage()));
        }
    }
}
