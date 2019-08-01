package algorithm.infrastructure;

public class NetworkUplink {
    private final FogNode source;
    private final FogNode destination;
    private int latency;
    private double bandwidth;

    NetworkUplink(FogNode source, FogNode destination, int latency, double bandwidth) {
        this.source = source;
        this.destination = destination;
        this.latency = latency;
        this.bandwidth = bandwidth;
    }

    FogNode getSource() {
        return source;
    }

    FogNode getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "NetworkUplink{" +
                "source=" + source.getId() +
                ", destination=" + destination.getId() +
                ", latency=" + latency +
                ", bandwidth=" + bandwidth +
                '}';
    }
}
