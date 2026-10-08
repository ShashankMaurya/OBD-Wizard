package com.diy.obdwizard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.diy.obdwizard.bluetooth.BluetoothConnection;
import com.diy.obdwizard.obd.RePidEngine;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;
import com.diy.obdwizard.trip.TripManager;
import com.diy.obdwizard.trip.TripRecord;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ExampleUnitTest {

    @Test
    public void testRePidEnginePollTelemetryThrowsIOExceptionOnDisconnect() {
        BluetoothConnection mockConn = new BluetoothConnection(null) {
            @Override
            public synchronized String sendCommand(String command) throws IOException {
                throw new IOException("Device disconnected");
            }

            @Override
            public boolean isConnected() {
                return false;
            }
        };

        RePidEngine engine = new RePidEngine(mockConn);
        assertFalse(engine.isConnected());

        try {
            engine.pollTelemetry();
            fail("Expected IOException when device is disconnected");
        } catch (IOException e) {
            assertEquals("Device disconnected", e.getMessage());
        }
    }

    @Test
    public void testBluetoothConnectionFailsWhenNotConnected() {
        BluetoothConnection conn = new BluetoothConnection(null);
        assertFalse(conn.isConnected());

        try {
            conn.sendCommand("0100");
            fail("Expected IOException when socket is null");
        } catch (IOException e) {
            assertNotNull(e.getMessage());
        }
        assertFalse(conn.isConnected());
    }

    @Test
    public void testTripManagerAutoSaveOnDisconnectSimulation() {
        TripManager tm = TripManager.getInstance();
        MotorcycleTelemetry telemetry = new MotorcycleTelemetry();
        telemetry.speedKmh = 40;
        telemetry.rpm = 2500;
        telemetry.sessionFuelConsumedLiters = 0.2;

        tm.startTrip(telemetry);
        Assert.assertTrue(tm.isRecording());

        tm.recordData(telemetry);

        // Simulate ECU/OBD disconnect event stopping the trip
        TripRecord record = tm.stopTrip(telemetry, null);
        assertNotNull(record);
        assertFalse(tm.isRecording());
    }
}