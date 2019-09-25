package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.NetworkUplink;
import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

import java.time.LocalDateTime;

public class NodeRedNetworkUplink extends NetworkUplink {

    private LocalDateTime measurementTime;
    private NetworkUplinkState state;

    /**
     * @param source        source FogNode
     * @param destination   destination FogNode
     * @param latency       in milliseconds
     * @param mbitPerSecond bandwidth in Mbit/s
     */
    public NodeRedNetworkUplink(NodeRedFogNode source, NodeRedFogNode destination, int latency, double mbitPerSecond) {
        super(source, destination, latency, SchedulerUtils.mbitToBit(mbitPerSecond));
        state = NetworkUplinkState.UP;
        measurementTime = LocalDateTime.now();
    }

    public NodeRedNetworkUplink(NodeRedFogNode source, NodeRedFogNode destination) {
        this(source, destination, Integer.MAX_VALUE, 0.0);
        state = NetworkUplinkState.INITIALIZED;
    }

    public LocalDateTime getMeasurementTime() {
        return measurementTime;
    }

    public void measure(boolean bandwidth, boolean latency) {
        if (state == NetworkUplinkState.MEASURING) // don't start a second measurement process if another one is still active
            return;
        state = NetworkUplinkState.MEASURING;
        NodeRedFogNode source = (NodeRedFogNode) getSource();
        NodeRedFogNode destination = (NodeRedFogNode) getDestination();
        if (bandwidth) {
            double newMbitPerSecond = source.measureBandwidthTo(destination.getAddress());
            super.setMbitPerSecond(newMbitPerSecond);
        }
        if (latency) {
            int newLatency = source.measureLatencyTo(destination.getNodeRedController().getIp());
            super.setLatency(newLatency);
        }
        measurementTime = LocalDateTime.now();
        state = NetworkUplinkState.UP;
    }

    @Override
    public void setMbitPerSecond(double mbitPerSecond) {
        if (state == NetworkUplinkState.MEASURING)
            return;
        this.setBitPerSecond(SchedulerUtils.mbitToBit(mbitPerSecond));
    }

    @Override
    public String toString() {
        return "NodeRedNetworkUplink{" +
                "source=" + getSource().getId() +
                ", destination=" + getDestination().getId() +
                ", latency=" + getLatency() +
                ", bandwidth=" + getMBitPerSecond() + "Mbit/s" +
                ", measurementTime=" + measurementTime +
                ", state=" + state +
                '}';
    }

    public enum NetworkUplinkState {
        INITIALIZED, MEASURING, UP
    }
}
