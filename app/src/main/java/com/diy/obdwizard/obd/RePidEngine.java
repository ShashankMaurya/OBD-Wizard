package com.diy.obdwizard.obd;

import android.content.Context;

import com.diy.obdwizard.bluetooth.BluetoothConnection;
import com.diy.obdwizard.dtc.DtcHelper;
import com.diy.obdwizard.obd.interfaces.IObdDataSource;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;
import com.diy.obdwizard.trip.ProfileManager;

import java.io.IOException;
import java.util.List;

public class RePidEngine implements IObdDataSource {
    private final BluetoothConnection connection;
    private final Context context;

    // State for performance timers
    private boolean isTiming0To40 = false;
    private long timer0To40StartMs = 0;
    private double last0To40Time = 0.0;

    private boolean isTiming0To60 = false;
    private long timer0To60StartMs = 0;
    private double last0To60Time = 0.0;

    private boolean isTiming0To80 = false;
    private long timer0To80StartMs = 0;
    private double last0To80Time = 0.0;

    private boolean isTiming0To100 = false;
    private long timer0To100StartMs = 0;
    private double last0To100Time = 0.0;

    // State for integration (Fuel/Distance)
    private long lastPollTimeMs = 0;
    private double sessionFuelConsumedLiters = 0.0;
    private double sessionDistanceKm = 0.0;

    public RePidEngine(BluetoothConnection connection, Context context) {
        this.connection = connection;
        this.context = context;
    }

    public RePidEngine(BluetoothConnection connection) {
        this.connection = connection;
        this.context = null;
    }

    @Override
    public boolean connect() {
        if (!connection.connect()) return false;
        return initializeElm();
    }

    private boolean initializeElm() {
        try {
            // ATZ: Reset ELM327 adapter to defaults
            connection.sendCommand("ATZ");
            // ATE0: Echo off (disable echoing of characters sent)
            connection.sendCommand("ATE0");
            // ATL0: Linefeeds off (disable linefeed characters in responses)
            connection.sendCommand("ATL0");
            // ATS0: Spaces off (remove whitespace separators in hex strings)
            connection.sendCommand("ATS0");
            // ATH0: Headers off (hide OBD header/CAN ID bytes)
            connection.sendCommand("ATH0");
            // ATSP0: Set protocol to Automatic search mode
            connection.sendCommand("ATSP0");
            // Mode 01 PID 00: Request supported PIDs (01-20) to probe OBD-II connection
            String probe = connection.sendCommand("0100");
            return probe.contains("4100") || probe.contains("41 00");
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void disconnect() {
        connection.disconnect();
    }

    @Override
    public boolean isConnected() {
        return connection.isConnected();
    }

    @Override
    public MotorcycleTelemetry pollTelemetry() throws IOException {
        MotorcycleTelemetry data = new MotorcycleTelemetry();
        // Base PIDs
        String rpmRaw = connection.sendCommand("010C");
        String speedRaw = connection.sendCommand("010D");
        String throttleRaw = connection.sendCommand("0111");
        String tempRaw = connection.sendCommand("0105");

        // New PIDs
        String mapRaw = connection.sendCommand("010B");
        String iatRaw = connection.sendCommand("010F");
        String o2VoltsRaw = connection.sendCommand("0114");
        String controlVoltsRaw = connection.sendCommand("0142");
        String loadRaw = connection.sendCommand("0104");
        String timingRaw = connection.sendCommand("010E");
        String runTimeRaw = connection.sendCommand("011F");
        String distMilRaw = connection.sendCommand("0121");
        String fuelLevelRaw = connection.sendCommand("012F");

        data.rpm = ObdParser.parseRpm(rpmRaw);
        data.speedKmh = ObdParser.parseSpeed(speedRaw);
        data.throttlePercent = ObdParser.parseThrottle(throttleRaw);
        data.engineTempC = ObdParser.parseTemp(tempRaw);
        data.calculatedGear = ObdParser.calculateGear(data.speedKmh, data.rpm);

        data.mapKpa = ObdParser.parseMap(mapRaw);
        data.iatTempC = ObdParser.parseIat(iatRaw);
        data.o2Voltage = ObdParser.parseO2Voltage(o2VoltsRaw);
        data.controlModuleVoltage = ObdParser.parseControlModuleVoltage(controlVoltsRaw);
        data.engineLoadPercent = ObdParser.parseEngineLoad(loadRaw);
        data.timingAdvance = ObdParser.parseTimingAdvance(timingRaw);
        data.runTimeSeconds = ObdParser.parseRunTime(runTimeRaw);
        data.distanceWithMilKm = ObdParser.parseDistanceMil(distMilRaw);
        data.fuelLevelPercent = ObdParser.parseFuelLevel(fuelLevelRaw);

        // Calculate derived fields
        double displacementLiters = 0.349;
        if (context != null) {
            displacementLiters = ProfileManager.getInstance(context).getActiveDisplacementLiters(context);
        }

        data.fuelEconomyKml = ObdParser.calculateFuelEconomy(data.rpm, data.mapKpa, data.iatTempC, data.speedKmh, displacementLiters);
        data.isBatteryChargingPoor = ObdParser.checkBatteryHealth(data.rpm, data.controlModuleVoltage);
        data.mixtureStatus = ObdParser.evaluateMixture(data.o2Voltage);

        // Fuel Consumption and Distance Integration
        long currentTimeMs = System.currentTimeMillis();
        long dtMs = (lastPollTimeMs == 0) ? 0 : (currentTimeMs - lastPollTimeMs);
        lastPollTimeMs = currentTimeMs;

        // Cap dt to 2 seconds to avoid massive jumps if app paused or polling delayed
        if (dtMs > 0 && dtMs < 2000) {
            double dtHours = dtMs / 3600000.0;
            double fuelFlowLph = ObdParser.calculateFuelFlowLph(data.rpm, data.mapKpa, data.iatTempC, displacementLiters);
            sessionFuelConsumedLiters += fuelFlowLph * dtHours;
            sessionDistanceKm += data.speedKmh * dtHours;
        }

        data.sessionFuelConsumedLiters = sessionFuelConsumedLiters;
        if (sessionFuelConsumedLiters > 0.001) {
            data.averageFuelEconomyKml = sessionDistanceKm / sessionFuelConsumedLiters;
        } else {
            data.averageFuelEconomyKml = 0.0;
        }

        // 0-40 Timer Logic
        if (data.speedKmh == 0) {
            isTiming0To40 = false;
            isTiming0To60 = false;
            isTiming0To80 = false;
            isTiming0To100 = false;
        }

        if (data.speedKmh > 0 && data.speedKmh < 40 && !isTiming0To40) {
            isTiming0To40 = true;
            timer0To40StartMs = System.currentTimeMillis();
        } else if (data.speedKmh >= 40 && isTiming0To40) {
            isTiming0To40 = false;
            last0To40Time = (System.currentTimeMillis() - timer0To40StartMs) / 1000.0;
        }

        if (data.speedKmh > 0 && data.speedKmh < 60 && !isTiming0To60) {
            isTiming0To60 = true;
            timer0To60StartMs = System.currentTimeMillis();
        } else if (data.speedKmh >= 60 && isTiming0To60) {
            isTiming0To60 = false;
            last0To60Time = (System.currentTimeMillis() - timer0To60StartMs) / 1000.0;
        }

        if (data.speedKmh > 0 && data.speedKmh < 80 && !isTiming0To80) {
            isTiming0To80 = true;
            timer0To80StartMs = System.currentTimeMillis();
        } else if (data.speedKmh >= 80 && isTiming0To80) {
            isTiming0To80 = false;
            last0To80Time = (System.currentTimeMillis() - timer0To80StartMs) / 1000.0;
        }

        if (data.speedKmh > 0 && data.speedKmh < 100 && !isTiming0To100) {
            isTiming0To100 = true;
            timer0To100StartMs = System.currentTimeMillis();
        } else if (data.speedKmh >= 100 && isTiming0To100) {
            isTiming0To100 = false;
            last0To100Time = (System.currentTimeMillis() - timer0To100StartMs) / 1000.0;
        }

        data.zeroToFortyTime = last0To40Time;
        data.zeroToSixtyTime = last0To60Time;
        data.zeroToEightyTime = last0To80Time;
        data.zeroToHundredTime = last0To100Time;

        return data;
    }

    @Override
    public List<String> readDtcCodes() throws IOException {
        // Mode 03: Request Stored Diagnostic Trouble Codes (DTCs)
        String raw = connection.sendCommand("03");
        return DtcHelper.parseMode03Response(raw);
    }

    @Override
    public boolean clearDtcCodes() throws IOException {
        // Mode 04: Clear Diagnostic Trouble Codes / Reset Malfunction Indicator Lamp (MIL)
        String res = connection.sendCommand("04");
        return DtcHelper.isClearSuccessful(res);
    }
}
