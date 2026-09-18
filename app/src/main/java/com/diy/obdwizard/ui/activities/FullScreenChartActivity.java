package com.diy.obdwizard.ui.activities;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.diy.obdwizard.R;
import com.diy.obdwizard.ui.charts.ChartDataHolder;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

public class FullScreenChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_chart);

        ChartDataHolder.ChartConfig config = ChartDataHolder.getConfig();
        if (config == null || config.dataSets == null) {
            finish();
            return;
        }

        setupChart(config);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
    }

    private void setupChart(ChartDataHolder.ChartConfig config) {
        LineChart chart = findViewById(R.id.chartFullScreen);

        LineData lineData = new LineData(config.dataSets);
        chart.setData(lineData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#33FFFFFF"));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(config.rightAxisEnabled);
        if (config.rightAxisEnabled) {
            rightAxis.setTextColor(Color.parseColor("#FF9100")); // Assume RPM color for now
            rightAxis.setDrawGridLines(false);
        }

        chart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear the static reference to avoid memory leaks if we're closing the full screen
        // Note: In a real app, you might want to clear this only when leaving the trip summary
        // but for a one-off viewer, this is safer.
        // ChartDataHolder.clear(); 
    }
}
