package com.diy.obdwizard.obd;

import android.bluetooth.BluetoothDevice;

import com.diy.obdwizard.bluetooth.BluetoothConnection;
import com.diy.obdwizard.obd.interfaces.IObdDataSource;
import com.diy.obdwizard.obd.interfaces.ObdManagerCallbacks;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObdConnectionManager {

    private final ExecutorService workerThread = Executors.newSingleThreadExecutor();
    private final ObdManagerCallbacks.ConnectionListener listener;
    private IObdDataSource obdSource;
    private volatile boolean isPolling = false;
    private volatile boolean isConnecting = false;

    public ObdConnectionManager(ObdManagerCallbacks.ConnectionListener listener) {
        this.listener = listener;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void connect(BluetoothDevice targetDevice) {
        if (isPolling || isConnecting) return;
        isConnecting = true;
        if (listener != null) listener.onConnecting();

        workerThread.execute(() -> {
            BluetoothConnection bt = new BluetoothConnection(targetDevice);
            obdSource = new RePidEngine(bt);
            boolean connected = obdSource.connect();
            isConnecting = false;
            if (connected) {
                isPolling = true;
                if (listener != null) listener.onConnectionStateChanged(true);
                runTelemetryLoop();
            } else {
                if (listener != null) {
                    listener.onError("Connection Failed");
                    listener.onConnectionStateChanged(false);
                }
            }
        });
    }

    private void runTelemetryLoop() {
        while (isPolling && obdSource != null && obdSource.isConnected()) {
            try {
                MotorcycleTelemetry data = obdSource.pollTelemetry();
                if (listener != null) listener.onTelemetryDataReceived(data);
            } catch (IOException e) {
                handleUnexpectedDisconnect("OBD device disconnected");
                break;
            }

            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                break;
            }
        }
        if (isPolling && (obdSource == null || !obdSource.isConnected())) {
            handleUnexpectedDisconnect("OBD device disconnected");
        }
    }

    private void handleUnexpectedDisconnect(String errorMsg) {
        if (!isPolling) return;
        isPolling = false;
        if (obdSource != null) {
            obdSource.disconnect();
        }
        if (listener != null) {
            listener.onError(errorMsg);
            listener.onConnectionStateChanged(false);
        }
    }

    public void disconnect() {
        isPolling = false;
        workerThread.execute(() -> {
            if (obdSource != null) obdSource.disconnect();
            if (listener != null) listener.onConnectionStateChanged(false);
        });
    }

    public void readDtc(ObdManagerCallbacks.DtcReadListener callback) {
        if (!isConnected()) {
            if (callback != null) callback.onError("Connect to ECU first");
            return;
        }

        workerThread.execute(() -> {
            try {
                List<String> codes = obdSource.readDtcCodes();
                if (callback != null) callback.onDtcRead(codes);
            } catch (IOException e) {
                handleUnexpectedDisconnect("Device disconnected during DTC read");
                if (callback != null)
                    callback.onError("Failed to read DTC codes: Device disconnected");
            }
        });
    }

    public void clearDtc(ObdManagerCallbacks.DtcClearListener callback) {
        workerThread.execute(() -> {
            try {
                boolean cleared = obdSource.clearDtcCodes();
                if (callback != null) callback.onDtcCleared(cleared);
            } catch (IOException e) {
                handleUnexpectedDisconnect("Device disconnected during DTC clear");
                if (callback != null) callback.onDtcCleared(false);
            }
        });
    }

    public void executeInBackground(Runnable runnable) {
        workerThread.execute(runnable);
    }

    public void destroy() {
        disconnect();
        workerThread.shutdown();
    }

    public boolean isConnected() {
        return isPolling && obdSource != null && obdSource.isConnected();
    }
}
