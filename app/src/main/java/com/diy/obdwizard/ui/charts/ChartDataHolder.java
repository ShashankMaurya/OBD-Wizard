package com.diy.obdwizard.ui.charts;

import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.List;

/**
 * A simple holder to share chart data between activities to avoid redundant processing
 * and Intent size limits (TransactionTooLargeException).
 */
public class ChartDataHolder {
    private static ChartConfig currentConfig;

    public static ChartConfig getConfig() {
        return currentConfig;
    }

    public static void setConfig(ChartConfig config) {
        currentConfig = config;
    }

    public static void clear() {
        currentConfig = null;
    }

    public static class ChartConfig {
        public final List<ILineDataSet> dataSets;
        public final boolean rightAxisEnabled;
        public final String title;

        public ChartConfig(List<ILineDataSet> dataSets, boolean rightAxisEnabled, String title) {
            this.dataSets = dataSets;
            this.rightAxisEnabled = rightAxisEnabled;
            this.title = title;
        }
    }
}
