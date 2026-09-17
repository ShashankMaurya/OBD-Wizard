package com.diy.obdwizard.trip.db;

import androidx.room.TypeConverter;

import com.diy.obdwizard.trip.TripRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class TripConverters {
    private static final Gson gson = new Gson();

    // Deserialize the  JSON string to a list
    @TypeConverter
    public static List<TripRecord.TelemetrySnapshot> fromString(String value) {
        Type listType = new TypeToken<List<TripRecord.TelemetrySnapshot>>() {
        }.getType();
        return gson.fromJson(value, listType);
    }

    // Serialize the list to a JSON string
    @TypeConverter
    public static String fromList(List<TripRecord.TelemetrySnapshot> list) {
        return gson.toJson(list);
    }
}
