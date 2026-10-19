package com.diy.obdwizard.trip.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface VehicleProfileDao {
    @Query("SELECT * FROM vehicle_profiles")
    List<VehicleProfile> getAllProfiles();

    @Query("SELECT * FROM vehicle_profiles WHERE id = :profileId")
    VehicleProfile getProfileById(long profileId);

    @Query("SELECT * FROM vehicle_profiles WHERE isDefault = 1 LIMIT 1")
    VehicleProfile getDefaultProfile();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertProfile(VehicleProfile profile);

    @Update
    void updateProfile(VehicleProfile profile);

    @Delete
    void deleteProfile(VehicleProfile profile);

    @Query("UPDATE vehicle_profiles SET isDefault = 0")
    void clearAllDefaults();
}
