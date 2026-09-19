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
    public double sessionFuelConsumedLiters;
    public int maxSpeedKmh;
    public int maxRpm;
    public double avgRpm;
    public double avgSpeedKmh;

    // Performance timers (best for this trip)
    public double best0to40 = -1;
    public double best0to60 = -1;
    public double best0to80 = -1;
    public double best0to100 = -1;

    // Additional Summary/Aggregate Fields aligned with MotorcycleTelemetry
    public int maxEngineTempC;
    public double maxThrottlePercent;
    public double avgThrottlePercent;
    public double maxLoadPercent;
    public double avgLoadPercent;
    public double minVoltage = 0;
    public double maxVoltage;
    public double avgFuelEconomyKml;

    // Time-series data for visualization
    public List<TelemetrySnapshot> snapshots = new ArrayList<>();

    public TripRecord(long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public static class TelemetrySnapshot implements Serializable {
        public long timestampMs;
        public int speedKmh;
        public int rpm;
        public int engineTempC;
        public double throttlePercent;
        public int mapKpa;
        public int iatTempC;
        public double o2Voltage;
        public double controlModuleVoltage;
        public double engineLoadPercent;
        public double timingAdvance;
        public String calculatedGear;
        public double fuelEconomyKml;
        public double fuelFlow; // Keep if still useful, though sessionFuelConsumedLiters is primary

        public TelemetrySnapshot() {
        }

        public TelemetrySnapshot(long timestampMs, int speedKmh, int rpm, int engineTempC) {
            this.timestampMs = timestampMs;
            this.speedKmh = speedKmh;
            this.rpm = rpm;
            this.engineTempC = engineTempC;
        }
    }
}
