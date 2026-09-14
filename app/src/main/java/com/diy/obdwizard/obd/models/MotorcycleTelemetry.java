package com.diy.obdwizard.obd.models;

public class MotorcycleTelemetry {
    public int rpm = 0;
    public int speedKmh = 0;
    public int engineTempC = 0;
    public double throttlePercent = 0.0;
    public String calculatedGear = "N";

    public int mapKpa = 0;
    public int iatTempC = 0;
    public double o2Voltage = 0.0;
    public double controlModuleVoltage = 0.0;
    public double engineLoadPercent = 0.0;
    public double timingAdvance = 0.0;
    public int runTimeSeconds = 0;
    public int distanceWithMilKm = 0;
    public double fuelLevelPercent = -1.0; // PID 012F Fuel Tank Level Input (-1.0 if unsupported)

    // Derived / Calculated fields
    public double fuelEconomyKml = 0.0;
    public double averageFuelEconomyKml = 0.0;
    public double sessionFuelConsumedLiters = 0.0;
    public boolean isBatteryChargingPoor = false;
    public String mixtureStatus = "-";
    public double zeroToFortyTime = 0.0;
    public double zeroToSixtyTime = 0.0;
    public double zeroToEightyTime = 0.0;
    public double zeroToHundredTime = 0.0;

    public MotorcycleTelemetry() {
    }
}
