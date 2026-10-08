package com.diy.obdwizard.trip;

import android.content.Context;

import com.diy.obdwizard.obd.models.MotorcycleTelemetry;
import com.diy.obdwizard.trip.db.TripDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripManager {
    private static TripManager instance;
    private TripRecord currentTrip;
    private boolean isRecording = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Integration helpers
    private double startFuel = 0;
    private final double startDistance = 0;
    private MotorcycleTelemetry lastTelemetry = null;

    public MotorcycleTelemetry getLastTelemetry() {
        return lastTelemetry;
    }

    public static synchronized TripManager getInstance() {
        if (instance == null) {
            instance = new TripManager();
        }
        return instance;
    }

    public void startTrip(MotorcycleTelemetry initialData) {
        currentTrip = new TripRecord(System.currentTimeMillis());
        isRecording = true;
        startFuel = initialData.sessionFuelConsumedLiters;
        lastTelemetry = initialData;
        // We don't have a direct 'totalDistance' field in telemetry that is global, 
        // but sessionDistanceKm in RePidEngine is integrated. 
        // For simplicity, we'll track the delta of sessionFuelConsumedLiters.
    }

    public TripRecord stopTrip(MotorcycleTelemetry finalData, Context context) {
        if (!isRecording || currentTrip == null) return null;

        currentTrip.endTimeMs = System.currentTimeMillis();
        currentTrip.durationMs = currentTrip.endTimeMs - currentTrip.startTimeMs;
        currentTrip.sessionFuelConsumedLiters = finalData.sessionFuelConsumedLiters - startFuel;

        // Finalize stats
        if (!currentTrip.snapshots.isEmpty()) {
            long durationSec = (currentTrip.endTimeMs - currentTrip.startTimeMs) / 1000;
            if (durationSec > 0) {
                double totalSpeed = 0;
                double totalRpm = 0;
                double totalThrottle = 0;
                double totalLoad = 0;
                double totalEconomy = 0;

                for (TripRecord.TelemetrySnapshot s : currentTrip.snapshots) {
                    totalSpeed += s.speedKmh;
                    totalRpm += s.rpm;
                    totalThrottle += s.throttlePercent;
                    totalLoad += s.engineLoadPercent;
                    totalEconomy += s.fuelEconomyKml;
                }
                int count = currentTrip.snapshots.size();
                currentTrip.avgSpeedKmh = totalSpeed / count;
                currentTrip.avgRpm = totalRpm / count;
                currentTrip.avgThrottlePercent = totalThrottle / count;
                currentTrip.avgLoadPercent = totalLoad / count;
                currentTrip.avgFuelEconomyKml = totalEconomy / count;
                currentTrip.totalDistanceKm = (currentTrip.avgSpeedKmh * durationSec) / 3600.0;
            }
        }

        isRecording = false;
        TripRecord finishedTrip = currentTrip;
        currentTrip = null;

        // Save to database
        if (context != null) {
            executor.execute(() -> {
                TripDatabase.getDatabase(context).tripDao().insert(finishedTrip);
            });
        }

        return finishedTrip;
    }

    public void recordData(MotorcycleTelemetry data) {
        if (!isRecording || currentTrip == null) return;
        this.lastTelemetry = data;

        TripRecord.TelemetrySnapshot snapshot = new TripRecord.TelemetrySnapshot(
                System.currentTimeMillis(), data.speedKmh, data.rpm, data.engineTempC);

        snapshot.throttlePercent = data.throttlePercent;
        snapshot.mapKpa = data.mapKpa;
        snapshot.iatTempC = data.iatTempC;
        snapshot.o2Voltage = data.o2Voltage;
        snapshot.controlModuleVoltage = data.controlModuleVoltage;
        snapshot.engineLoadPercent = data.engineLoadPercent;
        snapshot.timingAdvance = data.timingAdvance;
        snapshot.calculatedGear = data.calculatedGear;
        snapshot.fuelEconomyKml = data.fuelEconomyKml;

        currentTrip.snapshots.add(snapshot);

        // Update Peak values
        if (data.speedKmh > currentTrip.maxSpeedKmh) currentTrip.maxSpeedKmh = data.speedKmh;
        if (data.rpm > currentTrip.maxRpm) currentTrip.maxRpm = data.rpm;
        if (data.engineTempC > currentTrip.maxEngineTempC)
            currentTrip.maxEngineTempC = data.engineTempC;
        if (data.throttlePercent > currentTrip.maxThrottlePercent)
            currentTrip.maxThrottlePercent = data.throttlePercent;
        if (data.engineLoadPercent > currentTrip.maxLoadPercent)
            currentTrip.maxLoadPercent = data.engineLoadPercent;
        if (data.controlModuleVoltage > currentTrip.maxVoltage)
            currentTrip.maxVoltage = data.controlModuleVoltage;

        // Update Minimums
        if (currentTrip.minVoltage == 0 || data.controlModuleVoltage < currentTrip.minVoltage) {
            currentTrip.minVoltage = data.controlModuleVoltage;
        }

        // Update best performance timers for this trip
        updateBestTime(data.zeroToFortyTime, 40);
        updateBestTime(data.zeroToSixtyTime, 60);
        updateBestTime(data.zeroToEightyTime, 80);
        updateBestTime(data.zeroToHundredTime, 100);
    }

    private void updateBestTime(double time, int target) {
        if (time <= 0) return;
        switch (target) {
            case 40:
                if (currentTrip.best0to40 == -1 || time < currentTrip.best0to40)
                    currentTrip.best0to40 = time;
                break;
            case 60:
                if (currentTrip.best0to60 == -1 || time < currentTrip.best0to60)
                    currentTrip.best0to60 = time;
                break;
            case 80:
                if (currentTrip.best0to80 == -1 || time < currentTrip.best0to80)
                    currentTrip.best0to80 = time;
                break;
            case 100:
                if (currentTrip.best0to100 == -1 || time < currentTrip.best0to100)
                    currentTrip.best0to100 = time;
                break;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public long getStartTimeMs() {
        return (currentTrip != null) ? currentTrip.startTimeMs : 0;
    }
}
