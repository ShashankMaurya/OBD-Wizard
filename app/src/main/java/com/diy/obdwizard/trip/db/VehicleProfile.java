package com.diy.obdwizard.trip.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "vehicle_profiles")
public class VehicleProfile implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String profileName;
    public String vehicleType; // e.g. "Motorcycle", "Car"
    public String make;
    public String model;
    public int year;
    public double displacementCc;
    public double fuelCapacityLiters;
    public String fuelType;
    public boolean isDefault;

    public VehicleProfile() {
    }

    public VehicleProfile(String profileName, String vehicleType, String make, String model, int year, double displacementCc, double fuelCapacityLiters, String fuelType, boolean isDefault) {
        this.profileName = profileName;
        this.vehicleType = vehicleType;
        this.make = make;
        this.model = model;
        this.year = year;
        this.displacementCc = displacementCc;
        this.fuelCapacityLiters = fuelCapacityLiters;
        this.fuelType = fuelType;
        this.isDefault = isDefault;
    }
}
