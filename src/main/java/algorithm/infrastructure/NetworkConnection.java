package algorithm.infrastructure;

public class NetworkConnection {
    private String nodeA;
    private String nodeB;
    private int latency;
    private double bandwidthAtoB;
    private double bandwidthBtoA;

    public NetworkConnection(String nodeA, String nodeB, int latency, double bandwidthAtoB, double bandwidthBtoA) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.latency = latency;
        this.bandwidthAtoB = bandwidthAtoB;
        this.bandwidthBtoA = bandwidthBtoA;
    }

}
