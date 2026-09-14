package com.diy.obdwizard.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Custom View for rendering semi-circular analog card gauge arcs.
 * <p>
 * Enhanced with the shared RetroPointerDrawer element:
 * <ul>
 *     <li>240-degree background track arc with rounded stroke caps.</li>
 *     <li>Color-coded active progress fill arc based on value thresholds.</li>
 *     <li>Tapered retro pointer shaft with a laser core line and metallic pivot hub.</li>
 *     <li>Min/Max scale labels positioned via polar trigonometry at arc ends.</li>
 *     <li>Formatted digital value readout positioned inside the gauge.</li>
 * </ul>
 */
public class ArcGaugeView extends View {

    // Gauge geometry: Start at 150° (bottom-left) and sweep 240° clockwise
    private static final float START_ANGLE = 150f;
    private static final float SWEEP_ANGLE = 240f;
    // Pre-allocated Paints, Arc bounds, and Pointer Drawer
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcBounds = new RectF();
    private final RetroPointerDrawer pointerDrawer = new RetroPointerDrawer();
    // Value and range properties
    private float value = 0f;
    private float minValue = 0f;
    private float maxValue = 100f;
    private String minLabel = "E";
    private String maxLabel = "F";
    private String valueText = "0";
    private int activeColor = Color.parseColor("#00E5FF");

    public ArcGaugeView(Context context) {
        super(context);
        init();
    }

    public ArcGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializes paints and anti-aliasing configurations.
     */
    private void init() {
        // Dark track arc paint
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(Color.parseColor("#1C2433"));

        // Active value arc paint
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Center digital text readout paint
        valueTextPaint.setColor(Color.WHITE);
        valueTextPaint.setTextAlign(Paint.Align.CENTER);
        valueTextPaint.setFakeBoldText(true);

        // Min/Max scale label paint
        labelPaint.setColor(Color.parseColor("#5A6B82"));
        labelPaint.setFakeBoldText(true);
    }

    /**
     * Updates gauge data parameters and triggers a view redraw.
     *
     * @param value  Current numeric metric value.
     * @param minVal Minimum metric value (e.g. 0°C, 10V, 0L).
     * @param maxVal Maximum metric value (e.g. 120°C, 16V, 13L).
     * @param minLbl Label for min end (e.g. "C", "E", "10V").
     * @param maxLbl Label for max end (e.g. "H", "F", "16V").
     * @param valTxt Formatted digital string (e.g. "90 °C", "12.6 V", "11.2 L").
     * @param color  Color for the active progress arc fill.
     */
    public void setGaugeData(float value, float minVal, float maxVal, String minLbl, String maxLbl, String valTxt, int color) {
        this.value = value;
        this.minValue = minVal;
        this.maxValue = maxVal;
        this.minLabel = minLbl;
        this.maxLabel = maxLbl;
        this.valueText = valTxt;
        this.activeColor = color;
        invalidate(); // Request view redraw on UI thread
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = (int) (width * 0.70f);
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Calculate center point and gauge radius
        float cx = w / 2f;
        float cy = h * 0.58f;
        float padding = w * 0.12f;
        float radius = (Math.min(w, h) / 2f) - padding;
        if (radius <= 0) return;

        float strokeWidth = radius * 0.22f;
        trackPaint.setStrokeWidth(strokeWidth);
        arcPaint.setStrokeWidth(strokeWidth);
        arcPaint.setColor(activeColor);

        arcBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // 1. Draw background track arc
        canvas.drawArc(arcBounds, START_ANGLE, SWEEP_ANGLE, false, trackPaint);

        // 2. Draw active arc fill proportional to value / range fraction
        float fraction = Math.min(1.0f, Math.max(0.0f, (value - minValue) / (maxValue - minValue)));
        if (fraction > 0.001f) {
            float activeSweep = fraction * SWEEP_ANGLE;
            canvas.drawArc(arcBounds, START_ANGLE, activeSweep, false, arcPaint);
        }

        // 3. Draw Min Label positioned using polar coordinates at arc start
        labelPaint.setTextSize(radius * 0.22f);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        float minRad = (float) Math.toRadians(START_ANGLE + 10f);
        float minX = (float) (cx + (radius + strokeWidth) * Math.cos(minRad));
        float minY = (float) (cy + (radius + strokeWidth) * Math.sin(minRad));
        canvas.drawText(minLabel, minX, minY, labelPaint);

        // 4. Draw Max Label positioned using polar coordinates at arc end
        labelPaint.setTextAlign(Paint.Align.RIGHT);
        float maxRad = (float) Math.toRadians(START_ANGLE + SWEEP_ANGLE - 10f);
        float maxX = (float) (cx + (radius + strokeWidth) * Math.cos(maxRad));
        float maxY = (float) (cy + (radius + strokeWidth) * Math.sin(maxRad));
        canvas.drawText(maxLabel, maxX, maxY, labelPaint);

        // 5. Draw Retro Pointer Shaft with laser core & metallic hub
        float pointerAngleDeg = START_ANGLE + fraction * SWEEP_ANGLE;
        pointerDrawer.drawPointer(canvas, cx, cy, radius, strokeWidth, pointerAngleDeg, activeColor);

        // 6. Draw center digital readout value string below pivot hub
        valueTextPaint.setTextSize(radius * 0.38f);
        canvas.drawText(valueText, cx, cy + radius * 0.50f, valueTextPaint);
    }
}
