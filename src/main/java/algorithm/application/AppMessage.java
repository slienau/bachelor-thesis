package algorithm.application;

import algorithm.Utils;
import algorithm.infrastructure.FogNode;
import algorithm.infrastructure.NetworkUplink;

public class AppMessage {
    private final String content;
    private final String sourceModuleId; // sending module
    private final String destinationModuleId; // receiving module
    private final double dataPerMessage; // kb

    AppMessage(String content, String sourceModuleId, String destinationModuleId, double dataPerMessage) {
        this.content = content;
        this.sourceModuleId = sourceModuleId;
        this.destinationModuleId = destinationModuleId;
        this.dataPerMessage = dataPerMessage;
    }

    public String getSourceModuleId() {
        return sourceModuleId;
    }

    public String getDestinationModuleId() {
        return destinationModuleId;
    }

    public double getDataPerMessage() {
        return dataPerMessage;
    }

    public String getContent() {
        return content;
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
                messageTransferTime, sourceNode.getId(), destinationNode.getId(), this.getContent(), this.getDataPerMessage(), uplink.getMBitPerSecond(), uplink.getLatency());
    }

    @Override
    public String toString() {
        return "AppMessage{" +
                "content='" + content + '\'' +
                ", sourceModule=" + sourceModuleId +
                ", destinationModule=" + destinationModuleId +
                ", dataPerMessage=" + dataPerMessage +
                '}';
    }
}
