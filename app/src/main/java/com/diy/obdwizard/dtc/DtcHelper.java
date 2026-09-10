package com.diy.obdwizard.dtc;

import java.util.ArrayList;
import java.util.List;

public class DtcHelper {

    public static List<String> parseMode03Response(String rawResponse) {
        List<String> codes = new ArrayList<>();
        if (rawResponse == null || rawResponse.isEmpty()) return codes;

        if (rawResponse.contains("43") && !rawResponse.contains("NO DATA")) {
            String clean = rawResponse.replaceAll("\\s+", "").replace("43", "");
            for (int i = 0; i + 4 <= clean.length(); i += 4) {
                String chunk = clean.substring(i, i + 4);
                if (!chunk.equals("0000")) {
                    codes.add("P" + chunk);
                }
            }
        }
        return codes;
    }

    public static boolean isClearSuccessful(String rawResponse) {
        if (rawResponse == null) return false;
        return rawResponse.contains("OK") || rawResponse.contains("44");
    }

    public static String getDtcDescription(String code) {
        switch (code) {
            case "P0105":
                return "Manifold Absolute Pressure (MAP) Sensor Circuit";
            case "P0113":
                return "Intake Air Temperature (IAT) Sensor Circuit High";
            case "P0115":
                return "Engine Temperature Circuit";
            case "P0120":
                return "Throttle Position Sensor (TPS) Circuit";
            case "P0130":
                return "O2 Sensor Circuit (Bank 1)";
            default:
                return "Diagnostic Trouble Code";
        }
    }
}
