package de.tuberlin.aot.thesis.slienau.orchestrator;

import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.NetworkUplink;
import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

import java.time.LocalDateTime;

public class NodeRedNetworkUplink extends NetworkUplink {

    private LocalDateTime measurementTime;

    /**
     * @param source        source FogNode
     * @param destination   destination FogNode
     * @param latency       in milliseconds
     * @param mbitPerSecond bandwidth in Mbit/s
     */
    public NodeRedNetworkUplink(NodeRedFogNode source, NodeRedFogNode destination, int latency, double mbitPerSecond) {
        super(source, destination, latency, SchedulerUtils.mbitToBit(mbitPerSecond));
        updateMeasurementTime();
    }

    private void updateMeasurementTime() {
        measurementTime = LocalDateTime.now();
    }

    public LocalDateTime getMeasurementTime() {
        return measurementTime;
    }

    public void remeasure() {
        NodeRedFogNode source = (NodeRedFogNode) getSource();
        NodeRedFogNode destination = (NodeRedFogNode) getDestination();
        double newMbitPerSecond = source.measureBandwidthTo(destination.getAddress());
        int newLatency = source.measureLatencyTo(destination.getNodeRedController().getIp());
        this.setMbitPerSecond(newMbitPerSecond);
        this.setLatency(newLatency);
        updateMeasurementTime();
    }

    @Override
    public String toString() {
        return "NodeRedNetworkUplink{" +
                "source=" + getSource().getId() +
                ", destination=" + getDestination().getId() +
                ", latency=" + getLatency() +
                ", bandwidth=" + getMBitPerSecond() + "Mbit/s" +
                ", measurementTime=" + measurementTime +
                '}';
    }
}
