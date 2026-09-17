package com.diy.obdwizard.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Custom Canvas View for the central RPM and Speedometer Gauge.
 * <p>
 * Redesigned with a retro-futuristic aesthetic featuring:
 * <ul>
 *     <li>High-precision multi-tiered tick marks (Major 1000 RPM, Medium 500 RPM, Minor 250 RPM).</li>
 *     <li>Multi-color gradient progress arc (Cyan to Amber to Red redline).</li>
 *     <li>Tapered retro-futuristic red pointer shaft with a laser core and metallic pivot hub (via RetroPointerDrawer).</li>
 *     <li>Adjusted inner text layout for Digital Speedometer, Shift Badge, Status Lights, and Digital RPM.</li>
 * </ul>
 */
public class RpmSpeedGaugeView extends View {

    // Arc geometry constants: Start at 140° (bottom-left) and sweep 260° clockwise
    private static final float START_ANGLE = 140f;
    private static final float SWEEP_ANGLE = 260f;
    private final int maxRpm = 8000;
    // Shared Retro Pointer Drawer component
    private final RetroPointerDrawer pointerDrawer = new RetroPointerDrawer();
    // Pre-allocated RectF
    private final RectF arcBounds = new RectF();
    // Telemetry state values
    private int rpm = 0;
    private int speedKmh = 0;
    private boolean isEngineHot = false;
    private boolean isWarningActive = false;
    // Paints pre-allocated for Canvas rendering
    private Paint trackPaint;
    private Paint arcPaint;
    // Tick paints
    private Paint tickMajorPaint;
    private Paint tickMajorRedPaint;
    private Paint tickMinorPaint;
    private Paint tickMinorRedPaint;
    // Scale text paints
    private Paint scaleTextPaint;
    private Paint scaleTextRedPaint;
    // Inner readout text paints
    private Paint speedTextPaint;
    private Paint speedUnitPaint;
    private Paint badgeBgPaint;
    private Paint badgeTextPaint;
    private Paint rpmTextPaint;

    public RpmSpeedGaugeView(Context context) {
        super(context);
        init();
    }

    public RpmSpeedGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RpmSpeedGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializes paints and anti-aliasing configurations for vector rendering.
     */
    private void init() {
        // Dark background track arc
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setColor(Color.parseColor("#1C2433"));

        // Active RPM progress arc with rounded caps
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Major scale tick marks (0, 1, 2, 3, 4, 5 x1000 RPM)
        tickMajorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickMajorPaint.setStyle(Paint.Style.STROKE);
        tickMajorPaint.setColor(Color.parseColor("#4A5D78"));
        tickMajorPaint.setStrokeWidth(3.5f);

        // Major redline tick marks (6, 7, 8 x1000 RPM)
        tickMajorRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickMajorRedPaint.setStyle(Paint.Style.STROKE);
        tickMajorRedPaint.setColor(Color.parseColor("#FF3D00"));
        tickMajorRedPaint.setStrokeWidth(3.5f);

        // Minor intermediate tick marks
        tickMinorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickMinorPaint.setStyle(Paint.Style.STROKE);
        tickMinorPaint.setColor(Color.parseColor("#2A384E"));
        tickMinorPaint.setStrokeWidth(1.8f);

        // Minor redline intermediate tick marks
        tickMinorRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickMinorRedPaint.setStyle(Paint.Style.STROKE);
        tickMinorRedPaint.setColor(Color.parseColor("#B32700"));
        tickMinorRedPaint.setStrokeWidth(1.8f);

        // Text paints for normal scale labels
        scaleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scaleTextPaint.setColor(Color.parseColor("#8A99AD"));
        scaleTextPaint.setTextAlign(Paint.Align.CENTER);
        scaleTextPaint.setFakeBoldText(true);

        // Text paints for redline scale labels
        scaleTextRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scaleTextRedPaint.setColor(Color.parseColor("#FF3D00"));
        scaleTextRedPaint.setTextAlign(Paint.Align.CENTER);
        scaleTextRedPaint.setFakeBoldText(true);

        // Speed readout digital text paint
        speedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedTextPaint.setColor(Color.WHITE);
        speedTextPaint.setTextAlign(Paint.Align.CENTER);
        speedTextPaint.setFakeBoldText(true);

        // Speed unit (KM/H) label paint
        speedUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedUnitPaint.setColor(Color.parseColor("#8A99AD"));
        speedUnitPaint.setTextAlign(Paint.Align.CENTER);

        // Badge background paint
        badgeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgeBgPaint.setStyle(Paint.Style.FILL);

        // Badge text paint
        badgeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        badgeTextPaint.setColor(Color.WHITE);
        badgeTextPaint.setTextAlign(Paint.Align.CENTER);
        badgeTextPaint.setFakeBoldText(true);

        // Digital RPM readout text paint
        rpmTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rpmTextPaint.setColor(Color.parseColor("#00E5FF"));
        rpmTextPaint.setTextAlign(Paint.Align.CENTER);
        rpmTextPaint.setFakeBoldText(true);
    }

    /**
     * Updates the gauge with new telemetry values and triggers a view redraw.
     *
     * @param rpm             Current engine RPM.
     * @param speedKmh        Current speed in km/h.
     * @param isEngineHot     True if coolant temperature exceeds warning threshold.
     * @param isWarningActive True if battery/charging system alert is active.
     */
    public void setTelemetry(int rpm, int speedKmh, boolean isEngineHot, boolean isWarningActive) {
        this.rpm = rpm;
        this.speedKmh = speedKmh;
        this.isEngineHot = isEngineHot;
        this.isWarningActive = isWarningActive;
        invalidate(); // Request view redraw on UI thread
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Maintain custom aspect ratio for proper gauge scaling
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = (int) (width * 0.88f);
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Calculate center coordinates and gauge radius
        float cx = w / 2f;
        float cy = h * 0.50f;
        float padding = w * 0.12f;
        float radius = (Math.min(w, h) / 2f) - padding;
        if (radius <= 0) return;

        float strokeWidth = radius * 0.12f;
        trackPaint.setStrokeWidth(strokeWidth);
        arcPaint.setStrokeWidth(strokeWidth);

        // Set bounding rectangle for arc drawing
        arcBounds.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // 1. Draw background arc track
        canvas.drawArc(arcBounds, START_ANGLE, SWEEP_ANGLE, false, trackPaint);

        // 2. Draw active RPM arc using LinearGradient color stops (Cyan -> Orange -> Red)
        float rpmFraction = Math.min(rpm, maxRpm) / (float) maxRpm;
        if (rpmFraction > 0.001f) {
            float activeSweep = rpmFraction * SWEEP_ANGLE;

            LinearGradient gradient = new LinearGradient(
                    cx - radius, cy, cx + radius, cy,
                    new int[]{
                            Color.parseColor("#00E5FF"), // Cyan at low RPM
                            Color.parseColor("#FF9100"), // Orange at mid RPM
                            Color.parseColor("#FF3D00")  // Red at high RPM
                    },
                    new float[]{0.0f, 0.7f, 1.0f},
                    Shader.TileMode.CLAMP
            );
            arcPaint.setShader(gradient);
            canvas.drawArc(arcBounds, START_ANGLE, activeSweep, false, arcPaint);
        }

        // 3. Draw high-precision intermediate tick marks (Major, Medium 500 RPM, Minor 250 RPM)
        scaleTextPaint.setTextSize(radius * 0.11f);
        scaleTextRedPaint.setTextSize(radius * 0.11f);

        for (float tickVal = 0; tickVal <= 8.01f; tickVal += 0.25f) {
            float fraction = tickVal / 8.0f;
            float angleDeg = START_ANGLE + fraction * SWEEP_ANGLE;
            double rad = Math.toRadians(angleDeg);

            boolean isRedline = tickVal >= 6.0f;
            boolean isMajor = Math.abs(tickVal - Math.round(tickVal)) < 0.01f;
            boolean isMedium = Math.abs((tickVal * 2) - Math.round(tickVal * 2)) < 0.01f;

            float innerR;
            float outerR = radius + strokeWidth * 0.8f;
            Paint currentTickPaint;

            if (isMajor) {
                innerR = radius - strokeWidth * 0.9f;
                currentTickPaint = isRedline ? tickMajorRedPaint : tickMajorPaint;
            } else if (isMedium) {
                innerR = radius - strokeWidth * 0.5f;
                currentTickPaint = isRedline ? tickMinorRedPaint : tickMinorPaint;
            } else {
                innerR = radius - strokeWidth * 0.3f;
                currentTickPaint = isRedline ? tickMinorRedPaint : tickMinorPaint;
            }

            float x1 = (float) (cx + innerR * Math.cos(rad));
            float y1 = (float) (cy + innerR * Math.sin(rad));
            float x2 = (float) (cx + outerR * Math.cos(rad));
            float y2 = (float) (cy + outerR * Math.sin(rad));

            canvas.drawLine(x1, y1, x2, y2, currentTickPaint);

            // Draw numerical label only for major ticks (0, 1, 2, 3, 4, 5, 6, 7, 8)
            if (isMajor) {
                int labelVal = Math.round(tickVal);
                float textR = radius + strokeWidth * 1.6f;
                float tx = (float) (cx + textR * Math.cos(rad));
                float ty = (float) (cy + textR * Math.sin(rad)) + (radius * 0.04f);

                canvas.drawText(String.valueOf(labelVal), tx, ty, isRedline ? scaleTextRedPaint : scaleTextPaint);
            }
        }

        // 4. Draw dynamic Shift Badge above speedometer (only visible when SHIFT UP is required)
        if (rpm > 3500) {
            float badgeWidth = radius * 0.60f;
            float badgeHeight = radius * 0.18f;
            float badgeY = cy - radius * 0.58f;

            RectF badgeRect = new RectF(cx - badgeWidth / 2f, badgeY, cx + badgeWidth / 2f, badgeY + badgeHeight);

            badgeBgPaint.setColor(Color.parseColor("#FF3D00")); // Red warning background
            canvas.drawRoundRect(badgeRect, badgeHeight / 2f, badgeHeight / 2f, badgeBgPaint);
            badgeTextPaint.setTextSize(badgeHeight * 0.58f);
            canvas.drawText("SHIFT UP!", cx, badgeY + badgeHeight * 0.68f, badgeTextPaint);
        }

        // 5. Inner Text Adjustments: Large Digital Speedometer & Unit Label
        speedTextPaint.setTextSize(radius * 0.55f);
        canvas.drawText(String.valueOf(speedKmh), cx, cy - radius * 0.15f, speedTextPaint);

        speedUnitPaint.setTextSize(radius * 0.13f);
        canvas.drawText("KM/H", cx, cy + radius * 0.08f, speedUnitPaint);

        // 6. Draw Corrected Retro Pointer with Offset Pivot Geometry
        // We calculate the target point on the arc center line first, then compute angle from offset hub.
        float targetAngleDeg = START_ANGLE + rpmFraction * SWEEP_ANGLE;
        double targetRad = Math.toRadians(targetAngleDeg);

        // Target tip point on the arc track (slightly inside the outer edge)
        float tipR_from_center = radius - strokeWidth * 0.2f;
        float targetX = (float) (cx + tipR_from_center * Math.cos(targetRad));
        float targetY = (float) (cy + tipR_from_center * Math.sin(targetRad));

        float hubX = cx;
        float hubY = cy + radius * 0.28f;

        // Calculate angle and distance from the offset hub to the target arc point
        float correctedPointerAngle = (float) Math.toDegrees(Math.atan2(targetY - hubY, targetX - hubX));
        float needleDist = (float) Math.sqrt(Math.pow(targetX - hubX, 2) + Math.pow(targetY - hubY, 2));

        pointerDrawer.drawPointer(canvas, hubX, hubY, radius, needleDist, strokeWidth, correctedPointerAngle, Color.parseColor("#FF1744"));

        // 7. Draw bottom status warning icons
        drawStatusIcons(canvas, cx, cy + radius * 0.58f, radius * 0.12f);

        // 8. Draw Digital RPM readout in a futuristic "Glass Badge"
        float rpmTextSize = radius * 0.18f;
        float rpmY = cy + radius * 0.85f; // Increased padding from status icons

        // Dynamic color selection based on intensity
        int color;
        if (rpm > 6000) color = Color.parseColor("#FF3D00"); // Redline
        else if (rpm > 3500) color = Color.parseColor("#FF9100"); // High RPM
        else color = Color.parseColor("#00E5FF"); // Normal

        // 8a. Draw subtle background capsule (Glass look)
        String rpmVal = String.valueOf(rpm);
        String rpmUnit = " RPM";

        rpmTextPaint.setTextSize(rpmTextSize);
        float valWidth = rpmTextPaint.measureText(rpmVal);
        rpmTextPaint.setTextSize(rpmTextSize * 0.55f);
        float unitWidth = rpmTextPaint.measureText(rpmUnit);
        float totalWidth = valWidth + unitWidth;

        float bgPaddingH = radius * 0.12f;
        float bgPaddingV = radius * 0.06f;
        RectF rpmBgRect = new RectF(
                cx - (totalWidth / 2f) - bgPaddingH,
                rpmY - rpmTextSize,
                cx + (totalWidth / 2f) + bgPaddingH,
                rpmY + bgPaddingV * 2
        );

        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.argb(25, 255, 255, 255)); // Semi-transparent glass
        canvas.drawRoundRect(rpmBgRect, 8, 8, bgPaint);

        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeWidth(1.5f);
        bgPaint.setColor(Color.argb(60, 255, 255, 255)); // Subtle border
        canvas.drawRoundRect(rpmBgRect, 8, 8, bgPaint);

        // 8b. Draw Text with glow effect at high RPM
        rpmTextPaint.setColor(color);
        if (rpm > 3500) {
            rpmTextPaint.setShadowLayer(15f, 0, 0, color);
        }

        // Align and draw value
        rpmTextPaint.setTextAlign(Paint.Align.LEFT);
        rpmTextPaint.setTextSize(rpmTextSize);
        float startX = cx - (totalWidth / 2f);
        canvas.drawText(rpmVal, startX, rpmY, rpmTextPaint);

        // Draw "RPM" unit in smaller, muted text
        rpmTextPaint.clearShadowLayer();
        rpmTextPaint.setTextSize(rpmTextSize * 0.55f);
        rpmTextPaint.setColor(Color.parseColor("#8A99AD"));
        canvas.drawText(rpmUnit, startX + valWidth, rpmY, rpmTextPaint);
    }

    /**
     * Draws warning lights below the central speedometer.
     */
    private void drawStatusIcons(Canvas canvas, float cx, float cy, float iconRadius) {
        Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Bike System Indicator (Left) - Always Green when online
        iconPaint.setColor(Color.parseColor("#00E676"));
        canvas.drawCircle(cx - iconRadius * 3f, cy, iconRadius * 0.4f, iconPaint);

        // Alert Warning Indicator (Center) - Amber if warning, else muted dark
        iconPaint.setColor(isWarningActive ? Color.parseColor("#FFAB00") : Color.parseColor("#2A3546"));
        canvas.drawCircle(cx, cy, iconRadius * 0.4f, iconPaint);

        // Engine Temp Warning Indicator (Right) - Cyan if normal, Red if hot
        iconPaint.setColor(isEngineHot ? Color.parseColor("#FF3D00") : Color.parseColor("#00E5FF"));
        canvas.drawCircle(cx + iconRadius * 3f, cy, iconRadius * 0.4f, iconPaint);
    }
}
