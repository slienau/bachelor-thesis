package de.tuberlin.aot.thesis.slienau.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class SystemInfo {
    @JsonProperty
    private String deviceName;

    @JsonProperty
    private LocalDateTime timestamp;

    @JsonProperty
    private String arch;

    @JsonProperty
    private Float totalMem;

    @JsonProperty
    private Float freeMem;

    @JsonProperty
    private Float totalDisk;

    @JsonProperty
    private Float freeDisk;

    @JsonProperty
    private short cpuCount;

    @JsonProperty
    private Float loadAvg1;

    @JsonProperty
    private Float loadAvg5;

    @JsonProperty
    private Float loadAvg15;

    @JsonProperty
    private List<String> connectedHardware;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        // not implemented
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Float getTotalMem() {
        return totalMem;
    }

    public void setTotalMem(Float totalMem) {
        this.totalMem = totalMem;
    }

    public Float getFreeMem() {
        return freeMem;
    }

    public void setFreeMem(Float freeMem) {
        this.freeMem = freeMem;
    }

    public short getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(short cpuCount) {
        this.cpuCount = cpuCount;
    }

    public Float getLoadAvg1() {
        return loadAvg1;
    }

    public void setLoadAvg1(Float loadAvg1) {
        this.loadAvg1 = loadAvg1;
    }

    public Float getLoadAvg5() {
        return loadAvg5;
    }

    public void setLoadAvg5(Float loadAvg5) {
        this.loadAvg5 = loadAvg5;
    }

    public Float getLoadAvg15() {
        return loadAvg15;
    }

    public void setLoadAvg15(Float loadAvg15) {
        this.loadAvg15 = loadAvg15;
    }

    public List<String> getConnectedHardware() {
        return connectedHardware;
    }

    public void setConnectedHardware(List<String> connectedHardware) {
        this.connectedHardware = connectedHardware;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public Float getTotalDisk() {
        return totalDisk;
    }

    public void setTotalDisk(Float totalDisk) {
        this.totalDisk = totalDisk;
    }

    public Float getFreeDisk() {
        return freeDisk;
    }

    public void setFreeDisk(Float freeDisk) {
        this.freeDisk = freeDisk;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "deviceName='" + deviceName + '\'' +
                ", timestamp=" + timestamp +
                ", arch='" + arch + '\'' +
                ", totalMem=" + totalMem +
                ", freeMem=" + freeMem +
                ", totalDisk=" + totalDisk +
                ", freeDisk=" + freeDisk +
                ", cpuCount=" + cpuCount +
                ", loadAvg1=" + loadAvg1 +
                ", loadAvg5=" + loadAvg5 +
                ", loadAvg15=" + loadAvg15 +
                ", connectedHardware=" + connectedHardware +
                '}';
    }
}
