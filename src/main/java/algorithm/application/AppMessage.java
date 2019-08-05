package algorithm.application;

import algorithm.Utils;
import algorithm.infrastructure.FogNode;
import algorithm.infrastructure.NetworkUplink;

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
        return Utils.calculateTransferTime(uplink.getLatency(), uplink.getBitPerSecond(), this.getDataPerMessage());
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
