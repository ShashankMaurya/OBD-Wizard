package com.diy.obdwizard.ui.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Reusable Canvas drawing helper for rendering a retro-futuristic tapered pointer shaft
 * with a laser core center line and metallic pivot hub cap across custom gauge views.
 */
public class RetroPointerDrawer {

    private final Paint needleShaftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needleGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needleCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hubOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hubBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hubInnerDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path needlePath = new Path();

    public RetroPointerDrawer() {
        needleShaftPaint.setStyle(Paint.Style.FILL);

        needleGlowPaint.setStyle(Paint.Style.STROKE);
        needleGlowPaint.setStrokeWidth(3f);

        needleCorePaint.setStyle(Paint.Style.STROKE);
        needleCorePaint.setColor(Color.WHITE); // Laser core line

        hubOuterPaint.setStyle(Paint.Style.FILL);
        hubOuterPaint.setColor(Color.parseColor("#141A23")); // Metallic hub dark fill

        hubBorderPaint.setStyle(Paint.Style.STROKE);
        hubBorderPaint.setColor(Color.parseColor("#2B3648")); // Metallic border
        hubBorderPaint.setStrokeWidth(2.5f);

        hubInnerDotPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Draws a retro-futuristic pointer shaft and pivot hub on the given Canvas.
     *
     * @param canvas       The target Canvas.
     * @param cxHub        Pivot hub center X coordinate.
     * @param cyHub        Pivot hub center Y coordinate.
     * @param hubRadiusBase Base radius for calculating hub and needle width.
     * @param tipRadius    Actual length of the needle from hub center to tip.
     * @param strokeWidth  Stroke width of the arc track.
     * @param angleDeg     Absolute angle in degrees that the pointer is facing.
     * @param pointerColor Main color of the pointer shaft and hub dot (e.g. #FF1744 red or #00E5FF cyan).
     */
    public void drawPointer(Canvas canvas, float cxHub, float cyHub, float hubRadiusBase, float tipRadius, float strokeWidth, float angleDeg, int pointerColor) {
        needleShaftPaint.setColor(pointerColor);
        needleGlowPaint.setColor((pointerColor & 0x00FFFFFF) | 0x80000000); // 50% opacity glow
        hubInnerDotPaint.setColor(pointerColor);
        needleCorePaint.setStrokeWidth(Math.max(1.0f, hubRadiusBase * 0.02f));

        double pointerRad = Math.toRadians(angleDeg);

        // Tip position extending towards arc scale
        float tipX = cxHub + (float) (tipRadius * Math.cos(pointerRad));
        float tipY = cyHub + (float) (tipRadius * Math.sin(pointerRad));

        // Base width for tapered needle
        float baseWidth = hubRadiusBase * 0.045f;
        double perpRad = pointerRad + (Math.PI / 2.0);

        float p1x = cxHub + (float) (baseWidth * Math.cos(perpRad));
        float p1y = cyHub + (float) (baseWidth * Math.sin(perpRad));
        float p2x = cxHub - (float) (baseWidth * Math.cos(perpRad));
        float p2y = cyHub - (float) (baseWidth * Math.sin(perpRad));

        // Tapered needle path
        needlePath.reset();
        needlePath.moveTo(p1x, p1y);
        needlePath.lineTo(tipX, tipY);
        needlePath.lineTo(p2x, p2y);
        needlePath.close();

        // 1. Draw glowing needle body
        canvas.drawPath(needlePath, needleShaftPaint);
        canvas.drawPath(needlePath, needleGlowPaint);

        // 2. Draw white laser core center line
        canvas.drawLine(cxHub, cyHub, tipX, tipY, needleCorePaint);

        // 3. Draw Center Metallic Hub Cap
        float hubRadius = hubRadiusBase * 0.12f;
        canvas.drawCircle(cxHub, cyHub, hubRadius, hubOuterPaint);
        canvas.drawCircle(cxHub, cyHub, hubRadius, hubBorderPaint);
        canvas.drawCircle(cxHub, cyHub, hubRadius * 0.35f, hubInnerDotPaint);
    }
}
