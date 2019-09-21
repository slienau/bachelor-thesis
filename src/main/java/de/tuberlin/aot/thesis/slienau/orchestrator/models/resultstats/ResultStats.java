package de.tuberlin.aot.thesis.slienau.orchestrator.models.resultstats;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResultStats {
    @JsonProperty
    private List<TransferStat> transfers;

    @JsonProperty
    private List<ProcessStats> processes;

    @JsonProperty
    private int totalTransferTime;

    @JsonProperty
    private int totalProcessingTime;

    @JsonProperty
    private int totalTime;

    public List<TransferStat> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<TransferStat> transfers) {
        this.transfers = transfers;
    }

    public List<ProcessStats> getProcesses() {
        return processes;
    }

    public void setProcesses(List<ProcessStats> processes) {
        this.processes = processes;
    }

    public int getTotalTransferTime() {
        return totalTransferTime;
    }

    public void setTotalTransferTime(int totalTransferTime) {
        this.totalTransferTime = totalTransferTime;
    }

    public int getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public void setTotalProcessingTime(int totalProcessingTime) {
        this.totalProcessingTime = totalProcessingTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return "ResultStats{" +
                "transfers=" + transfers +
                ", processes=" + processes +
                ", totalTransferTime=" + totalTransferTime +
                ", totalProcessingTime=" + totalProcessingTime +
                ", totalTime=" + totalTime +
                '}';
    }
}
