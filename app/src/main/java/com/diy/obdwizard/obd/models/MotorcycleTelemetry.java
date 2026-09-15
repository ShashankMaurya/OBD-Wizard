package com.diy.obdwizard.obd.models;

/**
 * Data model representing a comprehensive snapshot of motorcycle telemetry.
 * Contains both raw sensor data parsed from the ECU and derived performance metrics.
 */
public class MotorcycleTelemetry {

    // --- Standard OBD-II Sensor Data ---

    /**
     * Engine Revolutions Per Minute (RPM). PID: 010C. Range: 0 - 16383.75 RPM.
     */
    public int rpm = 0;

    /** Vehicle Speed in Kilometers per Hour (km/h). PID: 010D. Range: 0 - 255 km/h. */
    public int speedKmh = 0;

    /** Engine Coolant (or Oil) Temperature in Celsius (°C). PID: 0105. Range: -40 to 215 °C. */
    public int engineTempC = 0;

    /** Absolute Throttle Position percentage. PID: 0111. Range: 0% to 100%. */
    public double throttlePercent = 0.0;

    /** Intake Manifold Absolute Pressure (MAP) in kilopascals (kPa). PID: 010B. Range: 0 - 255 kPa. */
    public int mapKpa = 0;

    /** Intake Air Temperature (IAT) in Celsius (°C). PID: 010F. Range: -40 to 215 °C. */
    public int iatTempC = 0;

    /** O2 Sensor Bank 1 Sensor 1 Output Voltage in Volts (V). PID: 0114. Range: 0 - 1.275 V. */
    public double o2Voltage = 0.0;

    /** Control Module (ECU/Battery) Voltage in Volts (V). PID: 0142. Range: 0 - 65.535 V. */
    public double controlModuleVoltage = 0.0;

    /** Calculated Engine Load percentage. PID: 0104. Range: 0% to 100%. */
    public double engineLoadPercent = 0.0;

    /** Ignition Timing Advance for #1 cylinder in degrees (°). PID: 010E. Range: -64 to 63.5 °. */
    public double timingAdvance = 0.0;

    /** Engine Run Time since engine start in seconds (s). PID: 011F. Range: 0 - 65535 s. */
    public int runTimeSeconds = 0;

    /** Distance traveled with Malfunction Indicator Lamp (MIL) active in km. PID: 0121. Range: 0 - 65535 km. */
    public int distanceWithMilKm = 0;

    /**
     * Fuel Tank Level Input percentage. PID: 012F. Range: 0% to 100%. Defaults to -1.0 if unsupported.
     */
    public double fuelLevelPercent = -1.0;

    // --- Derived & Calculated Metrics ---

    /**
     * Current estimated transmission gear based on Speed/RPM ratio. Values: 1, N, 2, 3, 4, 5.
     */
    public String calculatedGear = "N";

    /** Instantaneous Fuel Economy in kilometers per liter (km/L). Calculated using Speed Density (MAP/RPM/IAT). */
    public double fuelEconomyKml = 0.0;

    /** Session Average Fuel Economy in kilometers per liter (km/L). Integrated over time. */
    public double averageFuelEconomyKml = 0.0;

    /** Total Fuel Consumed during the current session in liters (L). Integrated using estimated fuel flow rate. */
    public double sessionFuelConsumedLiters = 0.0;

    /** Battery Charging Health status. True if RPM > 1500 but voltage is critically low (suggests stator/regulator failure). */
    public boolean isBatteryChargingPoor = false;

    /** Air-Fuel Mixture status estimated from O2 voltage. Values: LEAN, OPTIMAL, RICH. */
    public String mixtureStatus = "-";

    /** Performance Timer: Time taken to reach 40 km/h from a standstill (s). */
    public double zeroToFortyTime = 0.0;

    /** Performance Timer: Time taken to reach 60 km/h from a standstill (s). */
    public double zeroToSixtyTime = 0.0;

    /** Performance Timer: Time taken to reach 80 km/h from a standstill (s). */
    public double zeroToEightyTime = 0.0;

    /** Performance Timer: Time taken to reach 100 km/h from a standstill (s). */
    public double zeroToHundredTime = 0.0;

    public MotorcycleTelemetry() {
    }
}
