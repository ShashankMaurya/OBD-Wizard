package com.diy.obdwizard.trip.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.diy.obdwizard.trip.TripRecord;

import java.util.List;

@Dao
public interface TripDao {
    @Insert
    long insert(TripRecord trip);

    @Query("SELECT * FROM trips ORDER BY startTimeMs DESC")
    List<TripRecord> getAllTrips();

    @Query("SELECT * FROM trips WHERE id = :id")
    TripRecord getTripById(long id);
}
