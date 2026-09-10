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
}
