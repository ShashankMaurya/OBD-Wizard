package com.diy.obdwizard.obd.interfaces;

import com.diy.obdwizard.obd.models.MotorcycleTelemetry;

import java.io.IOException;
import java.util.List;

public interface IObdDataSource {
    boolean connect();

    void disconnect();

    boolean isConnected();

    MotorcycleTelemetry pollTelemetry();

    List<String> readDtcCodes() throws IOException;

    boolean clearDtcCodes() throws IOException;
}
