package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.NetworkUplink;
import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

import java.io.IOException;
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

        int oldLatency = this.getLatency();
        double oldBandwidth = this.getMBitPerSecond();
        state = NetworkUplinkState.MEASURING;
        NodeRedFogNode source = (NodeRedFogNode) getSource();
        NodeRedFogNode destination = (NodeRedFogNode) getDestination();
        if (bandwidth) {
            double newMbitPerSecond = source.measureBandwidthTo(destination.getAddress());
            super.setMbitPerSecond(newMbitPerSecond);
        }
        if (latency) {
            try {
                int newLatency = source.measureLatencyTo(destination.getNodeRedController().getIp());
                super.setLatency(newLatency);
            } catch (IOException e) {
                System.err.println(String.format("[NodeRedNetworkUplink] Failed to measure latency from '%s' to '%s'", source.getId(), destination.getId()));
                e.printStackTrace();
            }

        }
        measurementTime = LocalDateTime.now();
        state = NetworkUplinkState.UP;
        System.out.println(String.format("[NodeRedNetworkUplink]['%s'->'%s'] Measured and updated uplink: Latency: %sms --> %sms; Bandwidth: %sMbit/s --> %sMbit/s", this.getSource().getId(), this.getDestination().getId(), oldLatency, this.getLatency(), oldBandwidth, this.getMBitPerSecond()));
    }

    @Override
    public void setMbitPerSecond(double mbitPerSecond) {
        if (state == NetworkUplinkState.MEASURING)
            return;
        this.setBitPerSecond(SchedulerUtils.mbitToBit(mbitPerSecond));
    }

    public NetworkUplinkState getState() {
        return state;
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
