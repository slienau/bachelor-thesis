package de.tuberlin.aot.thesis.slienau.models.resultstats;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.tuberlin.aot.thesis.slienau.utils.SchedulerUtils;

public class TransferStat {
    @JsonProperty
    private String messageType;

    @JsonProperty
    private int size;

    @JsonProperty
    private String sourceNode;

    @JsonProperty
    private String destinationNode;

    @JsonProperty
    /**
     * Time in milliseconds
     */
    private int time;

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * @return message size in Byte
     */
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getSizeInBits() {
        return size * 8;
    }

    public double getSizeInKByte() {
        return size / 1024;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getDestinationNode() {
        return destinationNode;
    }

    public void setDestinationNode(String destinationNode) {
        this.destinationNode = destinationNode;
    }

    /**
     * Message transfer time in milliseconds
     *
     * @return
     */
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getTimeInSeconds() {
        return (double) time / 1000;
    }

    public double getMbitPerSecond() {
        double mbit = SchedulerUtils.bitToMbit(this.getSizeInBits());
        return mbit / this.getTimeInSeconds();
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "messageType='" + messageType + '\'' +
                ", size=" + size +
                ", sourceNode='" + sourceNode + '\'' +
                ", destinationNode='" + destinationNode + '\'' +
                ", time=" + time +
                '}';
    }
}
