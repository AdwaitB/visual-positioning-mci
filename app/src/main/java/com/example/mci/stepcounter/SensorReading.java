package com.example.mci.stepcounter;

public class SensorReading {
    private final Long timestamp;
    private final Double magnitude;

    public SensorReading(Long timestamp, Double magnitude) {
        this.timestamp = timestamp;
        this.magnitude = magnitude;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Double getMagnitude() {
        return magnitude;
    }
}
