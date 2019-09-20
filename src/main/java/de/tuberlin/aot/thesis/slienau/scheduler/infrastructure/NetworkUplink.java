package de.tuberlin.aot.thesis.slienau.scheduler.infrastructure;

import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

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

    public FogNode getSource() {
        return source;
    }

    public FogNode getDestination() {
        return destination;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public long getBitPerSecond() {
        return bitPerSecond;
    }

    public void setBitPerSecond(long bitPerSecond) {
        this.bitPerSecond = bitPerSecond;
    }

    public void setMbitPerSecond(double mbitPerSecond) {
        this.setBitPerSecond(SchedulerUtils.mbitToBit(mbitPerSecond));
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
