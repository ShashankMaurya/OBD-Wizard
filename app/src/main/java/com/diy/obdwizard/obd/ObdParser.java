package com.diy.obdwizard.obd;

/**
 * Utility class for parsing raw OBD-II PID responses and calculating derived values.
 */
public class ObdParser {

    private ObdParser() {
        // Utility class
    }

    /**
     * Parses the raw OBD-II response for Mode 01 PID 0C (Engine RPM).
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
     * Parses the raw OBD-II response for Mode 01 PID 0D (Vehicle Speed in km/h).
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
     * Parses the raw OBD-II response for Mode 01 PID 11 (Throttle Position percentage).
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
     * Parses the raw OBD-II response for Mode 01 PID 05 (Engine Coolant Temperature in °C).
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
     * Calculates the estimated gear based on vehicle speed (km/h) and engine RPM.
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
     * Mode 01 PID 0B (Intake Manifold Absolute Pressure - MAP) in kPa.
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
     * Mode 01 PID 0F (Intake Air Temperature - IAT) in °C.
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
     * Mode 01 PID 14 (O2 Sensor Bank 1 Sensor 1 Voltage) in Volts.
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
     * Mode 01 PID 42 (Control Module Voltage) in Volts.
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
     * Mode 01 PID 04 (Calculated Engine Load) in percentage.
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
     * Mode 01 PID 0E (Ignition Timing Advance) in degrees.
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
     * Mode 01 PID 1F (Run Time Since Engine Start) in seconds.
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
     * Mode 01 PID 21 (Distance Traveled with MIL on) in km.
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
     * Mode 01 PID 2F (Fuel Tank Level Input) in percentage (0% to 100%).
     * Returns -1.0 if unsupported or unavailable.
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
     * Calculates fuel flow in Liters per Hour (L/h).
     */
    public static double calculateFuelFlowLph(int rpm, int mapKpa, int iatC) {
        if (rpm == 0 || mapKpa == 0) return 0.0;

        double DISP_L = 0.349;
        double VE = 0.80;
        double AFR = 14.7;
        double GAS_DENSITY = 740.0; // g/L

        double tempK = iatC + 273.15;
        if (tempK == 0) return 0.0;

        // MAF (g/s) approximation using the Ideal Gas Law and Speed Density method
        double maf = (rpm * mapKpa / tempK) * (DISP_L * VE / 120.0) * 3.484;

        double fuelGs = maf / AFR;
        double fuelLs = fuelGs / GAS_DENSITY;
        return fuelLs * 3600.0;
    }

    /**
     * Calculates instant fuel economy (km/L) using RPM, MAP, IAT, and Speed.
     * Assumes 349cc displacement and ~80% volumetric efficiency.
     */
    public static double calculateFuelEconomy(int rpm, int mapKpa, int iatC, int speedKmh) {
        if (speedKmh == 0) return 0.0;
        double fuelLh = calculateFuelFlowLph(rpm, mapKpa, iatC);
        if (fuelLh == 0) return 0.0;
        return speedKmh / fuelLh;
    }

    /**
     * Checks if the battery charging system might be failing (e.g., Stator issue).
     */
    public static boolean checkBatteryHealth(int rpm, double voltage) {
        return rpm > 1500 && voltage > 0 && voltage < 13.5;
    }

    /**
     * Estimates Lean/Rich mixture state based on O2 sensor voltage.
     */
    public static String evaluateMixture(double o2Volts) {
        if (o2Volts <= 0.0) return "-";
        if (o2Volts < 0.3) return "LEAN";
        if (o2Volts > 0.6) return "RICH";
        return "OPTIMAL";
    }
}
