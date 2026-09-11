package com.diy.obdwizard.obd;

import com.diy.obdwizard.bluetooth.BluetoothConnection;
import com.diy.obdwizard.dtc.DtcHelper;
import com.diy.obdwizard.obd.interfaces.IObdDataSource;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;

import java.io.IOException;
import java.util.List;

public class RePidEngine implements IObdDataSource {
    private final BluetoothConnection connection;

    public RePidEngine(BluetoothConnection connection) {
        this.connection = connection;
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
