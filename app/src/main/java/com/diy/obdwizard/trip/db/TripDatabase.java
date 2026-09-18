package com.diy.obdwizard.trip.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.diy.obdwizard.trip.TripRecord;

@Database(entities = {TripRecord.class}, version = 4, exportSchema = false)
@TypeConverters({TripConverters.class})
public abstract class TripDatabase extends RoomDatabase {
    private static volatile TripDatabase INSTANCE;

    public static TripDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TripDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    TripDatabase.class, "trip_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract TripDao tripDao();
}
