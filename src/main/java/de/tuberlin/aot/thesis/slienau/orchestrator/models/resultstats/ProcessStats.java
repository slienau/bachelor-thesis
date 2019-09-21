package de.tuberlin.aot.thesis.slienau.orchestrator.models.resultstats;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessStats {
    @JsonProperty
    private String process;

    @JsonProperty
    private String node;

    @JsonProperty
    private int time;

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "ProcessStats{" +
                "process='" + process + '\'' +
                ", node='" + node + '\'' +
                ", time=" + time +
                '}';
    }
}
