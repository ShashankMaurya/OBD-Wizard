package com.diy.obdwizard.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.diy.obdwizard.R;
import com.diy.obdwizard.trip.TripRecord;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_summary);

        TripRecord record = (TripRecord) getIntent().getSerializableExtra("TRIP_RECORD");
        if (record == null) {
            finish();
            return;
        }

        bindData(record);
        setupCharts(record);

        findViewById(R.id.btnDone).setOnClickListener(v -> finish());
    }

    private void bindData(TripRecord record) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
        ((TextView) findViewById(R.id.txtTripTime)).setText(sdf.format(new Date(record.startTimeMs)));

        ((TextView) findViewById(R.id.txtSumDistance)).setText(String.format(Locale.US, "%.1f km", record.totalDistanceKm));

        long durationSec = record.durationMs / 1000;
        String durationStr = String.format(Locale.US, "%02d:%02d", durationSec / 60, durationSec % 60);
        ((TextView) findViewById(R.id.txtSumDuration)).setText(durationStr);

        ((TextView) findViewById(R.id.txtSumAvgSpeed)).setText(String.format(Locale.US, "%.0f km/h", record.avgSpeedKmh));
        ((TextView) findViewById(R.id.txtSumMaxSpeed)).setText(record.maxSpeedKmh + " km/h");

        ((TextView) findViewById(R.id.txtSumFuel)).setText(String.format(Locale.US, "%.2f L", record.fuelConsumedL));
        double economy = (record.totalDistanceKm > 0) ? (record.fuelConsumedL / record.totalDistanceKm) * 100 : 0;
        ((TextView) findViewById(R.id.txtSumEconomy)).setText(String.format(Locale.US, "%.1f L/100km", economy));

        ((TextView) findViewById(R.id.txtSumAvgRpm)).setText(String.format(Locale.US, "%.0f", record.avgRpm));
        ((TextView) findViewById(R.id.txtSumMaxRpm)).setText(String.valueOf(record.maxRpm));

        ((TextView) findViewById(R.id.txtSum0to40)).setText(record.best0to40 > 0 ? String.format(Locale.US, "%.1f s", record.best0to40) : "- s");
        ((TextView) findViewById(R.id.txtSum0to60)).setText(record.best0to60 > 0 ? String.format(Locale.US, "%.1f s", record.best0to60) : "- s");
        ((TextView) findViewById(R.id.txtSum0to80)).setText(record.best0to80 > 0 ? String.format(Locale.US, "%.1f s", record.best0to80) : "- s");
        ((TextView) findViewById(R.id.txtSum0to100)).setText(record.best0to100 > 0 ? String.format(Locale.US, "%.1f s", record.best0to100) : "- s");
    }

    private void setupCharts(TripRecord record) {
        LineChart chart = findViewById(R.id.chartCombined);

        List<Entry> speedEntries = new ArrayList<>();
        List<Entry> rpmEntries = new ArrayList<>();
        List<Entry> tempEntries = new ArrayList<>();

        long startTime = record.startTimeMs;
        for (TripRecord.TelemetrySnapshot s : record.snapshots) {
            float x = (s.timestampMs - startTime) / 1000f; // Seconds from start
            speedEntries.add(new Entry(x, s.speed));
            rpmEntries.add(new Entry(x, s.rpm));
            tempEntries.add(new Entry(x, s.engineTemp));
        }

        LineDataSet speedSet = createDataSet(speedEntries, "Speed (km/h)", Color.parseColor("#00E5FF"), YAxis.AxisDependency.LEFT);
        LineDataSet rpmSet = createDataSet(rpmEntries, "RPM", Color.parseColor("#FF9100"), YAxis.AxisDependency.RIGHT);
        LineDataSet tempSet = createDataSet(tempEntries, "Temp (°C)", Color.parseColor("#FF5252"), YAxis.AxisDependency.LEFT);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(speedSet);
        dataSets.add(rpmSet);
        dataSets.add(tempSet);

        LineData lineData = new LineData(dataSets);
        chart.setData(lineData);

        // Customize Chart
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#33FFFFFF"));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(true);
        rightAxis.setTextColor(Color.parseColor("#FF9100")); // RPM color
        rightAxis.setDrawGridLines(false);

        chart.invalidate();
    }

    private LineDataSet createDataSet(List<Entry> entries, String label, int color, YAxis.AxisDependency dependency) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setAxisDependency(dependency);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return dataSet;
    }
}
