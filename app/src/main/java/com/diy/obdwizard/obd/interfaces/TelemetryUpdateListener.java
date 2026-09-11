package com.diy.obdwizard.obd.interfaces;

import com.diy.obdwizard.obd.models.MotorcycleTelemetry;

public interface TelemetryUpdateListener {
    void onTelemetryData(MotorcycleTelemetry data);
}
