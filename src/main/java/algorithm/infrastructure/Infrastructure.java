package algorithm.infrastructure;

import algorithm.entities.FogNode;

import java.util.ArrayList;
import java.util.List;

public class Infrastructure {
    private final List<FogNode> fogNodes = new ArrayList<>();
    private final List<NetworkConnection> networkConnections = new ArrayList<>();

    public Infrastructure addFogNode(FogNode newFogNode) {
        // TODO: check if id is unique
        this.fogNodes.add(newFogNode);
        return this;
    }

    public Infrastructure removeFogNode(FogNode fogNodeToDelete) {
        // TODO: find and remove fog node from list
        return this;
    }

    public Infrastructure addNetworkConnection(NetworkConnection newNetworkConnection) {
        this.networkConnections.add(newNetworkConnection);
        return this;
    }

    public List<FogNode> getFogNodes() {
        return fogNodes;
    }

    public List<NetworkConnection> getNetworkConnections() {
        return networkConnections;
    }
}
