package de.tuberlin.aot.thesis.slienau.scheduler.application;

import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.FogNode;
import de.tuberlin.aot.thesis.slienau.scheduler.infrastructure.NetworkUplink;
import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

public class AppMessage {
    private final String contentType;
    private final double dataPerMessage; // kb

    AppMessage(String contentType, double dataPerMessage) {
        this.contentType = contentType;
        this.dataPerMessage = dataPerMessage;
    }

    public double getDataPerMessage() {
        return dataPerMessage;
    }

    public String getContentType() {
        return contentType;
    }

    public double calculateMessageTransferTime(FogNode sourceNode, FogNode destinationNode) {
        NetworkUplink uplink = sourceNode.getUplinkTo(destinationNode.getId());
        if (uplink == null) // no uplink from source to destination --> infinite transfer time
            return Double.MAX_VALUE;
        return SchedulerUtils.calculateTransferTime(uplink.getLatency(), uplink.getBitPerSecond(), this.getDataPerMessage());
    }

    public String createMessageTransferTimeString(FogNode sourceNode, FogNode destinationNode) {
        String transferStringTemplate = "%6sms Transfer time from '%s' to '%s' for message content '%s' (size: %sKB; bandwidth: %sMBit/s, RTT: %sms)";
        NetworkUplink uplink = sourceNode.getUplinkTo(destinationNode.getId());
        double messageTransferTime = this.calculateMessageTransferTime(sourceNode, destinationNode);
        return String.format(transferStringTemplate,
                messageTransferTime, sourceNode.getId(), destinationNode.getId(), this.getContentType(), this.getDataPerMessage(), uplink.getMBitPerSecond(), uplink.getLatency());
    }

    @Override
    public String toString() {
        return "AppMessage{" +
                "content='" + contentType + '\'' +
                ", dataPerMessage=" + dataPerMessage +
                '}';
    }
}
