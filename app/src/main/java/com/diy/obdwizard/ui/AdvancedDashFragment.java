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

/**
 * Secondary "Advanced Dashboard" Fragment.
 * <p>
 * Displays the remaining standard and derived ECU telemetry parameters in the default grid card layout:
 * <ul>
 *     <li>MAP (Intake Manifold Absolute Pressure in kPa).</li>
 *     <li>IAT (Intake Air Temperature in °C).</li>
 *     <li>Calculated Engine Load (%).</li>
 *     <li>O2 Sensor Bank 1 Voltage (V).</li>
 *     <li>Ignition Timing Advance (°).</li>
 *     <li>Distance Traveled with MIL Active (km).</li>
 *     <li>Session Fuel Consumed (L).</li>
 *     <li>Air/Fuel Mixture Status (LEAN / OPTIMAL / RICH).</li>
 *     <li>Shift UP Light Indicator.</li>
 *     <li>0-40, 0-60, 0-80, and 0-100 km/h Acceleration Timers.</li>
 * </ul>
 */
public class AdvancedDashFragment extends Fragment implements TelemetryUpdateListener {

    // View references for secondary metrics
    private TextView txtMap, txtIat, txtLoad, txtO2, txtTiming, txtMilDist;
    private TextView txtAvgEcon, txtFuelUsed, txtMixture, txtShift;
    private TextView txt0to40, txt0to60, txt0to80, txt0to100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advanced_dash, container, false);

        // Bind layout views
        txtMap = view.findViewById(R.id.txtMap);
        txtIat = view.findViewById(R.id.txtIat);
        txtLoad = view.findViewById(R.id.txtLoad);
        txtO2 = view.findViewById(R.id.txtO2);
        txtTiming = view.findViewById(R.id.txtTiming);
        txtMilDist = view.findViewById(R.id.txtMilDist);

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

    /**
     * Handles real-time telemetry updates and updates secondary metric card labels on the UI thread.
     *
     * @param data Latest MotorcycleTelemetry data object.
     */
    @Override
    public void onTelemetryData(MotorcycleTelemetry data) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (txtMap == null) return;

            // Pressure, Temperature, Load, and Electrical parameters
            txtMap.setText(data.mapKpa + " kPa");
            txtIat.setText(data.iatTempC + " °C");
            txtLoad.setText((int) data.engineLoadPercent + " %");
            txtO2.setText(String.format(Locale.US, "%.2f V", data.o2Voltage));
            txtTiming.setText(String.format(Locale.US, "%.1f°", data.timingAdvance));
            txtMilDist.setText(data.distanceWithMilKm + " km");

            // Fuel Consumption and Mixture metrics
            txtAvgEcon.setText(String.format(Locale.US, "%.1f km/L", data.averageFuelEconomyKml));
            txtFuelUsed.setText(String.format(Locale.US, "%.2f L", data.sessionFuelConsumedLiters));
            txtMixture.setText(data.mixtureStatus);

            // Performance acceleration timers
            txt0to40.setText(data.zeroToFortyTime > 0 ? String.format(Locale.US, "%.1f s", data.zeroToFortyTime) : "- s");
            txt0to60.setText(data.zeroToSixtyTime > 0 ? String.format(Locale.US, "%.1f s", data.zeroToSixtyTime) : "- s");
            txt0to80.setText(data.zeroToEightyTime > 0 ? String.format(Locale.US, "%.1f s", data.zeroToEightyTime) : "- s");
            txt0to100.setText(data.zeroToHundredTime > 0 ? String.format(Locale.US, "%.1f s", data.zeroToHundredTime) : "- s");

            // Shift Light Indicator (Triggers RED warning text above 3500 RPM)
            if (getContext() != null && txtShift != null) {
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
