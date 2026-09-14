package com.diy.obdwizard.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.diy.obdwizard.R;
import com.diy.obdwizard.obd.interfaces.TelemetryUpdateListener;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;

import java.util.Locale;

public class DerivedDashFragment extends Fragment implements TelemetryUpdateListener {

    private TextView txtGear, txtEcon, txtAvgEcon, txtFuelUsed, txtMixture, txtShift;
    private TextView txt0to40, txt0to60, txt0to80, txt0to100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_derived_dash, container, false);

        txtGear = view.findViewById(R.id.txtGear);
        txtEcon = view.findViewById(R.id.txtEcon);
        txtAvgEcon = view.findViewById(R.id.txtAvgEcon);
        txtFuelUsed = view.findViewById(R.id.txtFuelUsed);
        txtMixture = view.findViewById(R.id.txtMixture);
        txtShift = view.findViewById(R.id.txtShift);

        txt0to40 = view.findViewById(R.id.txt0to40);
        txt0to60 = view.findViewById(R.id.txt0to60);
        txt0to80 = view.findViewById(R.id.txt0to80);
        txt0to100 = view.findViewById(R.id.txt0to100);

        return view;
    }

    @Override
    public void onTelemetryData(MotorcycleTelemetry data) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (txtGear == null) return;
            txtGear.setText(data.calculatedGear);
            if (getContext() != null) {
                txtGear.setTextColor(data.calculatedGear.equals("N") ? requireContext().getColor(R.color.accent_green) : requireContext().getColor(R.color.text_white));
            }

            txtEcon.setText(String.format(Locale.US, "%.1f km/L", data.fuelEconomyKml));
            txtAvgEcon.setText(String.format(Locale.US, "%.1f km/L", data.averageFuelEconomyKml));
            txtFuelUsed.setText(String.format(Locale.US, "%.2f L", data.sessionFuelConsumedLiters));
            txtMixture.setText(data.mixtureStatus);

            txt0to40.setText(data.zeroToFortyTime > 0 ? String.format(Locale.US, "%.1f s", data.zeroToFortyTime) : "- s");
            txt0to60.setText(data.zeroToSixtyTime > 0 ? String.format(Locale.US, "%.1f s", data.zeroToSixtyTime) : "- s");
            txt0to80.setText(data.zeroToEightyTime > 0 ? String.format(Locale.US, "%.1f s", data.zeroToEightyTime) : "- s");
            txt0to100.setText(data.zeroToHundredTime > 0 ? String.format(Locale.US, "%.1f s", data.zeroToHundredTime) : "- s");

            if (getContext() != null) {
                if (data.rpm > 3500) {
                    txtShift.setText("SHIFT UP!");
                    txtShift.setTextColor(requireContext().getColor(R.color.accent_red));
                } else {
                    txtShift.setText("-");
                    txtShift.setTextColor(requireContext().getColor(R.color.text_gray));
                }
            }
        });
    }
}
