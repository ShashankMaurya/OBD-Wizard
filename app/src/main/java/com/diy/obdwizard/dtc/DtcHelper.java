package com.diy.obdwizard.dtc;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DtcHelper {

    private static final String DTC_DICTIONARY = "dtc_dictionary.json";
    private static Map<String, String> dtcMap = null;

    // Synchronized to ensure thread-safety since it will be called from a background thread
    public static synchronized void preloadDtcMap(Context context) {
        if (dtcMap != null) return;

        Map<String, String> tempMap = new LinkedHashMap<>();
        try {
            InputStream is = context.getAssets().open(DTC_DICTIONARY);
            int size = is.available();
            byte[] buffer = new byte[size];
            int bytesRead = is.read(buffer);
            is.close();

            if (bytesRead > 0) {
                String json = new String(buffer, StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(json);
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    tempMap.put(key, jsonObject.getString(key));
                }
            }
            dtcMap = tempMap;
        } catch (Exception e) {
            Log.e("DtcHelper", "Failed to load DTC dictionary", e);
            if (dtcMap == null) dtcMap = new LinkedHashMap<>(); // Prevent null pointer exceptions
        }
    }

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

    public static String getDtcDescription(Context context, String code) {
        preloadDtcMap(context);
        return dtcMap.getOrDefault(code, "Diagnostic Trouble Code (Unknown)");
    }

    public static Map<String, String> getAllKnownDtcs(Context context) {
        preloadDtcMap(context);
        return dtcMap;
    }
}
