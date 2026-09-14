package com.diy.obdwizard.obd.interfaces;

import com.diy.obdwizard.obd.models.MotorcycleTelemetry;

import java.util.List;

public interface ObdManagerCallbacks {

    interface ConnectionListener {
        void onConnecting();

        void onConnectionStateChanged(boolean isConnected);

        void onTelemetryDataReceived(MotorcycleTelemetry data);

        void onError(String message);
    }

    interface DtcReadListener {
        void onDtcRead(List<String> codes);

        void onError(String error);
    }

    interface DtcClearListener {
        void onDtcCleared(boolean success);
    }
}
