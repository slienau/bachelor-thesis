package de.tuberlin.aot.thesis.slienau.models.resultstats;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ResultStats {
    @JsonProperty
    private List<TransferStat> transfers;

    @JsonProperty
    private List<ProcessStats> processes;

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

    @Override
    public String toString() {
        return "ResultStats{" +
                "transfers=" + transfers +
                ", processes=" + processes +
                '}';
    }
}
