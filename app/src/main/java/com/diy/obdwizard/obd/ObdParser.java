package com.diy.obdwizard.obd;

/**
 * Utility class for parsing raw OBD-II PID responses and calculating derived telemetry values.
 * Standard OBD-II Mode 01 PID formulas are implemented based on the SAE J1979 standard.
 */
public class ObdParser {

    private ObdParser() {
        // Utility class
    }

    /**
     * Parses Mode 01 PID 0C (Engine RPM).
     * <p>
     * Formula: ((A * 256) + B) / 4
     * A and B are the first and second bytes of the data response.
     * Output Range: 0 to 16,383.75 RPM.
     */
    public static int parseRpm(String raw) {
        if (raw == null) return 0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("410C");
        if (idx != -1 && clean.length() >= idx + 8) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                int b = Integer.parseInt(clean.substring(idx + 6, idx + 8), 16);
                return ((a * 256) + b) / 4;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Parses Mode 01 PID 0D (Vehicle Speed).
     * <p>
     * Formula: A
     * A is the single data byte representing speed in km/h.
     * Output Range: 0 to 255 km/h.
     */
    public static int parseSpeed(String raw) {
        if (raw == null) return 0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("410D");
        if (idx != -1 && clean.length() >= idx + 6) {
            try {
                return Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Parses Mode 01 PID 11 (Throttle Position).
     * <p>
     * Formula: (A * 100) / 255
     * A is the data byte. Represents the percentage of throttle opening.
     * Output Range: 0% to 100%.
     */
    public static double parseThrottle(String raw) {
        if (raw == null) return 0.0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("4111");
        if (idx != -1 && clean.length() >= idx + 6) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                return (a * 100.0) / 255.0;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0.0;
    }

    /**
     * Parses Mode 01 PID 05 (Engine Coolant Temperature).
     * <p>
     * Formula: A - 40
     * A is the data byte.
     * Output Range: -40°C to 215°C.
     */
    public static int parseTemp(String raw) {
        if (raw == null) return 0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("4105");
        if (idx != -1 && clean.length() >= idx + 6) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                return a - 40;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Calculates the estimated gear based on the ratio between Vehicle Speed and Engine RPM.
     * <p>
     * Formula: Ratio = (Speed_kmh / RPM) * 1000
     * Tightly calibrated for Royal Enfield 350 gear ratios.
     * Ratio bands:
     * - 6.0 - 10.0: 1st Gear
     * - 10.0 - 15.5: 2nd Gear
     * - 15.5 - 21.0: 3rd Gear
     * - 21.0 - 27.5: 4th Gear
     * - > 27.5: 5th Gear
     * - Speed < 3 or RPM < 800: Neutral (N)
     */
    public static String calculateGear(int speedKmh, int rpm) {
        if (speedKmh < 3 || rpm < 800) return "N";
        double ratio = ((double) speedKmh / (double) rpm) * 1000.0;
        if (ratio >= 6.0 && ratio <= 10.0) return "1";
        if (ratio > 10.0 && ratio <= 15.5) return "2";
        if (ratio > 15.5 && ratio <= 21.0) return "3";
        if (ratio > 21.0 && ratio <= 27.5) return "4";
        if (ratio > 27.5) return "5";
        return "-";
    }

    /**
     * Parses Mode 01 PID 0B (Intake Manifold Absolute Pressure - MAP).
     * <p>
     * Formula: A
     * A is the data byte representing pressure in kPa.
     * Output Range: 0 to 255 kPa.
     */
    public static int parseMap(String raw) {
        if (raw == null) return 0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("410B");
        if (idx != -1 && clean.length() >= idx + 6) {
            try {
                return Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Parses Mode 01 PID 0F (Intake Air Temperature - IAT).
     * <p>
     * Formula: A - 40
     * A is the data byte.
     * Output Range: -40°C to 215°C.
     */
    public static int parseIat(String raw) {
        if (raw == null) return 0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("410F");
        if (idx != -1 && clean.length() >= idx + 6) {
            try {
                return Integer.parseInt(clean.substring(idx + 4, idx + 6), 16) - 40;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Parses Mode 01 PID 14 (O2 Sensor Voltage).
     * <p>
     * Formula: A / 200
     * A is the data byte.
     * Output Range: 0 to 1.275 Volts.
     */
    public static double parseO2Voltage(String raw) {
        if (raw == null) return 0.0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("4114");
        if (idx != -1 && clean.length() >= idx + 6) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                return a / 200.0;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0.0;
    }

    /**
     * Parses Mode 01 PID 42 (Control Module Voltage).
     * <p>
     * Formula: ((A * 256) + B) / 1000
     * Represents the voltage at the ECU/Battery.
     * Output Range: 0 to 65.535 Volts.
     */
    public static double parseControlModuleVoltage(String raw) {
        if (raw == null) return 0.0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("4142");
        if (idx != -1 && clean.length() >= idx + 8) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                int b = Integer.parseInt(clean.substring(idx + 6, idx + 8), 16);
                return ((a * 256.0) + b) / 1000.0;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0.0;
    }

    /**
     * Parses Mode 01 PID 04 (Calculated Engine Load).
     * <p>
     * Formula: (A * 100) / 255
     * A is the data byte.
     * Output Range: 0% to 100%.
     */
    public static double parseEngineLoad(String raw) {
        if (raw == null) return 0.0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("4104");
        if (idx != -1 && clean.length() >= idx + 6) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                return (a * 100.0) / 255.0;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0.0;
    }

    /**
     * Parses Mode 01 PID 0E (Ignition Timing Advance).
     * <p>
     * Formula: (A / 2) - 64
     * A is the data byte.
     * Output Range: -64 to 63.5 degrees.
     */
    public static double parseTimingAdvance(String raw) {
        if (raw == null) return 0.0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("410E");
        if (idx != -1 && clean.length() >= idx + 6) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                return (a / 2.0) - 64.0;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0.0;
    }

    /**
     * Parses Mode 01 PID 1F (Run Time Since Engine Start).
     * <p>
     * Formula: (A * 256) + B
     * A and B are bytes representing total seconds.
     * Output Range: 0 to 65,535 seconds.
     */
    public static int parseRunTime(String raw) {
        if (raw == null) return 0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("411F");
        if (idx != -1 && clean.length() >= idx + 8) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                int b = Integer.parseInt(clean.substring(idx + 6, idx + 8), 16);
                return (a * 256) + b;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Parses Mode 01 PID 21 (Distance Traveled with MIL on).
     * <p>
     * Formula: (A * 256) + B
     * A and B are bytes representing distance in km.
     * Output Range: 0 to 65,535 km.
     */
    public static int parseDistanceMil(String raw) {
        if (raw == null) return 0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("4121");
        if (idx != -1 && clean.length() >= idx + 8) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                int b = Integer.parseInt(clean.substring(idx + 6, idx + 8), 16);
                return (a * 256) + b;
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Parses Mode 01 PID 2F (Fuel Tank Level Input).
     * <p>
     * Formula: (A * 100) / 255
     * Output Range: 0% to 100%. Returns -1.0 if unavailable.
     */
    public static double parseFuelLevel(String raw) {
        if (raw == null) return -1.0;
        String clean = raw.replaceAll("\\s+", "");
        int idx = clean.indexOf("412F");
        if (idx != -1 && clean.length() >= idx + 6) {
            try {
                int a = Integer.parseInt(clean.substring(idx + 4, idx + 6), 16);
                return (a * 100.0) / 255.0;
            } catch (NumberFormatException ignored) {
            }
        }
        return -1.0;
    }

    /**
     * Calculates the estimated Fuel Flow in Liters per Hour (L/h) using the Speed-Density method.
     * <p>
     * Core Algorithm (Speed Density Approximation):
     * 1. Intake Air Volume Flow Rate (IVR) = (RPM * MAP / 2) * Displacement * VolumetricEfficiency
     * 2. Mass Air Flow (MAF) = (IVR * MolarMassOfAir) / (R * Temp_K)
     * 3. Fuel Mass Flow = MAF / AFR (Air-Fuel Ratio, typically 14.7)
     * 4. Fuel Volume Flow = FuelMassFlow / FuelDensity
     * <p>
     * Variables used for RE 350:
     * - DISP_L = 0.349 (349cc displacement)
     * - VE = 0.80 (80% Volumetric Efficiency estimate)
     * - AFR = 14.7 (Stoichiometric ratio for gasoline)
     * - GAS_DENSITY = 740.0 g/L
     */
    public static double calculateFuelFlowLph(int rpm, int mapKpa, int iatC, double displacementLiters) {
        if (rpm == 0 || mapKpa == 0) return 0.0;

        double DISP_L = displacementLiters > 0 ? displacementLiters : 0.349;
        double VE = 0.80;
        double AFR = 14.7;
        double GAS_DENSITY = 740.0; // g/L

        double tempK = iatC + 273.15;
        if (tempK == 0) return 0.0;

        // MAF (g/s) approximation using the Ideal Gas Law: PV = nRT -> n = PV/RT
        // 3.484 is the simplified constant combining molar mass of air and gas constant.
        double maf = (rpm * mapKpa / tempK) * (DISP_L * VE / 120.0) * 3.484;

        double fuelGs = maf / AFR;
        double fuelLs = fuelGs / GAS_DENSITY;
        return fuelLs * 3600.0;
    }

    public static double calculateFuelFlowLph(int rpm, int mapKpa, int iatC) {
        return calculateFuelFlowLph(rpm, mapKpa, iatC, 0.349);
    }

    /**
     * Calculates instantaneous fuel economy in km/L.
     * <p>
     * Formula: Speed_kmh / FuelFlow_Lph
     */
    public static double calculateFuelEconomy(int rpm, int mapKpa, int iatC, int speedKmh, double displacementLiters) {
        if (speedKmh == 0) return 0.0;
        double fuelLh = calculateFuelFlowLph(rpm, mapKpa, iatC, displacementLiters);
        if (fuelLh == 0) return 0.0;
        return speedKmh / fuelLh;
    }

    public static double calculateFuelEconomy(int rpm, int mapKpa, int iatC, int speedKmh) {
        return calculateFuelEconomy(rpm, mapKpa, iatC, speedKmh, 0.349);
    }

    /**
     * Checks battery/charging health status.
     * <p>
     * Logic: If engine is above 1500 RPM (charging range) but voltage is below 13.5V, 
     * it suggests the stator or regulator/rectifier is underperforming.
     */
    public static boolean checkBatteryHealth(int rpm, double voltage) {
        return rpm > 1500 && voltage > 0 && voltage < 13.5;
    }

    /**
     * Evaluates Air-Fuel Mixture status based on O2 sensor voltage.
     * <p>
     * Logic:
     * - < 0.3V: LEAN (Excess air)
     * - > 0.6V: RICH (Excess fuel)
     * - 0.3V - 0.6V: OPTIMAL
     */
    public static String evaluateMixture(double o2Volts) {
        if (o2Volts <= 0.0) return "-";
        if (o2Volts < 0.3) return "LEAN";
        if (o2Volts > 0.6) return "RICH";
        return "OPTIMAL";
    }
}
