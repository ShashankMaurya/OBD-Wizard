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
        // We don't have a direct 'totalDistance' field in telemetry that is global, 
        // but sessionDistanceKm in RePidEngine is integrated. 
        // For simplicity, we'll track the delta of sessionFuelConsumedLiters.
    }

    public TripRecord stopTrip(MotorcycleTelemetry finalData, Context context) {
        if (!isRecording || currentTrip == null) return null;

        currentTrip.endTimeMs = System.currentTimeMillis();
        currentTrip.fuelConsumedL = finalData.sessionFuelConsumedLiters - startFuel;

        // Finalize stats
        if (!currentTrip.snapshots.isEmpty()) {
            long durationSec = (currentTrip.endTimeMs - currentTrip.startTimeMs) / 1000;
            if (durationSec > 0) {
                double totalSpeed = 0;
                for (TripRecord.TelemetrySnapshot s : currentTrip.snapshots) {
                    totalSpeed += s.speed;
                }
                currentTrip.avgSpeedKmh = totalSpeed / currentTrip.snapshots.size();
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

        currentTrip.snapshots.add(new TripRecord.TelemetrySnapshot(System.currentTimeMillis(), data.speedKmh, data.rpm));

        if (data.speedKmh > currentTrip.maxSpeedKmh) currentTrip.maxSpeedKmh = data.speedKmh;
        if (data.rpm > currentTrip.maxRpm) currentTrip.maxRpm = data.rpm;

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
}
