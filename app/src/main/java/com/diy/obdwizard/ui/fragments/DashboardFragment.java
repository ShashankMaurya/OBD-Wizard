package com.diy.obdwizard.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.diy.obdwizard.MainActivity;
import com.diy.obdwizard.R;
import com.diy.obdwizard.obd.interfaces.TelemetryUpdateListener;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;
import com.diy.obdwizard.trip.TripManager;
import com.diy.obdwizard.trip.TripRecord;
import com.diy.obdwizard.ui.activities.TripSummaryActivity;
import com.diy.obdwizard.ui.views.ArcGaugeView;
import com.diy.obdwizard.ui.views.BikeGearView;
import com.diy.obdwizard.ui.views.RpmSpeedGaugeView;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * Primary Default Dashboard Fragment.
 * <p>
 * Displays the futuristic primary motorcycle instrumentation layout:
 * <ul>
 *     <li>Central RPM arc dial & digital speedometer (RpmSpeedGaugeView).</li>
 *     <li>Bike 5-speed gear pill bar (BikeGearView).</li>
 *     <li>Engine Coolant Temperature gauge card (ArcGaugeView).</li>
 *     <li>Battery Voltage gauge card (ArcGaugeView).</li>
 *     <li>Throttle Position percentage gauge card (ArcGaugeView).</li>
 *     <li>Engine Load percentage gauge card (ArcGaugeView) - incorporating PID 0104 engine load sensor feedback.</li>
 *     <li>Engine Runtime digital timer card.</li>
 *     <li>Session Fuel Consumed digital card.</li>
 *     <li>Instant and Average Fuel Economy consumption cards.</li>
 *     <li>Trip Recording control button.</li>
 * </ul>
 */
public class DashboardFragment extends Fragment implements TelemetryUpdateListener {

    private BikeGearView bikeGearView;
    private RpmSpeedGaugeView rpmSpeedGauge;
    private ArcGaugeView gaugeTemp;
    private ArcGaugeView gaugeBattery;
    private ArcGaugeView gaugeThrottle;
    private ArcGaugeView gaugeLoad;
    private TextView txtRuntimeValue;
    private TextView txtFuelConsumedValue;
    private TextView txtEconInstValue;
    private TextView txtEconAvgValue;
    private MaterialButton btnTripAction;

    // OBD Connection Views
    private TextView txtHeaderStatus;
    private TextView txtConnectBanner;
    private MaterialButton btnConnectObd;

    private MotorcycleTelemetry lastData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Bind layout views
        bikeGearView = view.findViewById(R.id.bikeGearView);
        rpmSpeedGauge = view.findViewById(R.id.rpmSpeedGauge);
        gaugeTemp = view.findViewById(R.id.gaugeTemp);
        gaugeBattery = view.findViewById(R.id.gaugeBattery);
        gaugeThrottle = view.findViewById(R.id.gaugeThrottle);
        gaugeLoad = view.findViewById(R.id.gaugeLoad);
        txtRuntimeValue = view.findViewById(R.id.txtRuntimeValue);
        txtFuelConsumedValue = view.findViewById(R.id.txtFuelConsumedValue);
        txtEconInstValue = view.findViewById(R.id.txtEconInstValue);
        txtEconAvgValue = view.findViewById(R.id.txtEconAvgValue);
        btnTripAction = view.findViewById(R.id.btnTripAction);

        // Bind OBD connection views
        txtHeaderStatus = view.findViewById(R.id.txtHeaderStatus);
        txtConnectBanner = view.findViewById(R.id.txtConnectBanner);
        btnConnectObd = view.findViewById(R.id.btnConnectObd);

        btnConnectObd.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                if (activity.getObdManager().isConnected()) {
                    activity.disconnect();
                } else {
                    activity.startConnection();
                }
            }
        });

        updateTripButtonState();
        updateConnectionStatus();

        btnTripAction.setOnClickListener(v -> {
            TripManager tm = TripManager.getInstance();
            if (tm.isRecording()) {
                TripRecord record = tm.stopTrip(lastData != null ? lastData : new MotorcycleTelemetry(), getContext());
                updateTripButtonState();
                if (record != null) {
                    Intent intent = new Intent(getContext(), TripSummaryActivity.class);
                    intent.putExtra("TRIP_RECORD", record);
                    startActivity(intent);
                }
            } else {
                if (lastData == null) {
                    Toast.makeText(getContext(), "Wait for telemetry data...", Toast.LENGTH_SHORT).show();
                    return;
                }
                tm.startTrip(lastData);
                updateTripButtonState();
                Toast.makeText(getContext(), "Trip Recording Started", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void updateTripButtonState() {
        TripManager tm = TripManager.getInstance();
        if (tm.isRecording()) {
            long elapsedMs = System.currentTimeMillis() - tm.getStartTimeMs();
            long sec = (elapsedMs / 1000) % 60;
            long min = (elapsedMs / 60000) % 60;
            long hr = (elapsedMs / 3600000);

            String timeStr = (hr > 0)
                    ? String.format(Locale.US, "%02d:%02d:%02d", hr, min, sec)
                    : String.format(Locale.US, "%02d:%02d", min, sec);

            btnTripAction.setText("STOP TRIP (" + timeStr + ")");
            btnTripAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF3D00")));
            btnTripAction.setTextColor(Color.WHITE);
            btnTripAction.setIconResource(R.drawable.ic_stop);
            btnTripAction.setIconTint(ColorStateList.valueOf(Color.WHITE));
        } else {
            btnTripAction.setText("START TRIP");
            btnTripAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00E676")));
            btnTripAction.setTextColor(Color.BLACK);
            btnTripAction.setIconResource(R.drawable.ic_play);
            btnTripAction.setIconTint(ColorStateList.valueOf(Color.BLACK));
        }
    }

    /**
     * Receives real-time telemetry updates from the OBD background thread.
     * Updates gauge views and formatted digital text readouts on the UI thread.
     *
     * @param data Latest MotorcycleTelemetry data object polled from ECU.
     */
    @Override
    public void onTelemetryData(MotorcycleTelemetry data) {
        this.lastData = data;
        TripManager.getInstance().recordData(data);
        
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            updateTripButtonState();
            if (rpmSpeedGauge == null) return;

            // 1. Update Central Gauge (RPM, Speed, High Temp Alert, Battery Alert)
            rpmSpeedGauge.setTelemetry(
                    data.rpm,
                    data.speedKmh,
                    data.engineTempC > 100,
                    data.isBatteryChargingPoor
            );

            // 2. Update Top Bar Bike Gear Indicator (1, N, 2, 3, 4, 5)
            if (bikeGearView != null) {
                bikeGearView.setGear(data.calculatedGear);
            }

            // 3. Update Engine Coolant Temperature Gauge (0 - 120°C range, Red alert if >100°C)
            if (gaugeTemp != null) {
                int tempColor = (data.engineTempC > 100) ? Color.parseColor("#FF3D00") : Color.parseColor("#00E5FF");
                gaugeTemp.setGaugeData(
                        data.engineTempC,
                        0f,
                        120f,
                        "C",
                        "H",
                        data.engineTempC + " °C",
                        tempColor
                );
            }

            // 4. Update Battery Voltage Gauge (10 - 16V range, Red alert if <12.0V)
            if (gaugeBattery != null) {
                int battColor = (data.controlModuleVoltage < 12.0) ? Color.parseColor("#FF3D00") : Color.parseColor("#00E5FF");
                gaugeBattery.setGaugeData(
                        (float) data.controlModuleVoltage,
                        10f,
                        16f,
                        "10V",
                        "16V",
                        String.format(Locale.US, "%.1f V", data.controlModuleVoltage),
                        battColor
                );
            }

            // 5. Update Throttle Position Percentage Gauge (0 - 100% range)
            if (gaugeThrottle != null) {
                gaugeThrottle.setGaugeData(
                        (float) data.throttlePercent,
                        0f,
                        100f,
                        "0%",
                        "100%",
                        (int) data.throttlePercent + " %",
                        Color.parseColor("#00E5FF")
                );
            }

            // 6. Update Engine Load Percentage Gauge (0 - 100% range)
            if (gaugeLoad != null) {
                int loadColor = (data.engineLoadPercent > 80.0) ? Color.parseColor("#FF3D00") : Color.parseColor("#00E5FF");
                gaugeLoad.setGaugeData(
                        (float) data.engineLoadPercent,
                        0f,
                        100f,
                        "0%",
                        "100%",
                        (int) data.engineLoadPercent + " %",
                        loadColor
                );
            }

            // 7. Update Engine Runtime Timer (Formatted into HH:MM:SS or MM:SS)
            if (txtRuntimeValue != null) {
                int hrs = data.runTimeSeconds / 3600;
                int mins = (data.runTimeSeconds % 3600) / 60;
                int secs = data.runTimeSeconds % 60;
                String timeStr = (hrs > 0)
                        ? String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
                        : String.format(Locale.US, "%02d:%02d", mins, secs);
                txtRuntimeValue.setText(timeStr);
            }

            // 8. Update Session Fuel Consumed Digital Card
            if (txtFuelConsumedValue != null) {
                txtFuelConsumedValue.setText(String.format(Locale.US, "%.2f L", data.sessionFuelConsumedLiters));
            }

            // 9. Update Instant & Average Fuel Economy Consumption Readouts
            if (txtEconInstValue != null) {
                txtEconInstValue.setText(String.format(Locale.US, "%.1f km/L", data.fuelEconomyKml));
            }
            if (txtEconAvgValue != null) {
                txtEconAvgValue.setText(String.format(Locale.US, "%.1f km/L", data.averageFuelEconomyKml));
            }
        });
    }

    public void showConnecting() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            if (txtHeaderStatus != null) {
                txtHeaderStatus.setText("● CONNECTING...");
                txtHeaderStatus.setTextColor(Color.parseColor("#8A99AD"));
            }
            if (txtConnectBanner != null) {
                txtConnectBanner.setText("Searching for OBD device...");
            }
            if (btnConnectObd != null) {
                btnConnectObd.setEnabled(false);
                btnConnectObd.setText("CONNECTING");
                btnConnectObd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#333333")));
                btnConnectObd.setTextColor(Color.parseColor("#8A99AD"));
            }
        });
    }

    public void updateConnectionStatus() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        activity.runOnUiThread(() -> {
            if (txtHeaderStatus == null || btnConnectObd == null) return;

            if (activity.getObdManager().isConnecting()) {
                showConnecting();
                return;
            }

            btnConnectObd.setEnabled(true);
            boolean isConnected = activity.getObdManager().isConnected();
            if (isConnected) {
                txtHeaderStatus.setText("● CONNECTED");
                txtHeaderStatus.setTextColor(Color.parseColor("#00E676"));
                if (txtConnectBanner != null) {
                    txtConnectBanner.setText("ECU Telemetry Active");
                }
                btnConnectObd.setText("DISCONNECT");
                btnConnectObd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF3D00")));
                btnConnectObd.setTextColor(Color.WHITE);
                btnConnectObd.setIconResource(R.drawable.ic_bluetooth);
                btnConnectObd.setIconTint(ColorStateList.valueOf(Color.WHITE));
            } else {
                txtHeaderStatus.setText("● DISCONNECTED");
                txtHeaderStatus.setTextColor(Color.parseColor("#FF3D00"));
                if (txtConnectBanner != null) {
                    txtConnectBanner.setText("Link OBD scanner for live ECU telemetry");
                }
                btnConnectObd.setText("CONNECT OBD");
                btnConnectObd.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00E676")));
                btnConnectObd.setTextColor(Color.BLACK);
                btnConnectObd.setIconResource(R.drawable.ic_bluetooth);
                btnConnectObd.setIconTint(ColorStateList.valueOf(Color.BLACK));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateConnectionStatus();
    }
}
