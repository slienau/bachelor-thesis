package algorithm.infrastructure;

public class NetworkUplink {
    private final FogNode source;
    private final FogNode destination;
    private int latency;
    private long bandwidthBitsPerSecond;

    /**
     * @param source                 source FogNode
     * @param destination            destination FogNode
     * @param latency                in milliseconds
     * @param bandwidthBitsPerSecond in bits per second
     */
    NetworkUplink(FogNode source, FogNode destination, int latency, long bandwidthBitsPerSecond) {
        this.source = source;
        this.destination = destination;
        this.latency = latency;
        this.bandwidthBitsPerSecond = bandwidthBitsPerSecond;
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

    public long getBandwidthBitsPerSecond() {
        return bandwidthBitsPerSecond;
    }

    public long getBandwidthMBitsPerSecond() {
        return bandwidthBitsPerSecond / 1000000;
    }

    @Override
    public String toString() {
        return "NetworkUplink{" +
                "source=" + source.getId() +
                ", destination=" + destination.getId() +
                ", latency=" + latency +
                ", bandwidth=" + bandwidthBitsPerSecond / 1000000 + "Mbit/s" +
                '}';
    }
}
