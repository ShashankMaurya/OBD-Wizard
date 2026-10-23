package com.diy.obdwizard.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diy.obdwizard.R;
import com.diy.obdwizard.trip.db.VehicleProfile;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    // Callback listener for user actions on profile items
    private final OnProfileActionListener listener;
    // List of vehicle profiles to display
    private List<VehicleProfile> profiles = new ArrayList<>();
    // ID of the currently active vehicle profile
    private long activeProfileId = -1;

    public ProfileAdapter(OnProfileActionListener listener) {
        this.listener = listener;
    }

    public void setProfiles(List<VehicleProfile> profiles, long activeProfileId) {
        this.profiles = profiles != null ? profiles : new ArrayList<>();
        this.activeProfileId = activeProfileId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the individual vehicle profile card layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        VehicleProfile profile = profiles.get(position);

        // Bind profile name
        holder.txtProfileName.setText(profile.profileName);

        // Format and bind vehicle technical details (Make, Model, Year, Displacement, Fuel Capacity, Fuel Type)
        String details = String.format(Locale.US, "%s %s (%d) • %.0f cc • %.1f L %s",
                profile.make != null ? profile.make : "",
                profile.model != null ? profile.model : "",
                profile.year,
                profile.displacementCc,
                profile.fuelCapacityLiters,
                profile.fuelType != null ? profile.fuelType : "Petrol"
        );
        holder.txtProfileDetails.setText(details.trim());

        // Check if this profile is currently active
        boolean isActive = (profile.id == activeProfileId);

        // Show/hide ACTIVE badge and update button states
        holder.badgeActive.setVisibility(isActive ? View.VISIBLE : View.GONE);
        holder.btnSelect.setEnabled(!isActive);
        holder.btnSelect.setText(isActive ? "Active" : "Select");

        // Wire up click listeners for Select, Edit, and Delete actions
        holder.btnSelect.setOnClickListener(v -> {
            if (listener != null) listener.onSelect(profile);
        });
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(profile);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(profile);
        });
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * Interface definition for callbacks when a user interacts with a profile item.
     */
    public interface OnProfileActionListener {

        void onSelect(VehicleProfile profile);

        void onEdit(VehicleProfile profile);


        void onDelete(VehicleProfile profile);
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView txtProfileName, txtProfileDetails, badgeActive;
        MaterialButton btnSelect, btnEdit, btnDelete;

        ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            txtProfileName = itemView.findViewById(R.id.txtProfileName);
            txtProfileDetails = itemView.findViewById(R.id.txtProfileDetails);
            badgeActive = itemView.findViewById(R.id.badgeActive);
            btnSelect = itemView.findViewById(R.id.btnSelect);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
