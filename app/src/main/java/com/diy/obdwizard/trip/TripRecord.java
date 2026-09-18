package com.diy.obdwizard.trip;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "trips")
public class TripRecord implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long startTimeMs;
    public long endTimeMs;
    public long durationMs;
    public double totalDistanceKm;
    public double fuelConsumedL;
    public int maxSpeedKmh;
    public int maxRpm;
    public double avgRpm;
    public double avgSpeedKmh;

    // Performance timers (best for this trip)
    public double best0to40 = -1;
    public double best0to60 = -1;
    public double best0to80 = -1;
    public double best0to100 = -1;

    // Time-series data for visualization
    public List<TelemetrySnapshot> snapshots = new ArrayList<>();

    public TripRecord(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public static class TelemetrySnapshot implements Serializable {
        public long timestampMs;
        public int speed;
        public int rpm;
        public int engineTemp;
        public double fuelFlow;

        public TelemetrySnapshot() {
        }

        public TelemetrySnapshot(long timestampMs, int speed, int rpm, int engineTemp) {
            this.timestampMs = timestampMs;
            this.speed = speed;
            this.rpm = rpm;
            this.engineTemp = engineTemp;
        }
    }
}
