package com.diy.obdwizard.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.diy.obdwizard.R;
import com.diy.obdwizard.obd.interfaces.TelemetryUpdateListener;
import com.diy.obdwizard.obd.models.MotorcycleTelemetry;
import com.diy.obdwizard.trip.TripManager;
import com.diy.obdwizard.trip.TripRecord;
import com.diy.obdwizard.ui.activities.TripSummaryActivity;
import com.diy.obdwizard.ui.views.ArcGaugeView;
import com.diy.obdwizard.ui.views.BikeGearView;
import com.diy.obdwizard.ui.views.RpmSpeedGaugeView;

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
 *     <li>Fuel Left gauge card (ArcGaugeView) - incorporating PID 012F fuel tank level sensor feedback & 13.0L capacity.</li>
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
    private ArcGaugeView gaugeFuel;
    private TextView txtRuntimeValue;
    private TextView txtFuelConsumedValue;
    private TextView txtEconInstValue;
    private TextView txtEconAvgValue;
    private Button btnTripAction;

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
        gaugeFuel = view.findViewById(R.id.gaugeFuel);
        txtRuntimeValue = view.findViewById(R.id.txtRuntimeValue);
        txtFuelConsumedValue = view.findViewById(R.id.txtFuelConsumedValue);
        txtEconInstValue = view.findViewById(R.id.txtEconInstValue);
        txtEconAvgValue = view.findViewById(R.id.txtEconAvgValue);
        btnTripAction = view.findViewById(R.id.btnTripAction);

        updateTripButtonState();

        btnTripAction.setOnClickListener(v -> {
            TripManager tm = TripManager.getInstance();
            if (tm.isRecording()) {
                TripRecord record = tm.stopTrip(lastData != null ? lastData : new MotorcycleTelemetry());
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
        if (TripManager.getInstance().isRecording()) {
            btnTripAction.setText("STOP TRIP");
            btnTripAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF3D00")));
            btnTripAction.setTextColor(Color.WHITE);
        } else {
            btnTripAction.setText("START TRIP");
            btnTripAction.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00E676")));
            btnTripAction.setTextColor(Color.BLACK);
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

            // 6. Update Fuel Left Gauge with Increased Accuracy:
            // Uses PID 012F Fuel Tank Level Input if available (0-100% of 13.0L capacity),
            // or falls back to Speed-Density MAF integration (13.0L - sessionFuelConsumedLiters).
            if (gaugeFuel != null) {
                double fuelLeftL;
                if (data.fuelLevelPercent >= 0) {
                    fuelLeftL = Math.max(0.0, Math.min(13.0, (data.fuelLevelPercent / 100.0) * 13.0));
                } else {
                    fuelLeftL = Math.max(0.0, 13.0 - data.sessionFuelConsumedLiters);
                }

                int fuelColor = (fuelLeftL < 2.5) ? Color.parseColor("#FF3D00") : Color.parseColor("#FF9100");
                gaugeFuel.setGaugeData(
                        (float) fuelLeftL,
                        0f,
                        13f,
                        "E",
                        "F",
                        String.format(Locale.US, "%.1f L", fuelLeftL),
                        fuelColor
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
}
