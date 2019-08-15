package de.tuberlin.aot.thesis.slienau.orchestrator.jsontypes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class Heartbeat {
    @JsonProperty
    private String deviceName;

    @JsonProperty
    private LocalDateTime timestamp;

    @Override
    public String toString() {
        return "Heartbeat{" +
                "deviceName='" + deviceName + '\'' +
                ", timestamp='" + timestamp + '\'' +
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

}
