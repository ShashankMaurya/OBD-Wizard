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

public class StandardDashFragment extends Fragment implements TelemetryUpdateListener {

    private TextView txtRpm, txtSpeed, txtTemp, txtThrottle, txtMap, txtIat, txtLoad, txtO2, txtBatt, txtTiming, txtRuntime, txtMilDist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_standard_dash, container, false);

        txtRpm = view.findViewById(R.id.txtRpm);
        txtSpeed = view.findViewById(R.id.txtSpeed);
        txtTemp = view.findViewById(R.id.txtTemp);
        txtThrottle = view.findViewById(R.id.txtThrottle);
        txtMap = view.findViewById(R.id.txtMap);
        txtIat = view.findViewById(R.id.txtIat);
        txtLoad = view.findViewById(R.id.txtLoad);
        txtO2 = view.findViewById(R.id.txtO2);
        txtBatt = view.findViewById(R.id.txtBatt);
        txtTiming = view.findViewById(R.id.txtTiming);
        txtRuntime = view.findViewById(R.id.txtRuntime);
        txtMilDist = view.findViewById(R.id.txtMilDist);

        return view;
    }

    @Override
    public void onTelemetryData(MotorcycleTelemetry data) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (txtRpm == null) return;
            txtRpm.setText(String.valueOf(data.rpm));
            txtSpeed.setText(data.speedKmh + " km/h");
            txtTemp.setText(data.engineTempC + " °C");
            txtThrottle.setText((int) data.throttlePercent + " %");
            txtMap.setText(data.mapKpa + " kPa");
            txtIat.setText(data.iatTempC + " °C");
            txtLoad.setText((int) data.engineLoadPercent + " %");
            txtO2.setText(String.format(Locale.US, "%.2f V", data.o2Voltage));
            txtBatt.setText(String.format(Locale.US, "%.1f V", data.controlModuleVoltage));
            txtTiming.setText(String.format(Locale.US, "%.1f°", data.timingAdvance));
            txtRuntime.setText(data.runTimeSeconds + " s");
            txtMilDist.setText(data.distanceWithMilKm + " km");

            // Temp coloring
            if (getContext() != null) {
                if (data.engineTempC < 50) {
                    txtTemp.setTextColor(requireContext().getColor(R.color.accent_blue));
                } else if (data.engineTempC > 120) {
                    txtTemp.setTextColor(requireContext().getColor(R.color.accent_red));
                } else {
                    txtTemp.setTextColor(requireContext().getColor(R.color.text_white));
                }
            }
        });
    }
}
