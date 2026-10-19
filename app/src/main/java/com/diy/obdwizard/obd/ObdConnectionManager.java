package com.diy.obdwizard.obd;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.diy.obdwizard.bluetooth.BluetoothConnection;
import com.diy.obdwizard.obd.interfaces.IObdDataSource;
import com.diy.obdwizard.obd.interfaces.ObdManagerCallbacks;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ObdConnectionManager {

    private final ExecutorService commandExecutor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService pollingExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ObdManagerCallbacks.ConnectionListener listener;
    private IObdDataSource obdSource;
    private volatile boolean isPolling = false;
    private volatile boolean isConnecting = false;
    private volatile boolean isPaused = false;

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

        commandExecutor.execute(() -> {
            BluetoothConnection bt = new BluetoothConnection(targetDevice);
            Context ctx = (listener instanceof Context) ? (Context) listener : null;
            obdSource = new RePidEngine(bt, ctx);
            boolean connected = obdSource.connect();
            isConnecting = false;
            if (connected) {
                isPolling = true;
                if (listener != null) listener.onConnectionStateChanged(true);
                scheduleNextPoll();
            } else {
                if (listener != null) {
                    listener.onError("Connection Failed");
                    listener.onConnectionStateChanged(false);
                }
            }
        });
    }

    private void scheduleNextPoll() {
        if (!isPolling) return;
        pollingExecutor.schedule(() -> {
            if (!isPolling) return;
            if (!isPaused && obdSource != null && obdSource.isConnected()) {
                try {
                    MotorcycleTelemetry data = obdSource.pollTelemetry();
                    if (listener != null) listener.onTelemetryDataReceived(data);
                } catch (IOException e) {
                    handleUnexpectedDisconnect("OBD device disconnected");
                    return;
                }
            }
            scheduleNextPoll();
        }, 60, TimeUnit.MILLISECONDS);
    }

    private void handleUnexpectedDisconnect(String errorMsg) {
        if (!isPolling) return;
        isPolling = false;
        isPaused = false;
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
        isPaused = false;
        commandExecutor.execute(() -> {
            if (obdSource != null) obdSource.disconnect();
            if (listener != null) listener.onConnectionStateChanged(false);
        });
    }

    public void readDtc(ObdManagerCallbacks.DtcReadListener callback) {
        if (!isConnected()) {
            if (callback != null) callback.onError("Connect to ECU first");
            return;
        }

        isPaused = true;
        commandExecutor.execute(() -> {
            try {
                // Wait briefly for any active poll to finish
                Thread.sleep(50);
                List<String> codes = obdSource.readDtcCodes();
                if (callback != null) callback.onDtcRead(codes);
            } catch (IOException e) {
                handleUnexpectedDisconnect("Device disconnected during DTC read");
                if (callback != null)
                    callback.onError("Failed to read DTC codes: Device disconnected");
            } catch (InterruptedException e) {
                if (callback != null) callback.onError("DTC read interrupted");
            } finally {
                isPaused = false;
            }
        });
    }

    public void clearDtc(ObdManagerCallbacks.DtcClearListener callback) {
        if (!isConnected()) {
            if (callback != null) callback.onDtcCleared(false);
            return;
        }

        isPaused = true;
        commandExecutor.execute(() -> {
            try {
                Thread.sleep(50);
                boolean cleared = obdSource.clearDtcCodes();
                if (callback != null) callback.onDtcCleared(cleared);
            } catch (IOException e) {
                handleUnexpectedDisconnect("Device disconnected during DTC clear");
                if (callback != null) callback.onDtcCleared(false);
            } catch (InterruptedException e) {
                if (callback != null) callback.onDtcCleared(false);
            } finally {
                isPaused = false;
            }
        });
    }

    public void executeInBackground(Runnable runnable) {
        commandExecutor.execute(runnable);
    }

    public void destroy() {
        disconnect();
        pollingExecutor.shutdown();
        commandExecutor.shutdown();
    }

    public boolean isConnected() {
        return isPolling && obdSource != null && obdSource.isConnected();
    }
}
