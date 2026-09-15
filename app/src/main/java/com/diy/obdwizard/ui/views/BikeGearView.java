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
 * Custom View for displaying a motorcycle's 5-speed gear bar (1, N, 2, 3, 4, 5).
 * <p>
 * Displays gears in a horizontal capsule/pill bar format, replacing traditional auto PRNDS.
 * Highlights the currently active gear with a bright pill capsule background:
 * <ul>
 *     <li>Neutral ('N'): Highlighted in vibrant Green (#00E676).</li>
 *     <li>Gears ('1', '2', '3', '4', '5'): Highlighted in bright Electric Cyan (#00E5FF).</li>
 * </ul>
 */
public class BikeGearView extends View {

    // Supported motorcycle gear order: 1, Neutral, 2, 3, 4, 5
    private static final String[] GEARS = {"1", "N", "2", "3", "4", "5"};
    // Pre-allocated paints and bounding boxes for efficient Canvas drawing
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF boundsRect = new RectF();
    private final RectF pillRect = new RectF();
    private String currentGear = "N";

    public BikeGearView(Context context) {
        super(context);
        init();
    }

    public BikeGearView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BikeGearView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Configures paint styles and colors for the capsule container and text labels.
     */
    private void init() {
        // Container background
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.parseColor("#141A23"));

        // Outer subtle border
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(Color.parseColor("#263244"));

        // Active selected gear pill background fill
        activeBgPaint.setStyle(Paint.Style.FILL);

        // Inactive gear text paint
        textPaint.setColor(Color.parseColor("#5A6B82"));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // Active selected gear text paint
        activeTextPaint.setColor(Color.WHITE);
        activeTextPaint.setTextAlign(Paint.Align.CENTER);
        activeTextPaint.setFakeBoldText(true);
    }

    /**
     * Updates the currently active gear position and triggers a redraw.
     *
     * @param gear The calculated gear string ("1", "N", "2", "3", "4", "5").
     */
    public void setGear(String gear) {
        if (gear == null || gear.trim().isEmpty() || gear.equals("-")) {
            this.currentGear = "N"; // Default to Neutral if undetermined
        } else {
            this.currentGear = gear.trim().toUpperCase();
        }
        invalidate(); // Request view redraw on UI thread
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Maintain pill shape ratio
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = (int) (width * 0.22f);
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        // 1. Draw rounded outer capsule container and stroke
        float cornerRadius = h / 2f;
        boundsRect.set(1f, 1f, w - 1f, h - 1f);

        canvas.drawRoundRect(boundsRect, cornerRadius, cornerRadius, bgPaint);
        canvas.drawRoundRect(boundsRect, cornerRadius, cornerRadius, borderPaint);

        // 2. Calculate horizontal slot width for dividing 6 gear positions
        float slotWidth = (w - 8f) / (float) GEARS.length;
        textPaint.setTextSize(h * 0.45f);
        activeTextPaint.setTextSize(h * 0.48f);

        // 3. Draw each gear symbol and active highlight pill
        for (int i = 0; i < GEARS.length; i++) {
            String gearSymbol = GEARS[i];
            float slotLeft = 4f + (i * slotWidth);
            float slotRight = slotLeft + slotWidth;
            float cx = slotLeft + (slotWidth / 2f);
            float cy = h / 2f;

            boolean isActive = gearSymbol.equalsIgnoreCase(currentGear);

            if (isActive) {
                // Draw active highlighted pill background
                pillRect.set(slotLeft + 2f, 4f, slotRight - 2f, h - 4f);
                if (gearSymbol.equalsIgnoreCase("N")) {
                    activeBgPaint.setColor(Color.parseColor("#00E676")); // Vibrant Green for Neutral
                    activeTextPaint.setColor(Color.BLACK);
                } else {
                    activeBgPaint.setColor(Color.parseColor("#00E5FF")); // Electric Cyan for 1-5 gears
                    activeTextPaint.setColor(Color.BLACK);
                }
                canvas.drawRoundRect(pillRect, (h - 8f) / 2f, (h - 8f) / 2f, activeBgPaint);
                canvas.drawText(gearSymbol, cx, cy + (h * 0.16f), activeTextPaint);
            } else {
                // Draw inactive gray gear label
                canvas.drawText(gearSymbol, cx, cy + (h * 0.15f), textPaint);
            }
        }
    }
}
