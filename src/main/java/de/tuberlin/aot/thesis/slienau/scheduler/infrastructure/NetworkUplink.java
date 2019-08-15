package de.tuberlin.aot.thesis.slienau.scheduler.infrastructure;

public class NetworkUplink {
    private final FogNode source;
    private final FogNode destination;
    private int latency;
    private long bitPerSecond;

    /**
     * @param source       source FogNode
     * @param destination  destination FogNode
     * @param latency      in milliseconds
     * @param bitPerSecond bandwidth in bit per second
     */
    public NetworkUplink(FogNode source, FogNode destination, int latency, long bitPerSecond) {
        this.source = source;
        this.destination = destination;
        this.latency = latency;
        this.bitPerSecond = bitPerSecond;
    }

    FogNode getSource() {
        return source;
    }

    FogNode getDestination() {
        return destination;
    }

    public int getLatency() {
        return latency;
    }

    public long getBitPerSecond() {
        return bitPerSecond;
    }

    public long getMBitPerSecond() {
        return bitPerSecond / 1000000;
    }

    @Override
    public String toString() {
        return "NetworkUplink{" +
                "source=" + source.getId() +
                ", destination=" + destination.getId() +
                ", latency=" + latency +
                ", bandwidth=" + this.getMBitPerSecond() + "Mbit/s" +
                '}';
    }
}
