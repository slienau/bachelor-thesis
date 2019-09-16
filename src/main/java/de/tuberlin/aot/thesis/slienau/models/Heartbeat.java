package de.tuberlin.aot.thesis.slienau.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class Heartbeat {
    @JsonProperty
    private String deviceName;

    @JsonProperty
    private LocalDateTime timestamp;

    @JsonProperty
    private String publicFqdn;

    @JsonProperty
    private int publicPort;

    @Override
    public String toString() {
        return "Heartbeat{" +
                "deviceName='" + deviceName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", publicFqdn='" + publicFqdn + '\'' +
                ", publicPort='" + publicPort + '\'' +
                '}';
    }

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
        long longTimestamp = Long.valueOf(timestamp);
        this.timestamp =
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(longTimestamp),
                        TimeZone.getDefault().toZoneId()
                );
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPublicFqdn() {
        return publicFqdn;
    }

    public void setPublicFqdn(String publicFqdn) {
        this.publicFqdn = publicFqdn;
    }

    public int getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(int publicPort) {
        this.publicPort = publicPort;
    }
}
