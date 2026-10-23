package com.diy.obdwizard.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diy.obdwizard.MainActivity;
import com.diy.obdwizard.R;
import com.diy.obdwizard.trip.ProfileManager;
import com.diy.obdwizard.trip.db.TripDatabase;
import com.diy.obdwizard.trip.db.VehicleProfile;
import com.diy.obdwizard.trip.db.VehicleProfileDao;
import com.diy.obdwizard.ui.adapters.ProfileAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class ProfileManagementFragment extends Fragment implements ProfileAdapter.OnProfileActionListener {

    private RecyclerView recyclerProfiles;
    private MaterialButton btnAddProfile;
    private ProfileAdapter adapter;
    private VehicleProfileDao profileDao;
    private ProfileManager profileManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_management, container, false);

        recyclerProfiles = view.findViewById(R.id.recyclerProfiles);
        btnAddProfile = view.findViewById(R.id.btnAddProfile);

        TripDatabase db = TripDatabase.getDatabase(requireContext());
        profileDao = db.vehicleProfileDao();
        profileManager = ProfileManager.getInstance(requireContext());

        recyclerProfiles.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProfileAdapter(this);
        recyclerProfiles.setAdapter(adapter);

        btnAddProfile.setOnClickListener(v -> showEditProfileDialog(null));

        loadProfiles();
        return view;
    }

    private void loadProfiles() {
        List<VehicleProfile> profiles = profileDao.getAllProfiles();
        VehicleProfile active = profileManager.getActiveProfile(requireContext());
        long activeId = active != null ? active.id : -1;
        adapter.setProfiles(profiles, activeId);
    }

    @Override
    public void onSelect(VehicleProfile profile) {
        profileManager.setActiveProfile(profile);
        Toast.makeText(requireContext(), "Active Profile: " + profile.profileName, Toast.LENGTH_SHORT).show();
        loadProfiles();
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).updateNavHeader();
        }
    }

    @Override
    public void onEdit(VehicleProfile profile) {
        showEditProfileDialog(profile);
    }

    @Override
    public void onDelete(VehicleProfile profile) {
        List<VehicleProfile> all = profileDao.getAllProfiles();
        if (all.size() <= 1) {
            Toast.makeText(requireContext(), "Cannot delete the last remaining profile", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete " + profile.profileName + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    profileDao.deleteProfile(profile);
                    VehicleProfile active = profileManager.getActiveProfile(requireContext());
                    if (active != null && active.id == profile.id) {
                        List<VehicleProfile> remaining = profileDao.getAllProfiles();
                        if (!remaining.isEmpty()) {
                            profileManager.setActiveProfile(remaining.get(0));
                        }
                    }
                    loadProfiles();
                    if (requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity()).updateNavHeader();
                    }
                    Toast.makeText(requireContext(), "Profile Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditProfileDialog(@Nullable VehicleProfile profileToEdit) {
        boolean isEdit = profileToEdit != null;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_vehicle_profile, null);

        EditText edtProfileName = dialogView.findViewById(R.id.edtProfileName);
        AutoCompleteTextView edtVehicleType = dialogView.findViewById(R.id.edtVehicleType);
        EditText edtMake = dialogView.findViewById(R.id.edtMake);
        EditText edtModel = dialogView.findViewById(R.id.edtModel);
        EditText edtYear = dialogView.findViewById(R.id.edtYear);
        EditText edtDisplacement = dialogView.findViewById(R.id.edtDisplacement);
        EditText edtFuelCapacity = dialogView.findViewById(R.id.edtFuelCapacity);
        AutoCompleteTextView edtFuelType = dialogView.findViewById(R.id.edtFuelType);

        ArrayAdapter<String> vehicleAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new String[]{"Car", "Motorcycle"});
        edtVehicleType.setAdapter(vehicleAdapter);
        edtVehicleType.setOnClickListener(v -> edtVehicleType.showDropDown());

        ArrayAdapter<String> fuelAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new String[]{"Petrol", "Diesel"});
        edtFuelType.setAdapter(fuelAdapter);
        edtFuelType.setOnClickListener(v -> edtFuelType.showDropDown());

        if (isEdit) {
            edtProfileName.setText(profileToEdit.profileName);
            edtVehicleType.setText(profileToEdit.vehicleType, false);
            edtMake.setText(profileToEdit.make);
            edtModel.setText(profileToEdit.model);
            edtYear.setText(String.valueOf(profileToEdit.year));
            edtDisplacement.setText(String.valueOf(profileToEdit.displacementCc));
            edtFuelCapacity.setText(String.valueOf(profileToEdit.fuelCapacityLiters));
            edtFuelType.setText(profileToEdit.fuelType, false);
        } else {
            edtVehicleType.setText("Motorcycle", false);
            edtYear.setText("2023");
            edtDisplacement.setText("349");
            edtFuelCapacity.setText("13.0");
            edtFuelType.setText("Petrol", false);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(isEdit ? "Edit Vehicle Profile" : "Create Vehicle Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = edtProfileName.getText().toString().trim();
                    String type = edtVehicleType.getText().toString().trim();
                    String make = edtMake.getText().toString().trim();
                    String model = edtModel.getText().toString().trim();
                    String yearStr = edtYear.getText().toString().trim();
                    String dispStr = edtDisplacement.getText().toString().trim();
                    String capStr = edtFuelCapacity.getText().toString().trim();
                    String fType = edtFuelType.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Profile name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int year = 2023;
                    try {
                        year = Integer.parseInt(yearStr);
                    } catch (NumberFormatException ignored) {
                    }

                    double disp = 349.0;
                    try {
                        disp = Double.parseDouble(dispStr);
                    } catch (NumberFormatException ignored) {
                    }

                    double cap = 13.0;
                    try {
                        cap = Double.parseDouble(capStr);
                    } catch (NumberFormatException ignored) {
                    }

                    boolean wasEmpty = profileDao.getAllProfiles().isEmpty();

                    if (isEdit) {
                        profileToEdit.profileName = name;
                        profileToEdit.vehicleType = type;
                        profileToEdit.make = make;
                        profileToEdit.model = model;
                        profileToEdit.year = year;
                        profileToEdit.displacementCc = disp;
                        profileToEdit.fuelCapacityLiters = cap;
                        profileToEdit.fuelType = fType;
                        profileDao.updateProfile(profileToEdit);
                    } else {
                        boolean isFirst = wasEmpty;
                        VehicleProfile newProfile = new VehicleProfile(name, type, make, model, year, disp, cap, fType, isFirst);
                        long id = profileDao.insertProfile(newProfile);
                        newProfile.id = id;
                        if (isFirst) {
                            profileManager.setActiveProfile(newProfile);
                        }
                    }
                    loadProfiles();
                    if (requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity()).updateNavHeader();
                    }
                    Toast.makeText(requireContext(), "Profile Saved", Toast.LENGTH_SHORT).show();

                    if (wasEmpty && requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity()).updateNavHeader();
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new DashboardFragment())
                                .commit();
                        requireActivity().setTitle("Dashboard");
                        NavigationView navView = requireActivity().findViewById(R.id.nav_view);
                        if (navView != null) {
                            navView.setCheckedItem(R.id.nav_dashboard);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
