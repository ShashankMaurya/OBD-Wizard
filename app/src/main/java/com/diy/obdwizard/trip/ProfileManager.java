package com.diy.obdwizard.trip;

import android.content.Context;
import android.content.SharedPreferences;

import com.diy.obdwizard.trip.db.TripDatabase;
import com.diy.obdwizard.trip.db.VehicleProfile;
import com.diy.obdwizard.trip.db.VehicleProfileDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager class responsible for handling vehicle profiles, active profile persistence,
 * caching, and engine displacement conversions.
 * Implements the Singleton pattern.
 */
public class ProfileManager {
    private static ProfileManager instance;
    private final VehicleProfileDao profileDao;
    private final SharedPreferences prefs;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private VehicleProfile cachedActiveProfile;

    /**
     * Private constructor for singleton initialization.
     *
     * @param context Application or activity context.
     */
    private ProfileManager(Context context) {
        TripDatabase db = TripDatabase.getDatabase(context);
        profileDao = db.vehicleProfileDao();
        prefs = context.getApplicationContext().getSharedPreferences("obd_profile_prefs", Context.MODE_PRIVATE);
    }

    /**
     * Retrieves the singleton instance of ProfileManager.
     *
     * @param context Application or activity context.
     * @return The ProfileManager singleton instance.
     */
    public static synchronized ProfileManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager(context);
        }
        return instance;
    }

    /**
     * Gets the currently active vehicle profile.
     * Checks memory cache first, then SharedPreferences, and falls back to default or first available profile.
     *
     * @param context Context used for accessing database if necessary.
     * @return The active VehicleProfile, or null if no profiles exist.
     */
    public VehicleProfile getActiveProfile(Context context) {
        if (cachedActiveProfile != null) return cachedActiveProfile;

        long activeId = prefs.getLong("active_profile_id", -1);
        List<VehicleProfile> all = profileDao.getAllProfiles();
        if (all.isEmpty()) {
            return null;
        }

        // Check memory cache (SharedPreferences) first
        if (activeId != -1) {
            for (VehicleProfile p : all) {
                if (p.id == activeId) {
                    cachedActiveProfile = p;
                    return p;
                }
            }
        }

        // Check in DB for default profile
        for (VehicleProfile p : all) {
            if (p.isDefault) {
                cachedActiveProfile = p;
                prefs.edit().putLong("active_profile_id", p.id).apply();
                return p;
            }
        }

        // Fallback: use first profile
        cachedActiveProfile = all.get(0);
        prefs.edit().putLong("active_profile_id", cachedActiveProfile.id).apply();
        return cachedActiveProfile;
    }

    /**
     * Sets the currently active vehicle profile and persists its ID in SharedPreferences.
     *
     * @param profile The vehicle profile to set as active.
     */
    public void setActiveProfile(VehicleProfile profile) {
        cachedActiveProfile = profile;
        if (profile != null) {
            prefs.edit().putLong("active_profile_id", profile.id).apply();
        } else {
            prefs.edit().remove("active_profile_id").apply();
        }
    }

    /**
     * Calculates and returns the engine displacement of the active profile in Liters.
     * Used by ObdParser for speed-density fuel flow and economy calculations.
     *
     * @param context Context used to query active profile.
     * @return Displacement in Liters (defaults to 0.349L if unspecified).
     */
    public double getActiveDisplacementLiters(Context context) {
        VehicleProfile p = getActiveProfile(context);
        if (p != null && p.displacementCc > 0) {
            return p.displacementCc / 1000.0;
        }
        return 0.349;
    }

    /**
     * Checks whether any vehicle profiles exist in the database.
     *
     * @return True if at least one profile exists, false otherwise.
     */
    public boolean hasProfiles() {
        return !profileDao.getAllProfiles().isEmpty();
    }
}
