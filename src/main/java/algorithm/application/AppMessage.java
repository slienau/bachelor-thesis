package algorithm.application;

import algorithm.Utils;
import algorithm.infrastructure.FogNode;
import algorithm.infrastructure.NetworkUplink;

public class AppMessage {
    private final String content;
    private final AppModule source; // sending module
    private final AppModule destination; // receiving module
    private final double dataPerMessage; // kb

    AppMessage(String content, AppModule source, AppModule destination, double dataPerMessage) {
        this.content = content;
        this.source = source;
        this.destination = destination;
        this.dataPerMessage = dataPerMessage;
    }

    public AppModule getSource() {
        return source;
    }

    public AppModule getDestination() {
        return destination;
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
                ", source=" + source.getId() +
                ", destination=" + destination.getId() +
                ", dataPerMessage=" + dataPerMessage +
                '}';
    }
}
