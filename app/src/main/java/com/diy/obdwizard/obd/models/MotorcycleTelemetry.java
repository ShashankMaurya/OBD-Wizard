package com.diy.obdwizard.obd.models;

public class MotorcycleTelemetry {
    public int rpm = 0;
    public int speedKmh = 0;
    public int engineTempC = 0;
    public double throttlePercent = 0.0;
    public String calculatedGear = "N";

    public MotorcycleTelemetry() {
    }

    public MotorcycleTelemetry(int rpm, int speedKmh, int engineTempC, double throttlePercent, String calculatedGear) {
        this.rpm = rpm;
        this.speedKmh = speedKmh;
        this.engineTempC = engineTempC;
        this.throttlePercent = throttlePercent;
        this.calculatedGear = calculatedGear;
    }
}
